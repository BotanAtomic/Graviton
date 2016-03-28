package graviton.game.maps;

import com.google.inject.Inject;
import com.google.inject.Injector;
import graviton.common.Utils;
import graviton.factory.NpcFactory;
import graviton.game.GameManager;
import graviton.game.client.player.Player;
import graviton.game.creature.Creature;
import graviton.game.creature.monster.MonsterGrade;
import graviton.game.creature.monster.MonsterGroup;
import graviton.game.creature.npc.Npc;
import graviton.game.enums.IdType;
import lombok.Data;
import org.jooq.Record;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

import static graviton.database.utils.game.Tables.MAPS;


/**
 * Created by Botan on 22/06/2015.
 */

@Data
public class Maps {
    private final ReentrantLock locker = new ReentrantLock();
    private final int id, width, heigth, X, Y, maxGroup;
    private final long date;
    private final String places, key, data;
    private final String descriptionPacket, loadingPacket;
    private final Map<Integer, Cell> cells;
    @Inject
    NpcFactory npcFactory;
    @Inject
    GameManager gameManager;
    private Map<Integer, Creature> creatures;

    public Maps(int id, Record record, Injector injector) {
        injector.injectMembers(this);
        this.id = id;
        this.date = Long.parseLong(record.getValue(MAPS.DATE));
        this.width = record.getValue(MAPS.WIDTH);
        this.heigth = record.getValue(MAPS.HEIGTH);
        this.places = record.getValue(MAPS.PLACES);
        this.key = record.getValue(MAPS.KEY);
        this.data = record.getValue(MAPS.DATE);
        String position = record.getValue(MAPS.MAPPOS);
        this.X = Integer.parseInt(position.split(",")[0]);
        this.Y = Integer.parseInt(position.split(",")[1]);
        this.cells = decompileCells(record.getValue(MAPS.MAPDATA), record.getValue(MAPS.CELLS), injector);
        this.loadingPacket = "GA;2;" + this.getId() + ";";
        this.descriptionPacket = "GDM|" + id + "|0" + this.date + "|" + (!this.key.isEmpty() ? this.key : this.data);
        this.maxGroup = record.getValue(MAPS.NUMGROUP);
        npcFactory.getNpcOnMap(this).forEach(this::addCreature);
        if (this.cells.size() > 5)
            spawnMonster(configureMonster(record.getValue(MAPS.MONSTERS)));
    }

    private List<MonsterGrade> configureMonster(String group) {
        List<MonsterGrade> monsters = new ArrayList<>();
        int monsterId, level;

        for (String mob : group.split("\\|")) {
            if (mob.isEmpty())
                continue;
            try {
                monsterId = Integer.parseInt(mob.split(",")[0]);
                level = Integer.parseInt(mob.split(",")[1]);
            } catch (NumberFormatException e) {
                continue;
            }

            if (monsterId == 0 || level == 0 || gameManager.getMonster(monsterId).getGrade(level) == null)
                continue;
            monsters.add(gameManager.getMonster(monsterId).getGrade(level));
        }
        return monsters;
    }

    private void spawnMonster(List<MonsterGrade> monsters) {
        int i = 0;
        for (MonsterGrade grade : monsters) {
            i++;
            addCreature(new MonsterGroup(monsters, this));
            if (i == maxGroup) break;
        }
    }

    public int getNextId(IdType type) {
        int startIndex = type.MAXIMAL_ID - this.id * 1000;
        for (int i = startIndex; i > startIndex - 1000 && i >= type.MINIMAL_ID; i--)
            if (!this.creatures.containsKey(i))
                return i;
        return 0;
    }

    public Map<Integer, Creature> getCreatures(IdType type) {
        Map<Integer, Creature> creatures = new HashMap<>();
        switch (type) {
            case NPC:
                this.creatures.values().stream().filter(creature -> creature instanceof Npc).forEach(creature -> creatures.put(creature.getId(), creature));
                return creatures;
            case CREATURE:
                this.creatures.values().stream().filter(creature -> creature instanceof Player).forEach(creature -> creatures.put(creature.getId(), creature));
                return creatures;
            case MONSTER_GROUP:
                this.creatures.values().stream().filter(creature -> creature instanceof MonsterGroup).forEach(creature -> creatures.put(creature.getId(), creature));
                return creatures;
        }
        return creatures;
    }

    public Cell getCell(int id) {
        return cells.get(id);
    }

    public void addCreature(Creature creature) {
        if (this.creatures == null)
            this.creatures = new ConcurrentHashMap<>();
        loadCreature(creature);
        creature.getPosition().getCell().addCreature(creature);
        creature.getPosition().setMap(this);
    }

    public void removeCreature(Creature creature) {
        this.creatures.remove(creature.getId());
        creature.getPosition().getCell().removeCreature(creature);
        unloadCreature(creature);
    }

    private void loadCreature(Creature creature) {
        creature.send(descriptionPacket);
        creature.send(loadingPacket);
        send("GM|+" + creature.getGm());
        this.creatures.put(creature.getId(), creature);
    }

    public void changeCell(Creature creature, Cell cell) {
        cell.addCreature(creature);
        refreshCreature(creature);
    }

    private void unloadCreature(Creature creature) {
        send("GM|-" + creature.getId());
        this.creatures.remove(creature.getId());
    }

    public String getGMs() {
        StringBuilder packet = new StringBuilder();
        creatures.values().forEach(creature -> packet.append("GM|+").append(creature.getGm()).append('\u0000'));
        return packet.toString();
    }

    public void send(String packet) {
        try {
            locker.lock();
            creatures.values().forEach(creature -> creature.send(packet));
        } finally {
            locker.unlock();
        }
    }

    public Cell getRandomCell() {
        List<Cell> goodCells = new ArrayList<>();
        this.cells.values().stream().filter(Cell::isWalkable).filter(cell1 -> cell1.getCreatures().isEmpty()).forEach(goodCells::add);
        return goodCells.get(new Random().nextInt(goodCells.size()));
    }

    public boolean supportFight() {
        return !this.places.isEmpty() && !this.places.equals("|");
    }

    public void sendGdf(Player player) {
        this.cells.values().stream().filter(cell -> cell.getInteractiveObject() != null).forEach(cell1 -> player.send(cell1.getInteractiveObject().getGDF()));
    }

    public void refreshCreature(Creature creature) {
        send("GM|-" + creature.getId());
        send("GM|+" + creature.getGm());
    }


    /**
     * Tools
     **/
    private Map<Integer, Cell> decompileCells(String data, String cellsData, Injector injector) {
        Map<Integer, Cell> cells = new ConcurrentHashMap<>();

        if (data.isEmpty()) {
            String[] infos = cellsData.split("\\|");
            for (String cellData : infos) {
                boolean walkable = false;
                int id = -1, object = -1;
                String[] cellInfos = cellData.split(",");
                try {
                    walkable = cellInfos[2].equals("1");
                    id = Integer.parseInt(cellInfos[0]);
                    if (!cellInfos[3].trim().equals(""))
                        object = Integer.parseInt(cellInfos[3]);
                } catch (Exception ignored) {
                }
                cells.put(id, new Cell(id, this, walkable, object, injector));
            }
            return cells;
        }

        String cellData;
        List<Byte> cellInfos = new ArrayList<>();
        for (int f = 0; f < data.length(); f += 10) {
            cellData = data.substring(f, f + 10);
            for (int i = 0; i < cellData.length(); i++)
                cellInfos.add((byte) getHashValue(cellData.charAt(i)));
            boolean walkable = (cellInfos.get(2) & 56) >> 3 != 0;
            int layerObject2 = ((cellInfos.get(0) & 2) << 12) + ((cellInfos.get(7) & 1) << 12) + (cellInfos.get(8) << 6) + cellInfos.get(9);
            boolean layerObject2Interactive = ((cellInfos.get(7) & 2) >> 1) != 0;
            int obj = (layerObject2Interactive ? layerObject2 : -1);
            Cell cell = new Cell(f / 10, this, walkable, obj, injector);
            cells.put(f / 10, cell);
            cellInfos.clear();
        }
        return cells;
    }

    private int getHashValue(char value) {
        char[] hash = Utils.HASH;
        for (int a = 0; a <= hash.length; a++)
            if (hash[a] == value)
                return a;
        return -1;
    }

}
