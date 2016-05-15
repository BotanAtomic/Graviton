package graviton.factory;

import com.google.common.collect.ArrayListMultimap;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.name.Named;
import graviton.api.Factory;
import graviton.api.InjectSetting;
import graviton.database.Database;
import graviton.enums.DataType;
import graviton.game.GameManager;
import graviton.game.client.Account;
import graviton.game.client.player.Player;
import graviton.game.client.player.packet.Packet;
import graviton.game.client.player.packet.Packets;
import graviton.game.common.Stats;
import graviton.game.enums.Classe;
import graviton.game.enums.StatsType;
import graviton.game.experience.Experience;
import graviton.game.object.Object;
import graviton.game.object.ObjectPosition;
import graviton.game.object.panoply.PanoplyTemplate;
import graviton.game.statistics.Statistics;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.jooq.Record;
import org.jooq.UpdateSetFirstStep;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import static graviton.database.utils.game.Tables.ITEMS;
import static graviton.database.utils.login.Tables.PLAYERS;

/**
 * Created by Botan on 24/12/2015.
 */
@Data
@Slf4j
public class PlayerFactory extends Factory<Player> {
    private final Map<Integer, Player> players;
    private final ReentrantLock locker;
    @Inject
    Injector injector;
    private GameManager gameManager;
    private Map<Classe, Map<Integer, Integer>> classData;

    private Map<Packets, Packet> packets;

    @InjectSetting("server.id")
    private int serverId;
    @InjectSetting("game.map")
    private int startMap;
    @InjectSetting("game.cell")
    private int startCell;
    @InjectSetting("game.kamas")
    private int startKamas;
    @InjectSetting("game.level")
    private int startLevel;

    @Inject
    @Named("database.game")
    private Database gameDatabase;

    @Inject
    public PlayerFactory(GameManager gameManager, @Named("database.login") Database database) {
        super(database);
        this.gameManager = gameManager;
        this.players = new ConcurrentHashMap<>();
        this.locker = new ReentrantLock();
    }

    public ArrayList<Player> load(Account account) {
        ArrayList<Player> players = database.getResult(PLAYERS, PLAYERS.ACCOUNT.equal(account.getId())).stream().filter(record -> record.getValue(PLAYERS.SERVER) == serverId).map(record -> new Player(account, record, injector)).collect(Collectors.toCollection(ArrayList::new));
        return players;
        //return database.getResult(PLAYERS, PLAYERS.ACCOUNT.equal(account.getId())).stream().filter(record -> record.getValue(PLAYERS.SERVER) == (serverId)).map(record -> new Player(account, record, injector)).collect(Collectors.toList());
    }

    public int getNextId() {
        return database.getNextId(PLAYERS, PLAYERS.ID);
    }

    public boolean checkName(String name) {
        return database.getRecord(PLAYERS, PLAYERS.NAME.equal(name)) != null;
    }

    public void remove(Player player) {
        database.remove(PLAYERS, PLAYERS.ID.equal(player.getId()));
        deleteObject(player.getObjects().values());
    }

    private void deleteObject(Collection<Object> objects) {
        objects.forEach(object1 -> gameDatabase.remove(ITEMS, ITEMS.ID.equal(object1.getId())));
    }

    public boolean create(Player player) {
        int id = database.getDSLContext().insertInto(PLAYERS, PLAYERS.ID, PLAYERS.ACCOUNT, PLAYERS.NAME, PLAYERS.SEX, PLAYERS.GFX, PLAYERS.CLASS,
                PLAYERS.COLORS, PLAYERS.SPELLS, PLAYERS.SPELLPOINTS, PLAYERS.CAPITAL, PLAYERS.LEVEL, PLAYERS.EXPERIENCE, PLAYERS.POSITION, PLAYERS.SERVER)
                .values(player.getId(),
                        player.getAccount().getId(),
                        player.getName(),
                        player.getSex(),
                        player.getGfx(),
                        player.getClasse().getId(),
                        player.getColor(1) + ";" + player.getColor(2) + ";" + player.getColor(3),
                        player.parseSpells(),
                        player.getSpellPoints(),
                        player.getCapital(),
                        player.getLevel(),
                        player.getExperience(),
                        player.getMap().getId() + ";" + player.getPosition().getCell().getId(),
                        serverId).execute();
        return id > 0;
    }

    public void update(Player player) {
        UpdateSetFirstStep firstStep = database.getDSLContext().update(PLAYERS);
        firstStep.set(PLAYERS.NAME, player.getName());
        firstStep.set(PLAYERS.SEX, player.getSex());
        firstStep.set(PLAYERS.GFX, player.getGfx());
        firstStep.set(PLAYERS.COLORS, player.getColor(1) + ";" + player.getColor(2) + ";" + player.getColor(3));
        firstStep.set(PLAYERS.SPELLS, player.parseSpells());
        firstStep.set(PLAYERS.STATISTICS, player.parseStatistics());
        firstStep.set(PLAYERS.CAPITAL, player.getCapital());
        firstStep.set(PLAYERS.SPELLPOINTS, player.getSpellPoints());
        firstStep.set(PLAYERS.ITEMS, player.parseObject());
        firstStep.set(PLAYERS.POSITION, player.getMap().getId() + ";" + player.getCell().getId());
        firstStep.set(PLAYERS.ALIGNEMENT, player.parseAlignement());
        firstStep.set(PLAYERS.KAMAS, player.getKamas());
        firstStep.set(PLAYERS.SPELLPOINTS, player.getGfx()).where(PLAYERS.ID.equal(player.getId())).execute();
    }


    @Override
    public DataType getType() {
        return DataType.PLAYER;
    }

    @Override
    public Map<Integer, Player> getElements() {
        return this.players;
    }


    public void add(Player player) {
        this.players.put(player.getId(), player);
    }

    public void delete(int id) {
        this.players.remove(id);
    }

    public List<Player> getOnlinePlayers() {
        List<Player> onlinePlayers = new ArrayList<>();
        this.players.values().stream().filter(Player::isOnline).forEach(onlinePlayers::add);
        return onlinePlayers;
    }

    @Override
    public Player get(java.lang.Object object) {
        return this.players.get(object);
    }

    public Player getByName(String name) {
        final Player[] player = {null};
        this.players.values().stream().filter(player1 -> player1.getName().equals(name)).forEach(playerSelected -> player[0] = playerSelected);
        return player[0];
    }

    public void send(String packet) {
        try {
            locker.lock();
            getOnlinePlayers().forEach(player -> player.send(packet));
        } finally {
            locker.unlock();
        }
    }

    @Override
    public void save() {
        log.debug("saving players...");
        this.players.values().forEach(player -> update(player));
        log.debug("saving players items...");
        this.players.values().forEach(player -> player.getObjects().values().forEach(object -> object.update()));
    }

    @Override
    public void configure() {
        this.classData = (Map<Classe, Map<Integer, Integer>>) decodeObject("classData");
        gameManager.setExperience(new Experience(decodeObject("experience/player"), decodeObject("experience/job"), decodeObject("experience/mount"), decodeObject("experience/pvp")));
        initPackets();
    }

    private void initPackets() {
        Map<Packets, Packet> packets = new Object2ObjectOpenHashMap<>();

        packets.put(Packets.ALK, player ->
                new StringBuilder("|").append(player.getId()).append(";").append(player.getName()).append(";").append(player.getLevel()).append(";").
                        append(player.getGfx()).append(";").
                        append((player.getColor(1) != -1 ? Integer.toHexString(player.getColor(1)) : "-1")).append(";").
                        append((player.getColor(2) != -1 ? Integer.toHexString(player.getColor(2)) : "-1")).append(";").
                        append((player.getColor(3) != -1 ? Integer.toHexString(player.getColor(3)) : "-1")).append(";").
                        append(player.getPacket(Packets.GMS)).append(";0;1;;;;;").toString()
        );

        packets.put(Packets.PM, player -> {
            StringBuilder builder = new StringBuilder();
            builder.append(player.getId()).append(";").append(player.getName()).append(";").append(player.getGfx()).append(";");
            builder.append(player.getColor(1)).append(";");
            builder.append(player.getColor(2)).append(";");
            builder.append(player.getColor(3)).append(";");
            builder.append(player.getPacket(Packets.GMS)).append(";");
            builder.append(player.getLife(false)).append(",").append(player.getLife(true)).append(";");
            builder.append(player.getLevel()).append(";").append(player.getInitiative()).append(";");
            builder.append(player.getTotalStatistics().getEffect(Stats.ADD_PROS)).append(";0");
            return builder.toString();
        });

        packets.put(Packets.GM, player -> {
            StringBuilder builder = new StringBuilder();
            builder.append(player.getCell().getId());
            builder.append(";").append(player.getOrientation()).append(";0;").append(player.getId()).append(";");
            builder.append(player.getName()).append(";").append(player.getClasse().getId());
            builder.append((player.getTitle() > 0 ? ("," + player.getTitle()) : ""));
            builder.append(";").append(player.getGfx()).append("^").append(player.getSize()).append(";").append(player.getSex()).append(";");
            builder.append(player.getAlignement().getType().getId()).append(",0,");
            builder.append((player.getAlignement().isShowWings() ? player.getAlignement().getGrade() : "0")).append(",");
            builder.append(player.getLevel() + player.getId());
            if (player.getAlignement().isShowWings() && player.getAlignement().getDeshonnor() > 0)
                builder.append(",").append(1).append(';');
            else
                builder.append(";");
            builder.append(player.getColor(1) == -1 ? "-1" : Integer.toHexString(player.getColor(1))).append(";");
            builder.append(player.getColor(2) == -1 ? "-1" : Integer.toHexString(player.getColor(2))).append(";");
            builder.append(player.getColor(3) == -1 ? "-1" : Integer.toHexString(player.getColor(3))).append(";");
            builder.append(player.getPacket(Packets.GMS)).append(";");
            builder.append(player.getLevel() > 99 ? (player.getLevel() > 199 ? (2) : (1)) : (0)).append(";;;");
            if (player.getGuild() != null)
                builder.append(player.getGuild().getName()).append(";").append(player.getGuild().getEmblem()).append(";");
            else
                builder.append(";;0;;;");
            return builder.toString();
        });

        packets.put(Packets.GMS, player -> {
            StringBuilder builder = new StringBuilder();
            ObjectPosition[] positions = {ObjectPosition.ARME, ObjectPosition.COIFFE, ObjectPosition.CAPE, ObjectPosition.FAMILIER, ObjectPosition.BOUCLIER};
            for (ObjectPosition position : positions) {
                if (player.getObjectByPosition(position.id) != null)
                    builder.append(Integer.toHexString(player.getObjectByPosition(position.id).getTemplate().getId()));
                builder.append(",");
            }
            return builder.toString().substring(0, builder.length() - 1);
        });

        packets.put(Packets.WC, player -> {
            StringBuilder builder = new StringBuilder();
            builder.append("WC").append(player.getMap().getId());
            player.getZaaps().forEach(zaap -> builder.append("|").append(gameManager.getMap(zaap.getMap().getId()).getId()).append(";").append(zaap.getCost(player.getMap())));
            return builder.toString();
        });

        packets.put(Packets.ASK, player -> {
            StringBuilder builder = new StringBuilder("ASK|");
            builder.append(player.getId()).append("|").append(player.getName()).append("|");
            builder.append(player.getLevel()).append("|");
            builder.append(player.getClasse().getId()).append("|");
            builder.append(player.getSex()).append("|");
            builder.append(player.getGfx()).append("|");
            builder.append((player.getColor(1)) == -1 ? "-1" : Integer.toHexString(player.getColor(1))).append("|");
            builder.append((player.getColor(2)) == -1 ? "-1" : Integer.toHexString(player.getColor(2))).append("|");
            builder.append((player.getColor(3)) == -1 ? "-1" : Integer.toHexString(player.getColor(3))).append("|");
            builder.append(player.getPacket(Packets.ASKI));
            return builder.toString();
        });


        packets.put(Packets.ASKI, player -> {
            assert !player.getObjects().isEmpty() : "";
            StringBuilder builder = new StringBuilder();
            player.getObjects().values().forEach(object -> builder.append(object.parseItem()));
            return builder.toString();
        });

        packets.put(Packets.As, player -> {
            Statistics stuff = player.getStuffStatistics();
            Statistics total = player.getTotalStatistics();
            Statistics base = player.getStatistics().get(StatsType.BASE);
            Statistics buff = player.getStatistics().get(StatsType.BUFF);
            Statistics gift = player.getStatistics().get(StatsType.GIFT);


            return new StringBuilder("As").append(player.getExperience()).append(",").append(gameManager.getPlayerExperience(player.getLevel())).append(",").append(gameManager.getPlayerExperience(player.getLevel() + 1)).append("|").
                    append(player.getKamas()).append("|").append(player.getCapital()).append("|").append(player.getSpellPoints()).append("|").
                    append(player.getAlignement().getType().getId()).append("~").
                    append(player.getAlignement().getType().getId()).append(",").append(player.getAlignement().getGrade()).append(",").append(player.getAlignement().getGrade()).append(",").append(player.getAlignement().getHonor()).append(",").append(player.getAlignement().getDeshonnor()).append(",").append(player.getAlignement().isShowWings() ? "1" : "0").append("|").
                    append(player.getLife(false)).append(",").append(player.getLife(true)).append("|").
                    append(10000).append(",10000|"). //TODO : Energy
                    append(player.getInitiative()).append("|").
                    append(base.getEffect(176) + stuff.getEffect(176) + (int) Math.ceil(total.getEffect(123) / 10) + buff.getEffect(176)).append("|").
                    append(base.getEffect(Stats.ADD_PA)).append(",").append(stuff.getEffect(Stats.ADD_PA)).append(",").append(gift.getEffect(Stats.ADD_PA)).append(",").append(buff.getEffect(Stats.ADD_PA)).append(",").append(total.getEffect(Stats.ADD_PA)).append("|").
                    append(base.getEffect(Stats.ADD_PM)).append(",").append(stuff.getEffect(Stats.ADD_PM)).append(",").append(gift.getEffect(Stats.ADD_PM)).append(",").append(buff.getEffect(Stats.ADD_PM)).append(",").append(total.getEffect(Stats.ADD_PM)).append("|").
                    append(base.getEffect(Stats.ADD_FORC)).append(",").append(stuff.getEffect(Stats.ADD_FORC)).append(",").append(gift.getEffect(Stats.ADD_FORC)).append(",").append(buff.getEffect(Stats.ADD_FORC)).append("|").
                    append(base.getEffect(Stats.ADD_VITA)).append(",").append(stuff.getEffect(Stats.ADD_VITA)).append(",").append(gift.getEffect(Stats.ADD_VITA)).append(",").append(buff.getEffect(Stats.ADD_VITA)).append("|").
                    append(base.getEffect(Stats.ADD_SAGE)).append(",").append(stuff.getEffect(Stats.ADD_SAGE)).append(",").append(gift.getEffect(Stats.ADD_SAGE)).append(",").append(buff.getEffect(Stats.ADD_SAGE)).append("|").
                    append(base.getEffect(Stats.ADD_CHAN)).append(",").append(stuff.getEffect(Stats.ADD_CHAN)).append(",").append(gift.getEffect(Stats.ADD_CHAN)).append(",").append(buff.getEffect(Stats.ADD_CHAN)).append("|").
                    append(base.getEffect(Stats.ADD_AGIL)).append(",").append(stuff.getEffect(Stats.ADD_AGIL)).append(",").append(gift.getEffect(Stats.ADD_AGIL)).append(",").append(buff.getEffect(Stats.ADD_AGIL)).append("|").
                    append(base.getEffect(Stats.ADD_INTE)).append(",").append(stuff.getEffect(Stats.ADD_INTE)).append(",").append(gift.getEffect(Stats.ADD_INTE)).append(",").append(buff.getEffect(Stats.ADD_INTE)).append("|").
                    append(base.getEffect(Stats.ADD_PO)).append(",").append(stuff.getEffect(Stats.ADD_PO)).append(",").append(gift.getEffect(Stats.ADD_PO)).append(",").append(buff.getEffect(Stats.ADD_PO)).append("|").
                    append(base.getEffect(Stats.CREATURE)).append(",").append(stuff.getEffect(Stats.CREATURE)).append(",").append(gift.getEffect(Stats.CREATURE)).append(",").append(buff.getEffect(Stats.CREATURE)).append("|").
                    append(base.getEffect(Stats.ADD_DOMA)).append(",").append(stuff.getEffect(Stats.ADD_DOMA)).append(",").append(gift.getEffect(Stats.ADD_DOMA)).append(",").append(buff.getEffect(Stats.ADD_DOMA)).append("|").
                    append(base.getEffect(Stats.ADD_PDOM)).append(",").append(stuff.getEffect(Stats.ADD_PDOM)).append(",").append(gift.getEffect(Stats.ADD_PDOM)).append(",").append(buff.getEffect(Stats.ADD_PDOM)).append("|").
                    append("0,0,0,0|").
                    append(base.getEffect(Stats.ADD_PERDOM)).append(",").append(stuff.getEffect(Stats.ADD_PERDOM)).append(",").append(gift.getEffect(Stats.ADD_PERDOM)).append(",").append(buff.getEffect(Stats.ADD_PERDOM)).append("|").
                    append(base.getEffect(Stats.ADD_SOIN)).append(",").append(stuff.getEffect(Stats.ADD_SOIN)).append(",").append(gift.getEffect(Stats.ADD_SOIN)).append(",").append(buff.getEffect(Stats.ADD_SOIN)).append("|").
                    append(base.getEffect(Stats.TRAPDOM)).append(",").append(stuff.getEffect(Stats.TRAPDOM)).append(",").append(gift.getEffect(Stats.TRAPDOM)).append(",").append(buff.getEffect(Stats.TRAPDOM)).append("|").
                    append(base.getEffect(Stats.TRAPPER)).append(",").append(stuff.getEffect(Stats.TRAPPER)).append(",").append(gift.getEffect(Stats.TRAPPER)).append(",").append(buff.getEffect(Stats.TRAPPER)).append("|").
                    append(base.getEffect(Stats.RETDOM)).append(",").append(stuff.getEffect(Stats.RETDOM)).append(",").append(gift.getEffect(Stats.RETDOM)).append(",").append(buff.getEffect(Stats.RETDOM)).append("|").
                    append(base.getEffect(Stats.ADD_CC)).append(",").append(stuff.getEffect(Stats.ADD_CC)).append(",").append(gift.getEffect(Stats.ADD_CC)).append(",").append(buff.getEffect(Stats.ADD_CC)).append("|").
                    append(base.getEffect(Stats.ADD_EC)).append(",").append(stuff.getEffect(Stats.ADD_EC)).append(",").append(gift.getEffect(Stats.ADD_EC)).append(",").append(buff.getEffect(Stats.ADD_EC)).append("|").
                    append(base.getEffect(Stats.ADD_AFLEE)).append(",").append(stuff.getEffect(Stats.ADD_AFLEE)).append(",").append(0).append(",").append(buff.getEffect(Stats.ADD_AFLEE)).append(",").append(buff.getEffect(Stats.ADD_AFLEE)).append("|").
                    append(base.getEffect(Stats.ADD_MFLEE)).append(",").append(stuff.getEffect(Stats.ADD_MFLEE)).append(",").append(0).append(",").append(buff.getEffect(Stats.ADD_MFLEE)).append(",").append(buff.getEffect(Stats.ADD_MFLEE)).append("|").
                    append(base.getEffect(Stats.ADD_R_NEU)).append(",").append(stuff.getEffect(Stats.ADD_R_NEU)).append(",").append(0).append(",").append(buff.getEffect(Stats.ADD_R_NEU)).append(",").append(buff.getEffect(Stats.ADD_R_NEU)).append("|").
                    append(base.getEffect(Stats.ADD_RP_NEU)).append(",").append(stuff.getEffect(Stats.ADD_RP_NEU)).append(",").append(0).append(",").append(buff.getEffect(Stats.ADD_RP_NEU)).append(",").append(buff.getEffect(Stats.ADD_RP_NEU)).append("|").
                    append(base.getEffect(Stats.ADD_R_PVP_NEU)).append(",").append(stuff.getEffect(Stats.ADD_R_PVP_NEU)).append(",").append(0).append(",").append(buff.getEffect(Stats.ADD_R_PVP_NEU)).append(",").append(buff.getEffect(Stats.ADD_R_PVP_NEU)).append("|").
                    append(base.getEffect(Stats.ADD_RP_PVP_NEU)).append(",").append(stuff.getEffect(Stats.ADD_RP_PVP_NEU)).append(",").append(0).append(",").append(buff.getEffect(Stats.ADD_RP_PVP_NEU)).append(",").append(buff.getEffect(Stats.ADD_RP_PVP_NEU)).append("|").
                    append(base.getEffect(Stats.ADD_R_TER)).append(",").append(stuff.getEffect(Stats.ADD_R_TER)).append(",").append(0).append(",").append(buff.getEffect(Stats.ADD_R_TER)).append(",").append(buff.getEffect(Stats.ADD_R_TER)).append("|").
                    append(base.getEffect(Stats.ADD_RP_TER)).append(",").append(stuff.getEffect(Stats.ADD_RP_TER)).append(",").append(0).append(",").append(buff.getEffect(Stats.ADD_RP_TER)).append(",").append(buff.getEffect(Stats.ADD_RP_TER)).append("|").
                    append(base.getEffect(Stats.ADD_R_PVP_TER)).append(",").append(stuff.getEffect(Stats.ADD_R_PVP_TER)).append(",").append(0).append(",").append(buff.getEffect(Stats.ADD_R_PVP_TER)).append(",").append(buff.getEffect(Stats.ADD_R_PVP_TER)).append("|").
                    append(base.getEffect(Stats.ADD_RP_PVP_TER)).append(",").append(stuff.getEffect(Stats.ADD_RP_PVP_TER)).append(",").append(0).append(",").append(buff.getEffect(Stats.ADD_RP_PVP_TER)).append(",").append(buff.getEffect(Stats.ADD_RP_PVP_TER)).append("|").
                    append(base.getEffect(Stats.ADD_R_EAU)).append(",").append(stuff.getEffect(Stats.ADD_R_EAU)).append(",").append(0).append(",").append(buff.getEffect(Stats.ADD_R_EAU)).append(",").append(buff.getEffect(Stats.ADD_R_EAU)).append("|").
                    append(base.getEffect(Stats.ADD_RP_EAU)).append(",").append(stuff.getEffect(Stats.ADD_RP_EAU)).append(",").append(0).append(",").append(buff.getEffect(Stats.ADD_RP_EAU)).append(",").append(buff.getEffect(Stats.ADD_RP_EAU)).append("|").
                    append(base.getEffect(Stats.ADD_R_PVP_EAU)).append(",").append(stuff.getEffect(Stats.ADD_R_PVP_EAU)).append(",").append(0).append(",").append(buff.getEffect(Stats.ADD_R_PVP_EAU)).append(",").append(buff.getEffect(Stats.ADD_R_PVP_EAU)).append("|").
                    append(base.getEffect(Stats.ADD_RP_PVP_EAU)).append(",").append(stuff.getEffect(Stats.ADD_RP_PVP_EAU)).append(",").append(0).append(",").append(buff.getEffect(Stats.ADD_RP_PVP_EAU)).append(",").append(buff.getEffect(Stats.ADD_RP_PVP_EAU)).append("|").
                    append(base.getEffect(Stats.ADD_R_AIR)).append(",").append(stuff.getEffect(Stats.ADD_R_AIR)).append(",").append(0).append(",").append(buff.getEffect(Stats.ADD_R_AIR)).append(",").append(buff.getEffect(Stats.ADD_R_AIR)).append("|").
                    append(base.getEffect(Stats.ADD_RP_AIR)).append(",").append(stuff.getEffect(Stats.ADD_RP_AIR)).append(",").append(0).append(",").append(buff.getEffect(Stats.ADD_RP_AIR)).append(",").append(buff.getEffect(Stats.ADD_RP_AIR)).append("|").
                    append(base.getEffect(Stats.ADD_R_PVP_AIR)).append(",").append(stuff.getEffect(Stats.ADD_R_PVP_AIR)).append(",").append(0).append(",").append(buff.getEffect(Stats.ADD_R_PVP_AIR)).append(",").append(buff.getEffect(Stats.ADD_R_PVP_AIR)).append("|").
                    append(base.getEffect(Stats.ADD_RP_PVP_AIR)).append(",").append(stuff.getEffect(Stats.ADD_RP_PVP_AIR)).append(",").append(0).append(",").append(buff.getEffect(Stats.ADD_RP_PVP_AIR)).append(",").append(buff.getEffect(Stats.ADD_RP_PVP_AIR)).append("|").
                    append(base.getEffect(Stats.ADD_R_FEU)).append(",").append(stuff.getEffect(Stats.ADD_R_FEU)).append(",").append(0).append(",").append(buff.getEffect(Stats.ADD_R_FEU)).append(",").append(buff.getEffect(Stats.ADD_R_FEU)).append("|").
                    append(base.getEffect(Stats.ADD_RP_FEU)).append(",").append(stuff.getEffect(Stats.ADD_RP_FEU)).append(",").append(0).append(",").append(buff.getEffect(Stats.ADD_RP_FEU)).append(",").append(buff.getEffect(Stats.ADD_RP_FEU)).append("|").
                    append(base.getEffect(Stats.ADD_R_PVP_FEU)).append(",").append(stuff.getEffect(Stats.ADD_R_PVP_FEU)).append(",").append(0).append(",").append(buff.getEffect(Stats.ADD_R_PVP_FEU)).append(",").append(buff.getEffect(Stats.ADD_R_PVP_FEU)).append("|").
                    append(base.getEffect(Stats.ADD_RP_PVP_FEU)).append(",").append(stuff.getEffect(Stats.ADD_RP_PVP_FEU)).append(",").append(0).append(",").append(buff.getEffect(Stats.ADD_RP_PVP_FEU)).append(",").append(buff.getEffect(Stats.ADD_RP_PVP_FEU)).append("|").toString();
        });

        packets.put(Packets.SL, player -> {
            StringBuilder builder = new StringBuilder("SL");
            player.getSpells().values().forEach(spell -> builder.append(spell.getTemplate()).append("~").append(spell.getLevel()).append("~").append(player.getSpellPlace().get(spell.getTemplate())).append(";"));
            return builder.toString();
        });

        packets.put(Packets.OS, player -> {
            player.getStatistics().get(StatsType.PANOPLY).getEffects().clear();
            StringBuilder builder = new StringBuilder();
            List<Integer> equippedObject;
            for (PanoplyTemplate panoplyTemplate : player.getPanoplys()) {
                equippedObject = player.getNumberOfEquippedObject(panoplyTemplate.getId());
                builder.append("OS").append("+").append(panoplyTemplate.getId()).append("|");
                for (Integer objectTemplate : equippedObject)
                    builder.append(objectTemplate).append(equippedObject.indexOf(objectTemplate) == (equippedObject.size() - 1) ? "" : ";");
                builder.append("|").append(panoplyTemplate.getStatistics(equippedObject.size(), player).toString()).append("\n");
            }
            return builder.toString();
        });

        this.packets = Collections.unmodifiableMap(packets);
    }
}
