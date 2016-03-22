package graviton.factory;

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
import graviton.game.common.Stats;
import graviton.game.enums.Classe;
import graviton.game.enums.ObjectPosition;
import graviton.game.enums.StatsType;
import graviton.game.experience.Experience;
import graviton.game.object.Object;
import graviton.game.spells.Spell;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.jooq.UpdateSetFirstStep;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.Lock;
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
    private final Lock locker;
    @Inject
    Injector injector;
    private GameManager gameManager;
    private Map<Classe, Map<Integer, Integer>> classData;

    private Map<String, Packet> packets;

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

    public List<Player> load(Account account) {
        List<Player> players = new CopyOnWriteArrayList<>();
        players.addAll(database.getResult(PLAYERS, PLAYERS.ACCOUNT.equal(account.getId())).stream().filter(record -> record.getValue(PLAYERS.SERVER) == (serverId)).map(record -> new Player(account, record, injector)).collect(Collectors.toList()));
        return players;
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
        final Player[] player = {null};
        try {
            locker.lock();

            if (object instanceof Integer)
                player[0] = this.players.get(object);
            else
                this.players.values().stream().filter(player1 -> player1.getName().equals(object)).forEach(playerSelected -> player[0] = playerSelected);

        } finally {
            locker.unlock();
        }
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
        Map<String, Packet> packets = new HashMap<>();
        packets.put("ALK", player -> {
            StringBuilder builder = new StringBuilder();
            builder.append("|").append(player.getId()).append(";").append(player.getName()).append(";").append(player.getLevel()).append(";");
            builder.append(player.getGfx()).append(";");
            builder.append((player.getColor(1) != -1 ? Integer.toHexString(player.getColor(1)) : "-1")).append(";");
            builder.append((player.getColor(2) != -1 ? Integer.toHexString(player.getColor(2)) : "-1")).append(";");
            builder.append((player.getColor(3) != -1 ? Integer.toHexString(player.getColor(3)) : "-1")).append(";");
            builder.append(player.getPacket("GMS")).append(";0;1;;;;;");
            return builder.toString();
        });

        packets.put("PM", player -> {
            StringBuilder builder = new StringBuilder();
            builder.append(player.getId()).append(";").append(player.getName()).append(";").append(player.getGfx()).append(";");
            builder.append(player.getColor(1)).append(";");
            builder.append(player.getColor(2)).append(";");
            builder.append(player.getColor(3)).append(";");
            builder.append(player.getPacket("GMS")).append(";");
            builder.append(player.getLife(false)).append(",").append(player.getLife(true)).append(";");
            builder.append(player.getLevel()).append(";").append(player.getInitiative()).append(";");
            builder.append(player.getTotalStatistics().getEffect(Stats.ADD_PROS)).append(";0");
            return builder.toString();
        });

        packets.put("GM", player -> {
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
            builder.append(player.getPacket("GMS")).append(";");
            builder.append(player.getLevel() > 99 ? (player.getLevel() > 199 ? (2) : (1)) : (0)).append(";;;");
            if (player.getGuild() != null)
                builder.append(player.getGuild().getName()).append(";").append(player.getGuild().getEmblem()).append(";");
            else
                builder.append(";;0;;;");
            return builder.toString();
        });

        packets.put("GMS", player -> {
            StringBuilder builder = new StringBuilder();
            ObjectPosition[] positions = {ObjectPosition.ARME, ObjectPosition.COIFFE, ObjectPosition.CAPE, ObjectPosition.FAMILIER, ObjectPosition.BOUCLIER};
            for (ObjectPosition position : positions) {
                if (player.getObjectByPosition(position) != null)
                    builder.append(Integer.toHexString(player.getObjectByPosition(position).getTemplate().getId()));
                builder.append(",");
            }
            return builder.toString().substring(0, builder.length() - 1);
        });

        packets.put("WC", player -> {
            StringBuilder builder = new StringBuilder();
            builder.append("WC").append(player.getMap().getId());
            player.getZaaps().forEach(zaap -> builder.append("|").append(gameManager.getMap(zaap.getMap().getId()).getId()).append(";").append(zaap.getCost(player.getMap())));
            return builder.toString();
        });

        packets.put("ASK", player -> {
            StringBuilder builder = new StringBuilder();
            builder.append("ASK|");
            builder.append(player.getId()).append("|").append(player.getName()).append("|");
            builder.append(player.getLevel()).append("|");
            builder.append(player.getClasse().getId()).append("|");
            builder.append(player.getSex()).append("|");
            builder.append(player.getGfx()).append("|");
            builder.append((player.getColor(1)) == -1 ? "-1" : Integer.toHexString(player.getColor(1))).append("|");
            builder.append((player.getColor(2)) == -1 ? "-1" : Integer.toHexString(player.getColor(2))).append("|");
            builder.append((player.getColor(3)) == -1 ? "-1" : Integer.toHexString(player.getColor(3))).append("|");
            builder.append(player.getPacket("ASKI"));
            return builder.toString();
        });

        packets.put("ASKI", player -> {
            if (player.getObjects().isEmpty()) return "";
            StringBuilder builder = new StringBuilder();
            player.getObjects().values().forEach(object -> builder.append(object.parseItem()));
            return builder.toString();
        });

        packets.put("As", player -> {
            StringBuilder builder = new StringBuilder();
            builder.append("As");
            builder.append(player.getExperience()).append(",").append(getGameManager().getPlayerExperience(player.getLevel())).append(",").append(gameManager.getPlayerExperience(player.getLevel() + 1)).append("|");
            builder.append(player.getKamas()).append("|").append(player.getCapital()).append("|").append(player.getSpellPoints()).append("|");
            builder.append(player.getAlignement().getType().getId()).append("~");
            builder.append(player.getAlignement().getType().getId()).append(",").append(player.getAlignement().getGrade()).append(",").append(player.getAlignement().getGrade()).append(",").append(player.getAlignement().getHonor()).append(",").append(player.getAlignement().getDeshonnor()).append(",").append(player.getAlignement().isShowWings() ? "1" : "0").append("|");
            builder.append(player.getLife(false)).append(",").append(player.getLife(true)).append("|");
            builder.append(10000).append(",10000|");
            builder.append(player.getInitiative()).append("|");
            builder.append(player.getStatistics().get(StatsType.BASE).getEffect(Stats.ADD_PROS) + player.getStatistics().get(StatsType.STUFF).getEffect(Stats.ADD_PROS) + ((int) Math.ceil(player.getStatistics().get(StatsType.BASE).getEffect(Stats.ADD_CHAN) / 10)) + player.getStatistics().get(StatsType.BUFF).getEffect(Stats.ADD_PROS)).append("|");
            builder.append(player.getStatistics().get(StatsType.BASE).getEffect(Stats.ADD_PA)).append(",").append(player.getStatistics().get(StatsType.STUFF).getEffect(Stats.ADD_PA)).append(",").append(player.getStatistics().get(StatsType.GIFT).getEffect(Stats.ADD_PA)).append(",").append(player.getStatistics().get(StatsType.BUFF).getEffect(Stats.ADD_PA)).append(",").append(player.getTotalStatistics().getEffect(Stats.ADD_PA)).append("|");
            builder.append(player.getStatistics().get(StatsType.BASE).getEffect(Stats.ADD_PM)).append(",").append(player.getStatistics().get(StatsType.STUFF).getEffect(Stats.ADD_PM)).append(",").append(player.getStatistics().get(StatsType.GIFT).getEffect(Stats.ADD_PM)).append(",").append(player.getStatistics().get(StatsType.BUFF).getEffect(Stats.ADD_PM)).append(",").append(player.getTotalStatistics().getEffect(Stats.ADD_PM)).append("|");
            builder.append(player.getStatistics().get(StatsType.BASE).getEffect(Stats.ADD_FORC)).append(",").append(player.getStatistics().get(StatsType.STUFF).getEffect(Stats.ADD_FORC)).append(",").append(player.getStatistics().get(StatsType.GIFT).getEffect(Stats.ADD_FORC)).append(",").append(player.getStatistics().get(StatsType.BUFF).getEffect(Stats.ADD_FORC)).append("|");
            builder.append(player.getStatistics().get(StatsType.BASE).getEffect(Stats.ADD_VITA)).append(",").append(player.getStatistics().get(StatsType.STUFF).getEffect(Stats.ADD_VITA)).append(",").append(player.getStatistics().get(StatsType.GIFT).getEffect(Stats.ADD_VITA)).append(",").append(player.getStatistics().get(StatsType.BUFF).getEffect(Stats.ADD_VITA)).append("|");
            builder.append(player.getStatistics().get(StatsType.BASE).getEffect(Stats.ADD_SAGE)).append(",").append(player.getStatistics().get(StatsType.STUFF).getEffect(Stats.ADD_SAGE)).append(",").append(player.getStatistics().get(StatsType.GIFT).getEffect(Stats.ADD_SAGE)).append(",").append(player.getStatistics().get(StatsType.BUFF).getEffect(Stats.ADD_SAGE)).append("|");
            builder.append(player.getStatistics().get(StatsType.BASE).getEffect(Stats.ADD_CHAN)).append(",").append(player.getStatistics().get(StatsType.STUFF).getEffect(Stats.ADD_CHAN)).append(",").append(player.getStatistics().get(StatsType.GIFT).getEffect(Stats.ADD_CHAN)).append(",").append(player.getStatistics().get(StatsType.BUFF).getEffect(Stats.ADD_CHAN)).append("|");
            builder.append(player.getStatistics().get(StatsType.BASE).getEffect(Stats.ADD_AGIL)).append(",").append(player.getStatistics().get(StatsType.STUFF).getEffect(Stats.ADD_AGIL)).append(",").append(player.getStatistics().get(StatsType.GIFT).getEffect(Stats.ADD_AGIL)).append(",").append(player.getStatistics().get(StatsType.BUFF).getEffect(Stats.ADD_AGIL)).append("|");
            builder.append(player.getStatistics().get(StatsType.BASE).getEffect(Stats.ADD_INTE)).append(",").append(player.getStatistics().get(StatsType.STUFF).getEffect(Stats.ADD_INTE)).append(",").append(player.getStatistics().get(StatsType.GIFT).getEffect(Stats.ADD_INTE)).append(",").append(player.getStatistics().get(StatsType.BUFF).getEffect(Stats.ADD_INTE)).append("|");
            builder.append(player.getStatistics().get(StatsType.BASE).getEffect(Stats.ADD_PO)).append(",").append(player.getStatistics().get(StatsType.STUFF).getEffect(Stats.ADD_PO)).append(",").append(player.getStatistics().get(StatsType.GIFT).getEffect(Stats.ADD_PO)).append(",").append(player.getStatistics().get(StatsType.BUFF).getEffect(Stats.ADD_PO)).append("|");
            builder.append(player.getStatistics().get(StatsType.BASE).getEffect(Stats.CREATURE)).append(",").append(player.getStatistics().get(StatsType.STUFF).getEffect(Stats.CREATURE)).append(",").append(player.getStatistics().get(StatsType.GIFT).getEffect(Stats.CREATURE)).append(",").append(player.getStatistics().get(StatsType.BUFF).getEffect(Stats.CREATURE)).append("|");
            builder.append(player.getStatistics().get(StatsType.BASE).getEffect(Stats.ADD_DOMA)).append(",").append(player.getStatistics().get(StatsType.STUFF).getEffect(Stats.ADD_DOMA)).append(",").append(player.getStatistics().get(StatsType.GIFT).getEffect(Stats.ADD_DOMA)).append(",").append(player.getStatistics().get(StatsType.BUFF).getEffect(Stats.ADD_DOMA)).append("|");
            builder.append(player.getStatistics().get(StatsType.BASE).getEffect(Stats.ADD_PDOM)).append(",").append(player.getStatistics().get(StatsType.STUFF).getEffect(Stats.ADD_PDOM)).append(",").append(player.getStatistics().get(StatsType.GIFT).getEffect(Stats.ADD_PDOM)).append(",").append(player.getStatistics().get(StatsType.BUFF).getEffect(Stats.ADD_PDOM)).append("|");
            builder.append("0,0,0,0|");
            builder.append(player.getStatistics().get(StatsType.BASE).getEffect(Stats.ADD_PERDOM)).append(",").append(player.getStatistics().get(StatsType.STUFF).getEffect(Stats.ADD_PERDOM)).append(",").append(player.getStatistics().get(StatsType.GIFT).getEffect(Stats.ADD_PERDOM)).append(",").append(player.getStatistics().get(StatsType.BUFF).getEffect(Stats.ADD_PERDOM)).append("|");
            builder.append(player.getStatistics().get(StatsType.BASE).getEffect(Stats.ADD_SOIN)).append(",").append(player.getStatistics().get(StatsType.STUFF).getEffect(Stats.ADD_SOIN)).append(",").append(player.getStatistics().get(StatsType.GIFT).getEffect(Stats.ADD_SOIN)).append(",").append(player.getStatistics().get(StatsType.BUFF).getEffect(Stats.ADD_SOIN)).append("|");
            builder.append(player.getStatistics().get(StatsType.BASE).getEffect(Stats.TRAPDOM)).append(",").append(player.getStatistics().get(StatsType.STUFF).getEffect(Stats.TRAPDOM)).append(",").append(player.getStatistics().get(StatsType.GIFT).getEffect(Stats.TRAPDOM)).append(",").append(player.getStatistics().get(StatsType.BUFF).getEffect(Stats.TRAPDOM)).append("|");
            builder.append(player.getStatistics().get(StatsType.BASE).getEffect(Stats.TRAPPER)).append(",").append(player.getStatistics().get(StatsType.STUFF).getEffect(Stats.TRAPPER)).append(",").append(player.getStatistics().get(StatsType.GIFT).getEffect(Stats.TRAPPER)).append(",").append(player.getStatistics().get(StatsType.BUFF).getEffect(Stats.TRAPPER)).append("|");
            builder.append(player.getStatistics().get(StatsType.BASE).getEffect(Stats.RETDOM)).append(",").append(player.getStatistics().get(StatsType.STUFF).getEffect(Stats.RETDOM)).append(",").append(player.getStatistics().get(StatsType.GIFT).getEffect(Stats.RETDOM)).append(",").append(player.getStatistics().get(StatsType.BUFF).getEffect(Stats.RETDOM)).append("|");
            builder.append(player.getStatistics().get(StatsType.BASE).getEffect(Stats.ADD_CC)).append(",").append(player.getStatistics().get(StatsType.STUFF).getEffect(Stats.ADD_CC)).append(",").append(player.getStatistics().get(StatsType.GIFT).getEffect(Stats.ADD_CC)).append(",").append(player.getStatistics().get(StatsType.BUFF).getEffect(Stats.ADD_CC)).append("|");
            builder.append(player.getStatistics().get(StatsType.BASE).getEffect(Stats.ADD_EC)).append(",").append(player.getStatistics().get(StatsType.STUFF).getEffect(Stats.ADD_EC)).append(",").append(player.getStatistics().get(StatsType.GIFT).getEffect(Stats.ADD_EC)).append(",").append(player.getStatistics().get(StatsType.BUFF).getEffect(Stats.ADD_EC)).append("|");
            builder.append(player.getStatistics().get(StatsType.BASE).getEffect(Stats.ADD_AFLEE)).append(",").append(player.getStatistics().get(StatsType.STUFF).getEffect(Stats.ADD_AFLEE)).append(",").append(0).append(",").append(player.getStatistics().get(StatsType.BUFF).getEffect(Stats.ADD_AFLEE)).append(",").append(player.getStatistics().get(StatsType.BUFF).getEffect(Stats.ADD_AFLEE)).append("|");
            builder.append(player.getStatistics().get(StatsType.BASE).getEffect(Stats.ADD_MFLEE)).append(",").append(player.getStatistics().get(StatsType.STUFF).getEffect(Stats.ADD_MFLEE)).append(",").append(0).append(",").append(player.getStatistics().get(StatsType.BUFF).getEffect(Stats.ADD_MFLEE)).append(",").append(player.getStatistics().get(StatsType.BUFF).getEffect(Stats.ADD_MFLEE)).append("|");
            builder.append(player.getStatistics().get(StatsType.BASE).getEffect(Stats.ADD_R_NEU)).append(",").append(player.getStatistics().get(StatsType.STUFF).getEffect(Stats.ADD_R_NEU)).append(",").append(0).append(",").append(player.getStatistics().get(StatsType.BUFF).getEffect(Stats.ADD_R_NEU)).append(",").append(player.getStatistics().get(StatsType.BUFF).getEffect(Stats.ADD_R_NEU)).append("|");
            builder.append(player.getStatistics().get(StatsType.BASE).getEffect(Stats.ADD_RP_NEU)).append(",").append(player.getStatistics().get(StatsType.STUFF).getEffect(Stats.ADD_RP_NEU)).append(",").append(0).append(",").append(player.getStatistics().get(StatsType.BUFF).getEffect(Stats.ADD_RP_NEU)).append(",").append(player.getStatistics().get(StatsType.BUFF).getEffect(Stats.ADD_RP_NEU)).append("|");
            builder.append(player.getStatistics().get(StatsType.BASE).getEffect(Stats.ADD_R_PVP_NEU)).append(",").append(player.getStatistics().get(StatsType.STUFF).getEffect(Stats.ADD_R_PVP_NEU)).append(",").append(0).append(",").append(player.getStatistics().get(StatsType.BUFF).getEffect(Stats.ADD_R_PVP_NEU)).append(",").append(player.getStatistics().get(StatsType.BUFF).getEffect(Stats.ADD_R_PVP_NEU)).append("|");
            builder.append(player.getStatistics().get(StatsType.BASE).getEffect(Stats.ADD_RP_PVP_NEU)).append(",").append(player.getStatistics().get(StatsType.STUFF).getEffect(Stats.ADD_RP_PVP_NEU)).append(",").append(0).append(",").append(player.getStatistics().get(StatsType.BUFF).getEffect(Stats.ADD_RP_PVP_NEU)).append(",").append(player.getStatistics().get(StatsType.BUFF).getEffect(Stats.ADD_RP_PVP_NEU)).append("|");
            builder.append(player.getStatistics().get(StatsType.BASE).getEffect(Stats.ADD_R_TER)).append(",").append(player.getStatistics().get(StatsType.STUFF).getEffect(Stats.ADD_R_TER)).append(",").append(0).append(",").append(player.getStatistics().get(StatsType.BUFF).getEffect(Stats.ADD_R_TER)).append(",").append(player.getStatistics().get(StatsType.BUFF).getEffect(Stats.ADD_R_TER)).append("|");
            builder.append(player.getStatistics().get(StatsType.BASE).getEffect(Stats.ADD_RP_TER)).append(",").append(player.getStatistics().get(StatsType.STUFF).getEffect(Stats.ADD_RP_TER)).append(",").append(0).append(",").append(player.getStatistics().get(StatsType.BUFF).getEffect(Stats.ADD_RP_TER)).append(",").append(player.getStatistics().get(StatsType.BUFF).getEffect(Stats.ADD_RP_TER)).append("|");
            builder.append(player.getStatistics().get(StatsType.BASE).getEffect(Stats.ADD_R_PVP_TER)).append(",").append(player.getStatistics().get(StatsType.STUFF).getEffect(Stats.ADD_R_PVP_TER)).append(",").append(0).append(",").append(player.getStatistics().get(StatsType.BUFF).getEffect(Stats.ADD_R_PVP_TER)).append(",").append(player.getStatistics().get(StatsType.BUFF).getEffect(Stats.ADD_R_PVP_TER)).append("|");
            builder.append(player.getStatistics().get(StatsType.BASE).getEffect(Stats.ADD_RP_PVP_TER)).append(",").append(player.getStatistics().get(StatsType.STUFF).getEffect(Stats.ADD_RP_PVP_TER)).append(",").append(0).append(",").append(player.getStatistics().get(StatsType.BUFF).getEffect(Stats.ADD_RP_PVP_TER)).append(",").append(player.getStatistics().get(StatsType.BUFF).getEffect(Stats.ADD_RP_PVP_TER)).append("|");
            builder.append(player.getStatistics().get(StatsType.BASE).getEffect(Stats.ADD_R_EAU)).append(",").append(player.getStatistics().get(StatsType.STUFF).getEffect(Stats.ADD_R_EAU)).append(",").append(0).append(",").append(player.getStatistics().get(StatsType.BUFF).getEffect(Stats.ADD_R_EAU)).append(",").append(player.getStatistics().get(StatsType.BUFF).getEffect(Stats.ADD_R_EAU)).append("|");
            builder.append(player.getStatistics().get(StatsType.BASE).getEffect(Stats.ADD_RP_EAU)).append(",").append(player.getStatistics().get(StatsType.STUFF).getEffect(Stats.ADD_RP_EAU)).append(",").append(0).append(",").append(player.getStatistics().get(StatsType.BUFF).getEffect(Stats.ADD_RP_EAU)).append(",").append(player.getStatistics().get(StatsType.BUFF).getEffect(Stats.ADD_RP_EAU)).append("|");
            builder.append(player.getStatistics().get(StatsType.BASE).getEffect(Stats.ADD_R_PVP_EAU)).append(",").append(player.getStatistics().get(StatsType.STUFF).getEffect(Stats.ADD_R_PVP_EAU)).append(",").append(0).append(",").append(player.getStatistics().get(StatsType.BUFF).getEffect(Stats.ADD_R_PVP_EAU)).append(",").append(player.getStatistics().get(StatsType.BUFF).getEffect(Stats.ADD_R_PVP_EAU)).append("|");
            builder.append(player.getStatistics().get(StatsType.BASE).getEffect(Stats.ADD_RP_PVP_EAU)).append(",").append(player.getStatistics().get(StatsType.STUFF).getEffect(Stats.ADD_RP_PVP_EAU)).append(",").append(0).append(",").append(player.getStatistics().get(StatsType.BUFF).getEffect(Stats.ADD_RP_PVP_EAU)).append(",").append(player.getStatistics().get(StatsType.BUFF).getEffect(Stats.ADD_RP_PVP_EAU)).append("|");
            builder.append(player.getStatistics().get(StatsType.BASE).getEffect(Stats.ADD_R_AIR)).append(",").append(player.getStatistics().get(StatsType.STUFF).getEffect(Stats.ADD_R_AIR)).append(",").append(0).append(",").append(player.getStatistics().get(StatsType.BUFF).getEffect(Stats.ADD_R_AIR)).append(",").append(player.getStatistics().get(StatsType.BUFF).getEffect(Stats.ADD_R_AIR)).append("|");
            builder.append(player.getStatistics().get(StatsType.BASE).getEffect(Stats.ADD_RP_AIR)).append(",").append(player.getStatistics().get(StatsType.STUFF).getEffect(Stats.ADD_RP_AIR)).append(",").append(0).append(",").append(player.getStatistics().get(StatsType.BUFF).getEffect(Stats.ADD_RP_AIR)).append(",").append(player.getStatistics().get(StatsType.BUFF).getEffect(Stats.ADD_RP_AIR)).append("|");
            builder.append(player.getStatistics().get(StatsType.BASE).getEffect(Stats.ADD_R_PVP_AIR)).append(",").append(player.getStatistics().get(StatsType.STUFF).getEffect(Stats.ADD_R_PVP_AIR)).append(",").append(0).append(",").append(player.getStatistics().get(StatsType.BUFF).getEffect(Stats.ADD_R_PVP_AIR)).append(",").append(player.getStatistics().get(StatsType.BUFF).getEffect(Stats.ADD_R_PVP_AIR)).append("|");
            builder.append(player.getStatistics().get(StatsType.BASE).getEffect(Stats.ADD_RP_PVP_AIR)).append(",").append(player.getStatistics().get(StatsType.STUFF).getEffect(Stats.ADD_RP_PVP_AIR)).append(",").append(0).append(",").append(player.getStatistics().get(StatsType.BUFF).getEffect(Stats.ADD_RP_PVP_AIR)).append(",").append(player.getStatistics().get(StatsType.BUFF).getEffect(Stats.ADD_RP_PVP_AIR)).append("|");
            builder.append(player.getStatistics().get(StatsType.BASE).getEffect(Stats.ADD_R_FEU)).append(",").append(player.getStatistics().get(StatsType.STUFF).getEffect(Stats.ADD_R_FEU)).append(",").append(0).append(",").append(player.getStatistics().get(StatsType.BUFF).getEffect(Stats.ADD_R_FEU)).append(",").append(player.getStatistics().get(StatsType.BUFF).getEffect(Stats.ADD_R_FEU)).append("|");
            builder.append(player.getStatistics().get(StatsType.BASE).getEffect(Stats.ADD_RP_FEU)).append(",").append(player.getStatistics().get(StatsType.STUFF).getEffect(Stats.ADD_RP_FEU)).append(",").append(0).append(",").append(player.getStatistics().get(StatsType.BUFF).getEffect(Stats.ADD_RP_FEU)).append(",").append(player.getStatistics().get(StatsType.BUFF).getEffect(Stats.ADD_RP_FEU)).append("|");
            builder.append(player.getStatistics().get(StatsType.BASE).getEffect(Stats.ADD_R_PVP_FEU)).append(",").append(player.getStatistics().get(StatsType.STUFF).getEffect(Stats.ADD_R_PVP_FEU)).append(",").append(0).append(",").append(player.getStatistics().get(StatsType.BUFF).getEffect(Stats.ADD_R_PVP_FEU)).append(",").append(player.getStatistics().get(StatsType.BUFF).getEffect(Stats.ADD_R_PVP_FEU)).append("|");
            builder.append(player.getStatistics().get(StatsType.BASE).getEffect(Stats.ADD_RP_PVP_FEU)).append(",").append(player.getStatistics().get(StatsType.STUFF).getEffect(Stats.ADD_RP_PVP_FEU)).append(",").append(0).append(",").append(player.getStatistics().get(StatsType.BUFF).getEffect(Stats.ADD_RP_PVP_FEU)).append(",").append(player.getStatistics().get(StatsType.BUFF).getEffect(Stats.ADD_RP_PVP_FEU)).append("|");
            return builder.toString();
        });

        packets.put("SL", player -> {
            StringBuilder builder = new StringBuilder();
            builder.append("SL");
            for (Spell spell : player.getSpells().values())
                builder.append(spell.getTemplate()).append("~").append(spell.getLevel()).append("~").append(player.getSpellPlace().get(spell.getTemplate())).append(";");
            return builder.toString();
        });

        this.packets = Collections.unmodifiableMap(packets);
    }
}
