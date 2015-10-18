package graviton.game.maps;

import graviton.common.Utils;
import graviton.game.client.player.Player;
import graviton.game.creature.Creature;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.locks.ReentrantLock;


/**
 * Created by Botan on 22/06/2015.
 */

@Data
public class Maps {
    private final ScheduledExecutorService sheduler = Executors.newScheduledThreadPool(1);
    private final ReentrantLock locker = new ReentrantLock();

    private final int id;
    private final long date;
    private final int width, heigth, X, Y;
    private final String places, key, data;

    private final Map<Integer, Cell> cells;
    private final String descriptionPacket;
    private final String loadingPacket;
    private Map<Integer, Creature> creatures;

    public Maps(int id, long date, int width, int heigth, String places, String key, String data, String position) {
        this.id = id;
        this.date = date;
        this.width = width;
        this.heigth = heigth;
        this.places = places;
        this.key = key;
        this.data = data;
        String[] mapInfos = position.split(",");
        this.X = Integer.parseInt(mapInfos[0]);
        this.Y = Integer.parseInt(mapInfos[1]);
        this.cells = decompileCells(data);
        this.loadingPacket = "GA;2;" + this.getId() + ";";
        this.descriptionPacket = "GDM|" + id + "|0" + date + "|" + (!key.isEmpty() ? key : data);
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
        locker.lock();
        creatures.values().forEach(creature -> creature.send(packet));
        locker.unlock();
    }

    public Cell getRandomCell() {
        List<Cell> goodCells = new ArrayList<>();
        this.cells.values().stream().filter(Cell::isWalkable).filter(cell1 -> cell1.getCreatures().isEmpty()).forEach(cell2 -> goodCells.add(cell2));
        return goodCells.get(Utils.getRandomValue(0, goodCells.size() - 1));
    }

    public void sendGdf(Player player) {
        this.cells.values().stream().filter(cell -> cell.getInteractiveObject() != null).forEach(cell1 -> player.send(cell1.getInteractiveObject().getGDF()));
    }

    public void refreshCreature(Creature creature) {
        send("GM|+" + creature.getGm());
    }

    /**
     * Tools
     **/
    private Map<Integer, Cell> decompileCells(String data) {
        Map<Integer, Cell> cells = new ConcurrentHashMap<>();
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
            cells.put(f / 10, new Cell(f / 10, this, walkable, obj));
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
