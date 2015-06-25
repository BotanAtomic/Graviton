package graviton.game.client.player;

import graviton.core.Configuration;
import graviton.core.Main;
import graviton.database.DatabaseManager;
import graviton.database.data.PlayerData;
import graviton.enums.*;
import graviton.game.client.Account;
import graviton.game.client.player.statistics.BaseStatistics;
import graviton.game.client.player.statistics.BuffStatistics;
import graviton.game.client.player.statistics.DonStatistics;
import graviton.game.client.player.statistics.StuffStatistics;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Botan on 19/06/2015.
 */
@Data
public class Player {

    private final PlayerData data;
    private final Configuration configuration;
    private final GameManager gameManager;

    private Account account;
    private boolean online;

    private int id;
    private String name;
    private int sex;
    private Classe classe;
    private Alignement alignement;
    private int level;
    private int gfx;
    private int[] colors;
    private long experience;
    private int size;
    private Map<StatsType,Statistics> statistics;
    private long kamas;
    private int capital;
    private int spellPoints;
    private int title;

    private Position position;
    private Map<Integer, Object> objects;
    private List<Zaap> zaaps;
    private Map<Integer, SpellStats> spells;
    private Map<Integer, Character> spellsPlace;

    private Fight fight;
    private Mount mount;

    private boolean merchant;

    public Player(int id,int account, String name, int sex, int classe, int alignement, int honor, int deshonnor,
                  int level, int gfx, int[] colors, long experience, int size, Map<Integer, Integer> stats,
                  long kamas, int capital, int spellPoints, int map, int cell) {
        this.data = (PlayerData) Main.getInstance(DatabaseManager.class).getData().get(DataType.PLAYER);
        this.configuration = Main.getInstance(Configuration.class);
        this.gameManager = Main.getInstance(GameManager.class);
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
        this.statistics = new HashMap<>();
        this.statistics.put(StatsType.BASE,new BaseStatistics(stats, this));
        this.statistics.put(StatsType.BUFFS,new BuffStatistics());
        this.statistics.put(StatsType.DONS,new DonStatistics());
        this.statistics.put(StatsType.STUFF,new StuffStatistics());
        this.size = size;
        this.kamas = kamas;
        this.capital = capital;
        this.spellPoints = spellPoints;
        final Maps playerMap = gameManager.getMap(map);
        this.position = new Position(playerMap, playerMap.getCell(cell),-1);
        this.objects = new HashMap<>();
        this.zaaps = new ArrayList<>();
        this.merchant = false;
        this.online = false;
        this.title = 0;
        gameManager.getPlayers().put(id, this);
    }

    /**
     * @param account = Account du player
     * Constructor for create player
     **/
    public Player(String name, byte sex, byte classeId, int[] colors, Account account) {
        this.account = account;
        this.data = (PlayerData) Main.getInstance(DatabaseManager.class).getData().get(DataType.PLAYER);
        this.configuration = Main.getInstance(Configuration.class);
        this.gameManager = Main.getInstance(GameManager.class);
        this.id = data.getNextId();
        this.name = name;
        this.sex = sex;
        this.classe = Classe.values()[classeId - 1];
        this.alignement = new Alignement();
        this.level = configuration.getStartlevel();
        this.gfx = classeId * 10 + sex;
        this.colors = colors;
        this.experience = 0; //TODO : Experience (Getting)
        this.size = 100;
        this.statistics = new HashMap<>();
        this.statistics.put(StatsType.BASE,new BaseStatistics(null,this));
        this.statistics.put(StatsType.BUFFS,new BuffStatistics());
        this.statistics.put(StatsType.DONS,new DonStatistics());
        this.statistics.put(StatsType.STUFF,new StuffStatistics());
        this.kamas = configuration.getStartKamas();
        this.capital = (level - 1) * 5;
        this.spellPoints = level - 1;
        final Maps maps = gameManager.getMap(configuration.getStartMap());
        final Cell cell = maps.getCell(configuration.getStartCell());
        this.position = new Position(maps, cell,0);
        this.objects = new HashMap<>();
        this.zaaps = new ArrayList<>();
        this.merchant = false;
        this.online = false;
        this.title = 0;
        if (data.create(this))
            gameManager.getPlayers().put(id, this);
    }

    public void joinGame() {
        this.online = true;
        send("BN");
        send("Rx"+(this.mount == null ? 0 : mount.getExperience()));
        send(getPacket("ALK"));
        send("ZS"+alignement.getType().getId());
        send("cC+*#%!pi$:?^@¤"); /** all canals **/
        send("al|");
        send("SL183~5~b;193~5~d;200~6~c;201~6~f");
        send("eL7667711|0");
        send("AR6bk");
        send("Ow0|1000"); //TODO : System for pods
        send("FO+");
        send("Im189");
        //TODO : send("Im0152;2015~06~23~12~24~127.0.0.1");
        //TODO : send("Im0153;127.0.0.1");
        sendText(configuration.getDefaultMessage());
        send("ILS2000");
    }

    public void createGame() {
        send("GCK|1|"+name);
        send(getPacket("AS"));
        send("GDM|" + position.getMap().getId()+"|0"+position.getMap().getDate()+"|"+position.getMap().getKey());
        position.getMap().addPlayer(this);
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
                builder.append(";").append(position.getOrientation()).append(";");//Orientation effective
                builder.append("0").append(";");//FIXME:?
                builder.append(id).append(";");//id de la personne
                builder.append(name).append(";");//nom de la personne
                builder.append(classe.getId());//Classe
                builder.append((title > 0 ? ("," + title) : ""));
                builder.append(";").append(gfx).append("^").append(size);//gfxID^size
                builder.append(";");
                builder.append(sex).append(";");
                builder.append(alignement.getType().getId()).append(",")
                        .append("0").append(",")
                        .append((alignement.isWings() ? 0 : "0")).append(",") //TODO : get Grade
                        .append(level + id);
                builder.append(";");
                builder.append((colors[0]) == -1 ? "-1" : Integer.toHexString(colors[0])).append(";");
                builder.append((colors[1]) == -1 ? "-1" : Integer.toHexString(colors[1])).append(";");
                builder.append((colors[2]) == -1 ? "-1" : Integer.toHexString(colors[2])).append(";");
                builder.append(getPacket("GMS")).append(";");
                builder.append("0;");
                builder.append(";");//Emote
                builder.append(";");//Emote timer
                builder.append(";;");
                builder.append(40).append(";");
                builder.append(";");
                return builder.toString();
            case "AS":

                return "";
            case "SL" :

                return "";
        }
        return null;
    }

    public void sendText(String message) {
        send("cs<font color='#" + configuration.getDefaultColor() + "'>" + message + "</font>");
    }

    public void sendText(String message, String color) {
        send("cs<font color='#" + color + "'>" + message + "</font>");
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

    public void send(String packet) {
       this.account.getClient().getSession().write(packet);
    }

    public void setAccount(Account account) {
        this.account = account;
    }

}
