package graviton.game.client.player;

import com.google.inject.Inject;
import com.google.inject.Injector;
import graviton.common.Pair;
import graviton.core.Configuration;
import graviton.core.Main;
import graviton.database.DatabaseManager;
import graviton.game.GameManager;
import graviton.game.alignement.Alignement;
import graviton.game.client.Account;
import graviton.game.client.player.component.ActionManager;
import graviton.game.client.player.component.CommandManager;
import graviton.game.client.player.component.FloodCheck;
import graviton.game.common.Stats;
import graviton.game.creature.Creature;
import graviton.game.creature.mount.Mount;
import graviton.game.enums.Classe;
import graviton.game.enums.ObjectPosition;
import graviton.game.enums.StatsType;
import graviton.game.fight.Fighter;
import graviton.game.guild.Guild;
import graviton.game.maps.Cell;
import graviton.game.maps.Maps;
import graviton.game.maps.Zaap;
import graviton.game.object.Object;
import graviton.game.spells.Spell;
import graviton.game.spells.SpellStats;
import graviton.game.statistics.Statistics;
import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Botan on 19/06/2015.
 */
@Data
public class Player implements Creature, Fighter {
    @Inject
    DatabaseManager data;
    @Inject
    Configuration configuration;
    @Inject
    GameManager gameManager;
    @Inject
    CommandManager commandManager;

    private final int id;
    private final Account account;
    private final Classe classe;

    private String name;
    private int sex;

    private boolean online;

    private Guild guild;
    private Group group;
    private FloodCheck floodCheck;
    private ActionManager actionManager;

    private Alignement alignement;
    private int level;
    private int gfx;
    private int[] colors;
    private long experience;
    private int size;
    private Map<StatsType, Statistics> statistics;
    private long kamas;
    private int capital;
    private int spellPoints;
    private int title;
    private Pair<Integer, Integer> life; //current & max

    private Position position;
    private Map<Integer, Object> objects;
    private List<Zaap> zaaps;
    private Map<Integer, SpellStats> spells;
    private Map<Integer, Character> spellPlace;
    private int inviting;

    private Mount mount;

    public Player(int id, Account account, String name, int sex, int classe, int alignement, int honor, int deshonnor, boolean showWings, int level, int gfx, int[] colors,
                  long experience, int size, Map<Integer, Integer> stats, String objects, long kamas, int capital, String spells, int spellPoints, int map, int cell, Injector injector) {
        injector.injectMembers(this);
        this.id = id;
        this.account = account;
        this.name = name;
        this.sex = sex;
        this.classe = Classe.values()[classe - 1];
        this.alignement = new Alignement(this, alignement, honor, deshonnor, showWings);
        this.level = level;
        this.gfx = gfx;
        this.colors = colors;
        this.experience = experience;
        this.configureStatisctics(stats);
        this.size = size;
        this.kamas = kamas;
        this.capital = capital;
        this.spellPoints = spellPoints;
        final Maps playerMap = gameManager.getMap(map);
        this.position = new Position(playerMap, playerMap.getCell(cell), -1);
        this.objects = new HashMap<>();
        this.zaaps = new ArrayList<>();
        this.online = false;
        this.title = 0;
        int life = ((this.level - 1) * 5 + 50) + getTotalStatistics().getEffect(Stats.ADD_VITA);
        this.life = new Pair<>(life, life);
        this.configureSpells(spells);
        this.zaaps.addAll(gameManager.getZaaps());
        if (objects != null && !objects.isEmpty())
            this.setStuff(objects);
        gameManager.getPlayers().put(id, this);
    }

    public Player(String name, byte sex, byte classeId, int[] colors, Account account,Injector injector) {
        injector.injectMembers(this);
        this.account = account;
        this.id = data.getNextPlayerId();
        this.name = name;
        this.sex = sex;
        this.classe = Classe.values()[classeId - 1];
        this.alignement = new Alignement(this);
        this.level = configuration.getStartlevel();
        this.gfx = classeId * 10 + sex;
        this.colors = colors;
        this.experience = gameManager.getPlayerExperience(level);
        this.size = 100;
        this.configureStatisctics(null);
        this.kamas = configuration.getStartKamas();
        this.capital = (level - 1) * 5;
        this.spellPoints = level - 1;
        final Maps maps = gameManager.getMap(configuration.getStartMap());
        final Cell cell = maps.getCell(configuration.getStartCell());
        this.position = new Position(maps, cell, -1);
        this.objects = new HashMap<>();
        this.zaaps = new ArrayList<>();
        this.online = false;
        this.title = 0;
        int life = ((this.level - 1) * 5 + 50) + getTotalStatistics().getEffect(Stats.ADD_VITA);
        this.life = new Pair<>(life, life);
        this.spells = classe.getStartSpells(gameManager, level);
        this.spellPlace = classe.getStartPlace(gameManager);
        this.zaaps.addAll(gameManager.getZaaps());
        if (data.createPlayer(this))
            gameManager.getPlayers().put(id, this);
    }

    private void configureStatisctics(Map<Integer, Integer> stats) {
        this.statistics = new HashMap<>();
        this.statistics.put(StatsType.BASE, new Statistics(this, stats));
        this.statistics.put(StatsType.BUFF, new Statistics());
        this.statistics.put(StatsType.GIFT, new Statistics());
        this.statistics.put(StatsType.STUFF, new Statistics());
        this.statistics.put(StatsType.MOUNT, new Statistics());
    }

    public void joinGame() {
        if (account.getClient() == null)
            return;
        account.setCurrentPlayer(this);
        this.online = true;
        this.account.setLastConnection();
        if (this.mount != null) {
            send("Re+" + this.mount.getPacket());
        }
        send("Rx" + (this.mount == null ? 0 : this.mount.getExperience()));
        send(getPacket("ASK"));
        //TODO : Bonus pano & job
        send("ZS" + this.alignement.getType().getId());
        send("cC+*%!p$?^#i^@^:");
        //TODO : Gs guild packet
        send("al|"); //TODO : Sub zone
        send(getPacket("SL")); //TODO: spells
        send("eL7667711|0");
        send("AR6bk");
        send("Ow0|1000"); //TODO : System for pods
        send("FO+"); //TODO : seeFriends
        send("ILS2000");
        sendText(configuration.getDefaultMessage(), configuration.getDefaultColor());
        this.account.setOnline();
        //TODO : Show last IP etc.. (for account)
    }

    public void createGame() {
        send("GCK|1|" + this.name);
        send(getPacket("As"));
        this.position.getMap().addCreature(this);
    }

    public String getPacket(String packet) {
        StringBuilder builder = new StringBuilder();
        switch (packet) {
            case "ALK":
                builder.append("|");
                builder.append(this.id).append(";");
                builder.append(this.getName()).append(";");
                builder.append(this.level).append(";");
                builder.append(this.gfx).append(";");
                builder.append((this.colors[0] != -1 ? Integer.toHexString(this.colors[0]) : "-1")).append(";");
                builder.append((this.colors[1] != -1 ? Integer.toHexString(this.colors[1]) : "-1")).append(";");
                builder.append((this.colors[2] != -1 ? Integer.toHexString(this.colors[2]) : "-1")).append(";");
                builder.append(getPacket("GMS")).append(";");
                builder.append(0).append(";");
                builder.append("1;");
                builder.append(";");
                builder.append(";");
                return builder.toString();
            case "GMS": //Stuff
                ObjectPosition[] positions = {ObjectPosition.ARME, ObjectPosition.COIFFE, ObjectPosition.CAPE, ObjectPosition.FAMILIER, ObjectPosition.BOUCLIER};
                for (ObjectPosition position : positions) {
                    if (getObjectByPosition(position) != null)
                        builder.append(Integer.toHexString(getObjectByPosition(position).getTemplate().getId()));
                    builder.append(",");
                }
                return builder.toString().substring(0, builder.length() - 1);
            case "WC":
                builder.append("WC").append(getMap().getId());
                this.zaaps.forEach(zaap -> builder.append("|").append(gameManager.getMap(zaap.getMap().getId()).getId()).append(";").append(zaap.getCost(getMap())));
                return builder.toString();
            case "ASK":
                builder.append("ASK|");
                builder.append(id).append("|").append(name).append("|");
                builder.append(level).append("|");
                builder.append(classe.getId()).append("|");
                builder.append(sex).append("|");
                builder.append(gfx).append("|");
                builder.append((colors[0]) == -1 ? "-1" : Integer.toHexString(colors[0])).append("|");
                builder.append((colors[1]) == -1 ? "-1" : Integer.toHexString(colors[1])).append("|");
                builder.append((colors[2]) == -1 ? "-1" : Integer.toHexString(colors[2])).append("|");
                builder.append(getPacket("ASKI"));
                return builder.toString();
            case "ASKI": //Ask to item
                if (this.objects.isEmpty()) return "";
                this.objects.values().forEach(object -> builder.append(object.parseItem()));
                return builder.toString();
            case "As":
                builder.append("As");
                builder.append(experience).append(",").append(gameManager.getPlayerExperience(level)).append(",").append(gameManager.getPlayerExperience(level + 1)).append("|");
                builder.append(kamas).append("|").append(capital).append("|").append(spellPoints).append("|");
                builder.append(alignement.getType().getId()).append("~");
                builder.append(alignement.getType().getId()).append(",").append(alignement.getGrade()).append(",").append(alignement.getGrade()).append(",").append(alignement.getHonor()).append(",").append(alignement.getDeshonnor()).append(",").append(alignement.isShowWings() ? "1" : "0").append("|");
                builder.append(life.getFirst()).append(",").append(life.getSecond()).append("|");
                builder.append(10000).append(",10000|");
                builder.append(getInitiative()).append("|");
                builder.append(statistics.get(StatsType.BASE).getEffect(Stats.ADD_PROS) + statistics.get(StatsType.STUFF).getEffect(Stats.ADD_PROS) + ((int) Math.ceil(statistics.get(StatsType.BASE).getEffect(Stats.ADD_CHAN) / 10)) + statistics.get(StatsType.BUFF).getEffect(Stats.ADD_PROS)).append("|");
                builder.append(statistics.get(StatsType.BASE).getEffect(Stats.ADD_PA)).append(",").append(statistics.get(StatsType.STUFF).getEffect(Stats.ADD_PA)).append(",").append(statistics.get(StatsType.GIFT).getEffect(Stats.ADD_PA)).append(",").append(statistics.get(StatsType.BUFF).getEffect(Stats.ADD_PA)).append(",").append(getTotalStatistics().getEffect(Stats.ADD_PA)).append("|");
                builder.append(statistics.get(StatsType.BASE).getEffect(Stats.ADD_PM)).append(",").append(statistics.get(StatsType.STUFF).getEffect(Stats.ADD_PM)).append(",").append(statistics.get(StatsType.GIFT).getEffect(Stats.ADD_PM)).append(",").append(statistics.get(StatsType.BUFF).getEffect(Stats.ADD_PM)).append(",").append(getTotalStatistics().getEffect(Stats.ADD_PM)).append("|");
                builder.append(statistics.get(StatsType.BASE).getEffect(Stats.ADD_FORC)).append(",").append(statistics.get(StatsType.STUFF).getEffect(Stats.ADD_FORC)).append(",").append(statistics.get(StatsType.GIFT).getEffect(Stats.ADD_FORC)).append(",").append(statistics.get(StatsType.BUFF).getEffect(Stats.ADD_FORC)).append("|");
                builder.append(statistics.get(StatsType.BASE).getEffect(Stats.ADD_VITA)).append(",").append(statistics.get(StatsType.STUFF).getEffect(Stats.ADD_VITA)).append(",").append(statistics.get(StatsType.GIFT).getEffect(Stats.ADD_VITA)).append(",").append(statistics.get(StatsType.BUFF).getEffect(Stats.ADD_VITA)).append("|");
                builder.append(statistics.get(StatsType.BASE).getEffect(Stats.ADD_SAGE)).append(",").append(statistics.get(StatsType.STUFF).getEffect(Stats.ADD_SAGE)).append(",").append(statistics.get(StatsType.GIFT).getEffect(Stats.ADD_SAGE)).append(",").append(statistics.get(StatsType.BUFF).getEffect(Stats.ADD_SAGE)).append("|");
                builder.append(statistics.get(StatsType.BASE).getEffect(Stats.ADD_CHAN)).append(",").append(statistics.get(StatsType.STUFF).getEffect(Stats.ADD_CHAN)).append(",").append(statistics.get(StatsType.GIFT).getEffect(Stats.ADD_CHAN)).append(",").append(statistics.get(StatsType.BUFF).getEffect(Stats.ADD_CHAN)).append("|");
                builder.append(statistics.get(StatsType.BASE).getEffect(Stats.ADD_AGIL)).append(",").append(statistics.get(StatsType.STUFF).getEffect(Stats.ADD_AGIL)).append(",").append(statistics.get(StatsType.GIFT).getEffect(Stats.ADD_AGIL)).append(",").append(statistics.get(StatsType.BUFF).getEffect(Stats.ADD_AGIL)).append("|");
                builder.append(statistics.get(StatsType.BASE).getEffect(Stats.ADD_INTE)).append(",").append(statistics.get(StatsType.STUFF).getEffect(Stats.ADD_INTE)).append(",").append(statistics.get(StatsType.GIFT).getEffect(Stats.ADD_INTE)).append(",").append(statistics.get(StatsType.BUFF).getEffect(Stats.ADD_INTE)).append("|");
                builder.append(statistics.get(StatsType.BASE).getEffect(Stats.ADD_PO)).append(",").append(statistics.get(StatsType.STUFF).getEffect(Stats.ADD_PO)).append(",").append(statistics.get(StatsType.GIFT).getEffect(Stats.ADD_PO)).append(",").append(statistics.get(StatsType.BUFF).getEffect(Stats.ADD_PO)).append("|");
                builder.append(statistics.get(StatsType.BASE).getEffect(Stats.CREATURE)).append(",").append(statistics.get(StatsType.STUFF).getEffect(Stats.CREATURE)).append(",").append(statistics.get(StatsType.GIFT).getEffect(Stats.CREATURE)).append(",").append(statistics.get(StatsType.BUFF).getEffect(Stats.CREATURE)).append("|");
                builder.append(statistics.get(StatsType.BASE).getEffect(Stats.ADD_DOMA)).append(",").append(statistics.get(StatsType.STUFF).getEffect(Stats.ADD_DOMA)).append(",").append(statistics.get(StatsType.GIFT).getEffect(Stats.ADD_DOMA)).append(",").append(statistics.get(StatsType.BUFF).getEffect(Stats.ADD_DOMA)).append("|");
                builder.append(statistics.get(StatsType.BASE).getEffect(Stats.ADD_PDOM)).append(",").append(statistics.get(StatsType.STUFF).getEffect(Stats.ADD_PDOM)).append(",").append(statistics.get(StatsType.GIFT).getEffect(Stats.ADD_PDOM)).append(",").append(statistics.get(StatsType.BUFF).getEffect(Stats.ADD_PDOM)).append("|");
                builder.append("0,0,0,0|");
                builder.append(statistics.get(StatsType.BASE).getEffect(Stats.ADD_PERDOM)).append(",").append(statistics.get(StatsType.STUFF).getEffect(Stats.ADD_PERDOM)).append(",").append(statistics.get(StatsType.GIFT).getEffect(Stats.ADD_PERDOM)).append(",").append(statistics.get(StatsType.BUFF).getEffect(Stats.ADD_PERDOM)).append("|");
                builder.append(statistics.get(StatsType.BASE).getEffect(Stats.ADD_SOIN)).append(",").append(statistics.get(StatsType.STUFF).getEffect(Stats.ADD_SOIN)).append(",").append(statistics.get(StatsType.GIFT).getEffect(Stats.ADD_SOIN)).append(",").append(statistics.get(StatsType.BUFF).getEffect(Stats.ADD_SOIN)).append("|");
                builder.append(statistics.get(StatsType.BASE).getEffect(Stats.TRAPDOM)).append(",").append(statistics.get(StatsType.STUFF).getEffect(Stats.TRAPDOM)).append(",").append(statistics.get(StatsType.GIFT).getEffect(Stats.TRAPDOM)).append(",").append(statistics.get(StatsType.BUFF).getEffect(Stats.TRAPDOM)).append("|");
                builder.append(statistics.get(StatsType.BASE).getEffect(Stats.TRAPPER)).append(",").append(statistics.get(StatsType.STUFF).getEffect(Stats.TRAPPER)).append(",").append(statistics.get(StatsType.GIFT).getEffect(Stats.TRAPPER)).append(",").append(statistics.get(StatsType.BUFF).getEffect(Stats.TRAPPER)).append("|");
                builder.append(statistics.get(StatsType.BASE).getEffect(Stats.RETDOM)).append(",").append(statistics.get(StatsType.STUFF).getEffect(Stats.RETDOM)).append(",").append(statistics.get(StatsType.GIFT).getEffect(Stats.RETDOM)).append(",").append(statistics.get(StatsType.BUFF).getEffect(Stats.RETDOM)).append("|");
                builder.append(statistics.get(StatsType.BASE).getEffect(Stats.ADD_CC)).append(",").append(statistics.get(StatsType.STUFF).getEffect(Stats.ADD_CC)).append(",").append(statistics.get(StatsType.GIFT).getEffect(Stats.ADD_CC)).append(",").append(statistics.get(StatsType.BUFF).getEffect(Stats.ADD_CC)).append("|");
                builder.append(statistics.get(StatsType.BASE).getEffect(Stats.ADD_EC)).append(",").append(statistics.get(StatsType.STUFF).getEffect(Stats.ADD_EC)).append(",").append(statistics.get(StatsType.GIFT).getEffect(Stats.ADD_EC)).append(",").append(statistics.get(StatsType.BUFF).getEffect(Stats.ADD_EC)).append("|");
                builder.append(statistics.get(StatsType.BASE).getEffect(Stats.ADD_AFLEE)).append(",").append(statistics.get(StatsType.STUFF).getEffect(Stats.ADD_AFLEE)).append(",").append(0).append(",").append(statistics.get(StatsType.BUFF).getEffect(Stats.ADD_AFLEE)).append(",").append(statistics.get(StatsType.BUFF).getEffect(Stats.ADD_AFLEE)).append("|");
                builder.append(statistics.get(StatsType.BASE).getEffect(Stats.ADD_MFLEE)).append(",").append(statistics.get(StatsType.STUFF).getEffect(Stats.ADD_MFLEE)).append(",").append(0).append(",").append(statistics.get(StatsType.BUFF).getEffect(Stats.ADD_MFLEE)).append(",").append(statistics.get(StatsType.BUFF).getEffect(Stats.ADD_MFLEE)).append("|");
                builder.append(statistics.get(StatsType.BASE).getEffect(Stats.ADD_R_NEU)).append(",").append(statistics.get(StatsType.STUFF).getEffect(Stats.ADD_R_NEU)).append(",").append(0).append(",").append(statistics.get(StatsType.BUFF).getEffect(Stats.ADD_R_NEU)).append(",").append(statistics.get(StatsType.BUFF).getEffect(Stats.ADD_R_NEU)).append("|");
                builder.append(statistics.get(StatsType.BASE).getEffect(Stats.ADD_RP_NEU)).append(",").append(statistics.get(StatsType.STUFF).getEffect(Stats.ADD_RP_NEU)).append(",").append(0).append(",").append(statistics.get(StatsType.BUFF).getEffect(Stats.ADD_RP_NEU)).append(",").append(statistics.get(StatsType.BUFF).getEffect(Stats.ADD_RP_NEU)).append("|");
                builder.append(statistics.get(StatsType.BASE).getEffect(Stats.ADD_R_PVP_NEU)).append(",").append(statistics.get(StatsType.STUFF).getEffect(Stats.ADD_R_PVP_NEU)).append(",").append(0).append(",").append(statistics.get(StatsType.BUFF).getEffect(Stats.ADD_R_PVP_NEU)).append(",").append(statistics.get(StatsType.BUFF).getEffect(Stats.ADD_R_PVP_NEU)).append("|");
                builder.append(statistics.get(StatsType.BASE).getEffect(Stats.ADD_RP_PVP_NEU)).append(",").append(statistics.get(StatsType.STUFF).getEffect(Stats.ADD_RP_PVP_NEU)).append(",").append(0).append(",").append(statistics.get(StatsType.BUFF).getEffect(Stats.ADD_RP_PVP_NEU)).append(",").append(statistics.get(StatsType.BUFF).getEffect(Stats.ADD_RP_PVP_NEU)).append("|");
                builder.append(statistics.get(StatsType.BASE).getEffect(Stats.ADD_R_TER)).append(",").append(statistics.get(StatsType.STUFF).getEffect(Stats.ADD_R_TER)).append(",").append(0).append(",").append(statistics.get(StatsType.BUFF).getEffect(Stats.ADD_R_TER)).append(",").append(statistics.get(StatsType.BUFF).getEffect(Stats.ADD_R_TER)).append("|");
                builder.append(statistics.get(StatsType.BASE).getEffect(Stats.ADD_RP_TER)).append(",").append(statistics.get(StatsType.STUFF).getEffect(Stats.ADD_RP_TER)).append(",").append(0).append(",").append(statistics.get(StatsType.BUFF).getEffect(Stats.ADD_RP_TER)).append(",").append(statistics.get(StatsType.BUFF).getEffect(Stats.ADD_RP_TER)).append("|");
                builder.append(statistics.get(StatsType.BASE).getEffect(Stats.ADD_R_PVP_TER)).append(",").append(statistics.get(StatsType.STUFF).getEffect(Stats.ADD_R_PVP_TER)).append(",").append(0).append(",").append(statistics.get(StatsType.BUFF).getEffect(Stats.ADD_R_PVP_TER)).append(",").append(statistics.get(StatsType.BUFF).getEffect(Stats.ADD_R_PVP_TER)).append("|");
                builder.append(statistics.get(StatsType.BASE).getEffect(Stats.ADD_RP_PVP_TER)).append(",").append(statistics.get(StatsType.STUFF).getEffect(Stats.ADD_RP_PVP_TER)).append(",").append(0).append(",").append(statistics.get(StatsType.BUFF).getEffect(Stats.ADD_RP_PVP_TER)).append(",").append(statistics.get(StatsType.BUFF).getEffect(Stats.ADD_RP_PVP_TER)).append("|");
                builder.append(statistics.get(StatsType.BASE).getEffect(Stats.ADD_R_EAU)).append(",").append(statistics.get(StatsType.STUFF).getEffect(Stats.ADD_R_EAU)).append(",").append(0).append(",").append(statistics.get(StatsType.BUFF).getEffect(Stats.ADD_R_EAU)).append(",").append(statistics.get(StatsType.BUFF).getEffect(Stats.ADD_R_EAU)).append("|");
                builder.append(statistics.get(StatsType.BASE).getEffect(Stats.ADD_RP_EAU)).append(",").append(statistics.get(StatsType.STUFF).getEffect(Stats.ADD_RP_EAU)).append(",").append(0).append(",").append(statistics.get(StatsType.BUFF).getEffect(Stats.ADD_RP_EAU)).append(",").append(statistics.get(StatsType.BUFF).getEffect(Stats.ADD_RP_EAU)).append("|");
                builder.append(statistics.get(StatsType.BASE).getEffect(Stats.ADD_R_PVP_EAU)).append(",").append(statistics.get(StatsType.STUFF).getEffect(Stats.ADD_R_PVP_EAU)).append(",").append(0).append(",").append(statistics.get(StatsType.BUFF).getEffect(Stats.ADD_R_PVP_EAU)).append(",").append(statistics.get(StatsType.BUFF).getEffect(Stats.ADD_R_PVP_EAU)).append("|");
                builder.append(statistics.get(StatsType.BASE).getEffect(Stats.ADD_RP_PVP_EAU)).append(",").append(statistics.get(StatsType.STUFF).getEffect(Stats.ADD_RP_PVP_EAU)).append(",").append(0).append(",").append(statistics.get(StatsType.BUFF).getEffect(Stats.ADD_RP_PVP_EAU)).append(",").append(statistics.get(StatsType.BUFF).getEffect(Stats.ADD_RP_PVP_EAU)).append("|");
                builder.append(statistics.get(StatsType.BASE).getEffect(Stats.ADD_R_AIR)).append(",").append(statistics.get(StatsType.STUFF).getEffect(Stats.ADD_R_AIR)).append(",").append(0).append(",").append(statistics.get(StatsType.BUFF).getEffect(Stats.ADD_R_AIR)).append(",").append(statistics.get(StatsType.BUFF).getEffect(Stats.ADD_R_AIR)).append("|");
                builder.append(statistics.get(StatsType.BASE).getEffect(Stats.ADD_RP_AIR)).append(",").append(statistics.get(StatsType.STUFF).getEffect(Stats.ADD_RP_AIR)).append(",").append(0).append(",").append(statistics.get(StatsType.BUFF).getEffect(Stats.ADD_RP_AIR)).append(",").append(statistics.get(StatsType.BUFF).getEffect(Stats.ADD_RP_AIR)).append("|");
                builder.append(statistics.get(StatsType.BASE).getEffect(Stats.ADD_R_PVP_AIR)).append(",").append(statistics.get(StatsType.STUFF).getEffect(Stats.ADD_R_PVP_AIR)).append(",").append(0).append(",").append(statistics.get(StatsType.BUFF).getEffect(Stats.ADD_R_PVP_AIR)).append(",").append(statistics.get(StatsType.BUFF).getEffect(Stats.ADD_R_PVP_AIR)).append("|");
                builder.append(statistics.get(StatsType.BASE).getEffect(Stats.ADD_RP_PVP_AIR)).append(",").append(statistics.get(StatsType.STUFF).getEffect(Stats.ADD_RP_PVP_AIR)).append(",").append(0).append(",").append(statistics.get(StatsType.BUFF).getEffect(Stats.ADD_RP_PVP_AIR)).append(",").append(statistics.get(StatsType.BUFF).getEffect(Stats.ADD_RP_PVP_AIR)).append("|");
                builder.append(statistics.get(StatsType.BASE).getEffect(Stats.ADD_R_FEU)).append(",").append(statistics.get(StatsType.STUFF).getEffect(Stats.ADD_R_FEU)).append(",").append(0).append(",").append(statistics.get(StatsType.BUFF).getEffect(Stats.ADD_R_FEU)).append(",").append(statistics.get(StatsType.BUFF).getEffect(Stats.ADD_R_FEU)).append("|");
                builder.append(statistics.get(StatsType.BASE).getEffect(Stats.ADD_RP_FEU)).append(",").append(statistics.get(StatsType.STUFF).getEffect(Stats.ADD_RP_FEU)).append(",").append(0).append(",").append(statistics.get(StatsType.BUFF).getEffect(Stats.ADD_RP_FEU)).append(",").append(statistics.get(StatsType.BUFF).getEffect(Stats.ADD_RP_FEU)).append("|");
                builder.append(statistics.get(StatsType.BASE).getEffect(Stats.ADD_R_PVP_FEU)).append(",").append(statistics.get(StatsType.STUFF).getEffect(Stats.ADD_R_PVP_FEU)).append(",").append(0).append(",").append(statistics.get(StatsType.BUFF).getEffect(Stats.ADD_R_PVP_FEU)).append(",").append(statistics.get(StatsType.BUFF).getEffect(Stats.ADD_R_PVP_FEU)).append("|");
                builder.append(statistics.get(StatsType.BASE).getEffect(Stats.ADD_RP_PVP_FEU)).append(",").append(statistics.get(StatsType.STUFF).getEffect(Stats.ADD_RP_PVP_FEU)).append(",").append(0).append(",").append(statistics.get(StatsType.BUFF).getEffect(Stats.ADD_RP_PVP_FEU)).append(",").append(statistics.get(StatsType.BUFF).getEffect(Stats.ADD_RP_PVP_FEU)).append("|");
                return builder.toString();
            case "SL":
                builder.append("SL");
                for (SpellStats spellStats : this.spells.values())
                    builder.append(spellStats.getSpell()).append("~").append(spellStats.getLevel()).append("~").append(this.spellPlace.get(spellStats.getSpell())).append(";");

                return builder.toString();
        }
        return null;
    }

    @Override
    public String getGm() {
        StringBuilder builder = new StringBuilder();
        builder.append(this.getCell().getId());//Id de la cellule
        builder.append(";").append(this.getOrientation()).append(";");
        builder.append("0").append(";");
        builder.append(this.id).append(";");//id de la personne
        builder.append(this.name).append(";");//nom de la personne
        builder.append(this.getClasse().getId());//Classe
        builder.append((this.getTitle() > 0 ? ("," + this.getTitle()) : ""));
        builder.append(";").append(this.gfx).append("^");
        builder.append(this.getSize());//gfxID^size
        builder.append(";");
        builder.append(this.getSex()).append(";");
        builder.append(this.getAlignement().getType().getId()).append(",");//1,0,0,4055064
        builder.append("0").append(",");
        builder.append((this.alignement.isShowWings() ? this.alignement.getGrade() : "0")).append(",");
        builder.append(this.level + this.id);
        if (this.alignement.isShowWings() && this.alignement.getDeshonnor() > 0)
            builder.append(",").append(1).append(';');
        else
            builder.append(";");
        builder.append(this.getColor(1) == -1 ? "-1" : Integer.toHexString(this.getColor(1))).append(";");
        builder.append(this.getColor(2) == -1 ? "-1" : Integer.toHexString(this.getColor(2))).append(";");
        builder.append(this.getColor(3) == -1 ? "-1" : Integer.toHexString(this.getColor(3))).append(";");
        builder.append(this.getPacket("GMS")).append(";");
        builder.append(this.level > 99 ? (this.level > 199 ? (2) : (1)) : (0)).append(";");
        builder.append(";");
        builder.append(";");
        builder.append(";;");
        builder.append(0).append(";");//Rebuilderiction
        builder.append(";");
        builder.append(";");
        return builder.toString();
    }

    private void configureSpells(String data) {
        this.spells = new HashMap<>();
        this.spellPlace = new HashMap<>();
        String[] spells = data.split(",");
        for (String element : spells) {
            try {
                int id = Integer.parseInt(element.split(";")[0]);
                int level = Integer.parseInt(element.split(";")[1]);
                char place = element.split(";")[2].charAt(0);
                this.spells.put(id, gameManager.getSpells().get(id).getStats(level));
                this.spellPlace.put(id, place);
            } catch (NumberFormatException e1) {
            }
        }
    }

    public String parseSpells() {
        StringBuilder builder = new StringBuilder();
        for (int key : this.spells.keySet()) {
            SpellStats spellStats = this.spells.get(key);
            builder.append(spellStats.getSpell()).append(";").append(spellStats.getLevel()).append(";");
            if (this.spellPlace.get(key) != null)
                builder.append(this.spellPlace.get(key));
            else
                builder.append("_");
            builder.append(",");
        }
        return builder.toString();
    }


    public void learnSpell(int spell, int level) {

    }

    private void setStuff(String objects) {
        if (objects.charAt(objects.length() - 1) == ',')
            objects = objects.substring(0, objects.length() - 1);
        for (String data : objects.split(",")) {
            Object object = gameManager.getObject(Integer.parseInt(data));
            if (object == null) continue;
            this.objects.put(object.getId(), object);
        }
    }

    public void addObject(Object newObject) {
        for (Object object : this.objects.values())
            if (object.getTemplate().getId() == newObject.getTemplate().getId())
                if (object.getStatistics().isSameStatistics(newObject.getStatistics())) {
                    object.setQuantity(object.getQuantity() + newObject.getQuantity());
                    data.updateObject(object);
                    send("OQ" + object.getId() + "|" + object.getQuantity());
                    return;
                }
        this.objects.put(newObject.getId(), newObject);
        gameManager.getObjects().put(newObject.getId(), newObject);
        send("OAKO" + newObject.parseItem());
        data.createObject(newObject);
        save();
    }

    public String parseItem() {
        StringBuilder builder = new StringBuilder();
        objects.values().forEach(object -> builder.append(object.getId()).append(","));
        return builder.toString();
    }

    public boolean boostSpell(int id) {
        if (!spells.containsKey(id))
            return false;
        Spell spell = gameManager.getSpells().get(id);
        int oldLevel = spells.get(id).getLevel();
        if (spellPoints < oldLevel || oldLevel == 6 || spell.getStats(oldLevel + 1).getRequiredLevel() > this.level)
            return false;

        spellPoints -= oldLevel;
        spells.put(id, gameManager.getSpells().get(id).getStats(oldLevel + 1));
        send("SUK" + id + "~" + (oldLevel + 1));
        send(getPacket("As"));
        save();
        return true;
    }

    public void moveSpell(int spell, char place) {
        for (int key : this.spells.keySet())
            if (this.spellPlace.get(key) != null)
                if (this.spellPlace.get(key).equals(place))
                    this.spellPlace.remove(key);
        spellPlace.put(spell, place);
        save();
    }

    public void boostStatistics(int id) {
        int cost = 0, value;
        switch (id) {
            case 10: //Force
                value = this.getStatistics().get(StatsType.BASE).getEffect(Stats.ADD_FORC);
                cost = classe == Classe.SACRIEUR ? 3 : classe.getCost(id, value, this.classe);
                if (cost <= capital)
                    this.getStatistics().get(StatsType.BASE).addEffect(Stats.ADD_FORC, 1);
                break;
            case 11: //Vitalite
                cost = 1;
                if (cost <= capital)
                    this.getStatistics().get(StatsType.BASE).addEffect(Stats.ADD_VITA, classe == Classe.SACRIEUR ? 2 : 1);
                break;
            case 12: //Sagesse
                cost = 3;
                if (cost <= capital)
                    this.getStatistics().get(StatsType.BASE).addEffect(Stats.ADD_SAGE, 1);
                break;
            case 13: //Chance
                value = this.getStatistics().get(StatsType.BASE).getEffect(Stats.ADD_CHAN);
                cost = classe == Classe.SACRIEUR ? 3 : classe.getCost(id, value, this.classe);
                if (cost <= capital)
                    this.getStatistics().get(StatsType.BASE).addEffect(Stats.ADD_CHAN, 1);
                break;
            case 14: // Agilite
                value = this.getStatistics().get(StatsType.BASE).getEffect(Stats.ADD_AGIL);
                cost = classe == Classe.SACRIEUR ? 3 : classe.getCost(id, value, this.classe);
                if (cost <= capital)
                    this.getStatistics().get(StatsType.BASE).addEffect(Stats.ADD_AGIL, 1);
                break;
            case 15: //Intelligence
                value = this.getStatistics().get(StatsType.BASE).getEffect(Stats.ADD_INTE);
                cost = classe == Classe.SACRIEUR ? 3 : classe.getCost(id, value, this.classe);
                if (cost <= capital)
                    this.getStatistics().get(StatsType.BASE).addEffect(Stats.ADD_INTE, 1);
                break;
        }
        capital -= cost;
        send(getPacket("As"));
        save();
    }

    public Statistics getTotalStatistics() {
        Statistics finalStats = new Statistics();
        List<Statistics> stats = new ArrayList<Statistics>() {{
            add(statistics.get(StatsType.BASE));
            add(statistics.get(StatsType.STUFF));
            add(statistics.get(StatsType.GIFT));
            if (mount != null)
                add(statistics.get(StatsType.MOUNT));
        }};
        return finalStats.cumulStatistics(stats);
    }

    public int getInitiative() {
        int maxLife = this.life.getSecond() - 50;
        int life = this.life.getFirst() - 50;
        int factor = (classe == Classe.SACRIEUR ? 8 : 4);
        double coef = maxLife / factor;
        coef += statistics.get(StatsType.STUFF).getEffect(Stats.ADD_INIT);
        coef += getTotalStatistics().getEffect(Stats.ADD_AGIL);
        coef += getTotalStatistics().getEffect(Stats.ADD_CHAN);
        coef += getTotalStatistics().getEffect(Stats.ADD_INTE);
        coef += getTotalStatistics().getEffect(Stats.ADD_FORC);
        int init = 1;
        if (maxLife != 0)
            init = (int) (coef * ((double) life / (double) maxLife));
        if (init < 0)
            init = 0;
        return init;
    }

    public int getColor(int id) {
        return colors[id - 1];
    }

    public Object getObjectByPosition(ObjectPosition position) {
        if (position == ObjectPosition.NO_EQUIPED)
            return null;
        for (Object object : this.objects.values())
            if (object.getPosition() == position)
                return object;
        return null;
    }

    public void speak(String message, String canal) {
        if (!account.canSpeak()) return;
        if (this.floodCheck == null) this.floodCheck = new FloodCheck(this);
        if (message.charAt(1) == '.') {
            commandManager.launchCommand(this, message.substring(2).split(" "));
            return;
        }
        switch (canal) {
            case "*": /** Canal general **/
                if (floodCheck.autorize(canal))
                    getMap().send("cMK|" + id + "|" + name + message);
                break;
            case "#": /** Canal equipe **/
                break;
            case "$": /** Canal groupe **/
                if (group != null)
                    group.send("cMK$|" + id + "|" + name + message);
                break;
            case "%": /** Canal guilde **/
                if (guild != null)
                    guild.send("cMK%|" + id + "|" + name + message);
                break;
            case "!": /** Canal alignement **/
                break;
            case "?": /** Canal recrutement **/
                if (floodCheck.autorize(canal))
                    gameManager.send("cMK?|" + id + "|" + name + message);
                break;
            case ":": /** Canal commerce **/
                if (floodCheck.autorize(canal))
                    gameManager.send("cMK:|" + id + "|" + name + message);
                break;
            case "@": /** Canal admin **/
                if (account.getRank().id != 0)
                    gameManager.sendToAdmins("cMK@|" + id + "|" + name + message);
                break;
        }
    }

    public void openZaap() {
        send(getPacket("WC"));
    }

    public void useZaap(int id) {
        Maps maps = gameManager.getMap(id);
        int cell = -1;
        int cost = 0;
        for (Zaap zaap : gameManager.getZaaps())
            if (zaap.getMap() == maps) {
                cell = zaap.getCell();
                cost = zaap.getCost(getMap());
            }

        send("WV");
        if (this.kamas < cost) {
            send("Im01;" + "Il te faut " + (cost - kamas) + " kamas en plus pour pouvoir utiliser ce zaap.");
            return;
        }
        this.kamas -= cost;
        send("Im046;" + cost);
        send(getPacket("As"));
        if (cell != -1)
            changePosition(maps.getCell(cell));
    }

    public void togglePvp(char c) {
        int cost = this.alignement.getHonor() * 5 / 100;
        switch (c) {
            case '*':
                send("GIP" + cost);
                return;
            case '+':
                alignement.setShowWings(true);
                break;
            case '-':
                this.getAlignement().removeHonor(cost);
                alignement.setShowWings(false);
                break;
        }
        send(getPacket("As"));
        save();
        refresh();
    }

    public void refresh() {
        getMap().refreshCreature(this);
    }

    public void createAction(int id, String arguments) {
        if (this.actionManager == null)
            this.actionManager = new ActionManager(this);
        this.actionManager.createAction(id, arguments);
    }

    public void sendText(String... arguments) {
        String message = arguments[0];
        String color;
        try {
            color = arguments[1];
        } catch (ArrayIndexOutOfBoundsException e) {
            color = configuration.getDefaultColor();
        }
        send("cs<font color='#" + color + "'>" + message + "</font>");
    }

    public final void send(String packet) {
        if (packet.isEmpty()) return;
        this.account.getClient().send(packet);
    }

    public void changeOrientation(int orientation, boolean send) {
        if (send)
            getMap().send("eD" + id + "|" + orientation);
        this.position.setOrientation(orientation);
    }

    public void changePosition(Cell cell) {
        if (this.getMap() != cell.getMap()) {
            this.getMap().removeCreature(this);
            this.position.setCell(cell);
            cell.getMap().addCreature(this);
            return;
        }
        getMap().changeCell(this, cell);
    }

    public Maps getMap() {
        return this.position.getMap();
    }

    public Cell getCell() {
        return this.position.getCell();
    }

    public int getOrientation() {
        return this.position.getOrientation();
    }

    public void delete() {
        account.getPlayers().remove(this);
        gameManager.getPlayers().remove(this.getId());
        data.deletePlayer(this);
        send(account.getPlayersPacket());
    }

    public void save() {
        data.updatePlayer(this);
    }
}
