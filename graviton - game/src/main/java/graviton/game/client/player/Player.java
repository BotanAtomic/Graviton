package graviton.game.client.player;

import graviton.common.Pair;
import graviton.common.Stats;
import graviton.core.Configuration;
import graviton.core.Main;
import graviton.database.DatabaseManager;
import graviton.database.data.PlayerData;
import graviton.enums.*;
import graviton.game.alignement.Alignement;
import graviton.game.client.Account;
import graviton.game.creature.mount.Mount;
import graviton.game.fight.Fight;
import graviton.game.GameManager;
import graviton.game.maps.Cell;
import graviton.game.maps.Maps;
import graviton.game.maps.Zaap;
import graviton.game.object.Object;
import graviton.game.spells.SpellStats;
import graviton.game.statistics.Statistics;
import lombok.Data;

import java.util.*;

/**
 * Created by Botan on 19/06/2015.
 */
@Data
public class Player {
    private final PlayerData data = (PlayerData) Main.getInstance(DatabaseManager.class).getData().get(DataType.PLAYER);
    private final Configuration configuration = Main.getInstance(Configuration.class);
    private final GameManager gameManager = Main.getInstance(GameManager.class);

    private final int id;
    private final Classe classe;

    private Account account;
    private boolean online;

    private String name;
    private int sex;

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
    private Map<Integer, Character> spellsPlace;

    private Fight fight;
    private Mount mount;

    private boolean merchant;

    public Player(int id, int account, String name, int sex, int classe, int alignement, int honor, int deshonnor,
                  int level, int gfx, int[] colors, long experience, int size, Map<Integer, Integer> stats,
                  long kamas, int capital, int spellPoints, int map, int cell) {
        this.account = gameManager.getAccounts().get(account);
        this.id = id;
        this.name = name;
        this.sex = sex;
        this.classe = Classe.values()[classe - 1];
        this.alignement = new Alignement(alignement, honor, deshonnor);
        this.level = level;
        this.gfx = gfx;
        this.colors = colors;
        this.experience = experience;
        this.initializeStatisctics(stats);
        this.size = size;
        this.kamas = kamas;
        this.capital = capital;
        this.spellPoints = spellPoints;
        final Maps playerMap = gameManager.getMap(map);
        this.position = new Position(playerMap, playerMap.getCell(cell), -1);
        this.objects = new HashMap<>();
        this.zaaps = new ArrayList<>();
        this.merchant = false;
        this.online = false;
        this.title = 0;
        int life = ((this.level - 1) * 5 + 50) + getTotalStatistics().getEffect(Stats.ADD_VITA);
        this.life = new Pair<>(life, life);
        this.spells = new HashMap<>();
        this.spellsPlace = new HashMap<>();
        gameManager.getPlayers().put(id, this);
    }

    /**
     * Constructor for create player
     **/
    public Player(String name, byte sex, byte classeId, int[] colors, Account account) {
        this.account = account;
        this.id = data.getNextId();
        this.name = name;
        this.sex = sex;
        this.classe = Classe.values()[classeId - 1];
        this.alignement = new Alignement();
        this.level = configuration.getStartlevel();
        this.gfx = classeId * 10 + sex;
        this.colors = colors;
        this.experience = gameManager.getExperience(level);
        this.size = 100;
        this.initializeStatisctics(null);
        this.kamas = configuration.getStartKamas();
        this.capital = (level - 1) * 5;
        this.spellPoints = level - 1;
        final Maps maps = gameManager.getMap(configuration.getStartMap());
        final Cell cell = maps.getCell(configuration.getStartCell());
        this.position = new Position(maps, cell, -1);
        this.objects = new HashMap<>();
        this.zaaps = new ArrayList<>();
        this.merchant = false;
        this.online = false;
        this.title = 0;
        int life = ((this.level - 1) * 5 + 50) + getTotalStatistics().getEffect(Stats.ADD_VITA);
        this.life = new Pair<>(life, life);
        this.spells = new HashMap<>();
        this.spellsPlace = new HashMap();
        this.learnDefaultSpell();
        if (data.create(this))
            gameManager.getPlayers().put(id, this);
    }

    private void initializeStatisctics(Map<Integer, Integer> stats) {
        this.statistics = new HashMap<>();
        this.statistics.put(StatsType.BASE, new Statistics(this, stats));
        this.statistics.put(StatsType.BUFF, new Statistics());
        this.statistics.put(StatsType.GIFT, new Statistics());
        this.statistics.put(StatsType.STUFF, new Statistics());
        this.statistics.put(StatsType.MOUNT, new Statistics());
    }

    public void joinGame() {

    }

    public void createGame() {

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
                builder.append((this.merchant ? 1 : 0)).append(";");
                builder.append("1;");
                builder.append(";");
                builder.append(";");
                return builder.toString();
            case "GMS": //Stuff
                if (getObjectByPosition(ObjectPosition.ARME) != null)
                    builder.append(Integer.toHexString(getObjectByPosition(ObjectPosition.ARME).getTemplate().getId()));
                builder.append(",");
                if (getObjectByPosition(ObjectPosition.COIFFE) != null)
                    builder.append(Integer.toHexString(getObjectByPosition(ObjectPosition.COIFFE).getTemplate().getId()));
                builder.append(",");
                if (getObjectByPosition(ObjectPosition.CAPE) != null)
                    builder.append(Integer.toHexString(getObjectByPosition(ObjectPosition.CAPE).getTemplate().getId()));
                builder.append(",");
                if (getObjectByPosition(ObjectPosition.FAMILIER) != null)
                    builder.append(Integer.toHexString(getObjectByPosition(ObjectPosition.FAMILIER).getTemplate().getId()));
                builder.append(",");
                if (getObjectByPosition(ObjectPosition.BOUCLIER) != null)
                    builder.append(Integer.toHexString(getObjectByPosition(ObjectPosition.BOUCLIER).getTemplate().getId()));
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
                //TODO : parse item
                return "";
            case "GM":
                builder.append(position.getCell().getId());//Id de la cellule
                builder.append(";").append(2).append(";");//TODO : Orientation effective
                builder.append("0").append(";");//FIXME:?
                builder.append(id).append(";");//id de la personne
                builder.append(name).append(";");//nom de la personne
                builder.append(classe.getId());//Classe
                builder.append((title > 0 ? ("," + title) : ""));
                builder.append(";").append(gfx).append("^").append(size);//gfxID^size
                builder.append(";");
                builder.append(sex).append(";");
                builder.append(alignement.getType().getId()).append(",");
                builder.append("0").append(",");
                builder.append((alignement.isWings() ? 0 : "0")).append(","); //TODO : get Grade
                builder.append(level + id);
                builder.append(";");
                builder.append((colors[0]) == -1 ? "-1" : Integer.toHexString(colors[0])).append(";");
                builder.append((colors[1]) == -1 ? "-1" : Integer.toHexString(colors[1])).append(";");
                builder.append((colors[2]) == -1 ? "-1" : Integer.toHexString(colors[2])).append(";");
                builder.append(getPacket("GMS")).append(";");
                builder.append("0;");
                builder.append(";");//Emote
                builder.append(";");//Emote timer
                builder.append(";;");
                builder.append(40).append(";"); //Speed
                builder.append(";;");
                return builder.toString();
            case "As":
                builder.append("As");
                builder.append(experience + "," + gameManager.getExperience(level) + "," + gameManager.getExperience(level + 1)).append("|");
                builder.append(kamas).append("|").append(capital).append("|").append(spellPoints).append("|");
                builder.append(alignement.getType().getId()).append("~");
                builder.append(alignement.getType().getId()).append(",").append(alignement.getGrade()).append(",").append(alignement.getGrade()).append(",").append(alignement.getHonor()).append(",").append(alignement.getDeshonnor() + ",").append(alignement.isWings() ? "1" : "0").append("|");
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
                builder.append(statistics.get(StatsType.BASE).getEffect(Stats.ADD_PERDOM)).append(",").append(statistics.get(StatsType.STUFF).getEffect(Stats.ADD_PERDOM)).append("," + statistics.get(StatsType.GIFT).getEffect(Stats.ADD_PERDOM)).append(",").append(statistics.get(StatsType.BUFF).getEffect(Stats.ADD_PERDOM)).append("|");
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

                return "";
        }
        return null;
    }

    private void learnDefaultSpell() {

    }

    public void learnSpell(int spell, int level, boolean send) {

    }

    public final Statistics getTotalStatistics() {
        Statistics finalStats = new Statistics();
        List<Statistics> stats = new ArrayList<Statistics>() {{
            add(statistics.get(StatsType.BASE));
            add(statistics.get(StatsType.STUFF));
            add(statistics.get(StatsType.GIFT));
            if (mount != null)
                add(statistics.get(StatsType.MOUNT));
            if (fight != null)
                add(statistics.get(StatsType.BUFF));
        }};
        finalStats.cumulStatistics(stats);
        return finalStats;
    }

    public int getInitiative() {
        int maxLife = this.life.getSecond() - 50;
        int life = this.life.getFirst() - 50;
        int factor = classe == Classe.SACRIEUR ? 8 : 4;
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

    public void sendText(String... message) {
        send("cs<font color='#" + (message[1] == null ? configuration.getDefaultColor() : message[1]) + "'>" + message[0] + "</font>");
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

    public final void send(String packet) {
        this.account.getClient().getSession().write(packet);
    }

    public final void delete() {
        account.getPlayers().remove(this);
        data.delete(this);
        send(account.getPlayersPacket());
    }
}
