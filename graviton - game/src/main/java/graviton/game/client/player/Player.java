package graviton.game.client.player;

import com.google.inject.Inject;
import com.google.inject.Injector;
import graviton.common.Pair;
import graviton.core.Configuration;
import graviton.factory.PlayerFactory;
import graviton.game.GameManager;
import graviton.game.alignement.Alignement;
import graviton.game.client.Account;
import graviton.game.client.player.component.ActionManager;
import graviton.game.client.player.component.CommandManager;
import graviton.game.client.player.component.FloodChecker;
import graviton.game.client.player.exchange.PlayerExchange;
import graviton.game.common.Stats;
import graviton.game.creature.Creature;
import graviton.game.creature.Position;
import graviton.game.creature.mount.Mount;
import graviton.game.creature.npc.Npc;
import graviton.game.enums.Classe;
import graviton.game.enums.ObjectPosition;
import graviton.game.enums.StatsType;
import graviton.game.fight.Fighter;
import graviton.game.group.Group;
import graviton.game.guild.Guild;
import graviton.game.guild.GuildMember;
import graviton.game.maps.Cell;
import graviton.game.maps.Maps;
import graviton.game.maps.Zaap;
import graviton.game.object.Object;
import graviton.game.spells.Spell;
import graviton.game.spells.SpellTemplate;
import graviton.game.statistics.Statistics;
import lombok.Data;
import org.jooq.Record;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static graviton.database.utils.login.Tables.PLAYERS;

/**
 * Created by Botan on 19/06/2015.
 */
@Data
public class Player implements Creature, Fighter {
    @Inject
    Configuration configuration;
    @Inject
    GameManager gameManager;
    @Inject
    CommandManager commandManager;
    @Inject
    PlayerFactory factory;

    private final int id;
    private final Account account;
    private final Classe classe;

    private String name;
    private int sex;

    private boolean online;

    private Guild guild;
    private Group group;
    private FloodChecker floodCheck;
    private ActionManager actionManager;
    private PlayerExchange exchange;

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
    private Map<Integer, Spell> spells;
    private Map<Integer, Character> spellPlace;
    private int inviting;

    private Mount mount;

    public Player(Account account, Record record, Injector injector) {
        injector.injectMembers(this);
        this.id = record.getValue(PLAYERS.ID);
        this.account = account;
        this.name = record.getValue(PLAYERS.NAME);
        this.sex = record.getValue(PLAYERS.SEX);
        this.classe = Classe.values()[record.getValue(PLAYERS.CLASS) - 1];
        String[] alignement = record.getValue(PLAYERS.ALIGNEMENT).split(";");
        this.alignement = new Alignement(this, Integer.parseInt(alignement[0]), Integer.parseInt(alignement[1]), Integer.parseInt(alignement[2]), Integer.parseInt(alignement[1]) == 1);
        this.level = record.getValue(PLAYERS.LEVEL);
        this.gfx = record.getValue(PLAYERS.GFX);
        String colors[] = record.getValue(PLAYERS.COLORS).split(";");
        this.colors = new int[]{Integer.parseInt(colors[0]), Integer.parseInt(colors[1]), Integer.parseInt(colors[2])};
        this.experience = record.getValue(PLAYERS.EXPERIENCE);
        this.configureStatisctics(record);
        this.size = record.getValue(PLAYERS.SIZE);
        this.kamas = record.getValue(PLAYERS.KAMAS);
        this.capital = record.getValue(PLAYERS.CAPITAL);
        this.spellPoints = record.getValue(PLAYERS.SPELLPOINTS);
        Maps playerMap = gameManager.getMap(Integer.parseInt(record.getValue(PLAYERS.POSITION).split(";")[0]));
        this.position = new Position(playerMap, playerMap.getCell(Integer.parseInt(record.getValue(PLAYERS.POSITION).split(";")[1])), -1);
        this.objects = new HashMap<>();
        this.zaaps = new ArrayList<>();
        this.online = true;
        this.title = 0;
        int life = ((this.level - 1) * 5 + 50) + getTotalStatistics().getEffect(Stats.ADD_VITA);
        this.life = new Pair<>(life, life);
        this.configureSpells(record.getValue(PLAYERS.SPELLS));
        this.zaaps.addAll(gameManager.getZaaps());
        if (objects != null && !objects.isEmpty())
            this.setStuff(record.getValue(PLAYERS.ITEMS));
        factory.add(this);
    }

    public Player(String name, byte sex, byte classeId, int[] colors, Account account, Injector injector) {
        injector.injectMembers(this);
        this.account = account;
        this.id = factory.getNextId();
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
        if (factory.create(this))
            factory.add(this);
    }

    private void configureStatisctics(Record record) {
        Map<Integer, Integer> stats = new HashMap<>();

        if (record != null) {
            String[] statistics = record.getValue(PLAYERS.STATISTICS).split(";");
            stats.put(Stats.ADD_VITA, Integer.parseInt(statistics[0]));
            stats.put(Stats.ADD_FORC, Integer.parseInt(statistics[1]));
            stats.put(Stats.ADD_SAGE, Integer.parseInt(statistics[2]));
            stats.put(Stats.ADD_INTE, Integer.parseInt(statistics[3]));
            stats.put(Stats.ADD_CHAN, Integer.parseInt(statistics[4]));
            stats.put(Stats.ADD_AGIL, Integer.parseInt(statistics[5]));
        }

        this.statistics = new HashMap<>();
        for (StatsType type : StatsType.values()) {
            if (type == StatsType.BASE) {
                this.statistics.put(type, new Statistics(this, stats));
                continue;
            }
            this.statistics.put(type, new Statistics());
        }
    }

    public void joinGame() {
        if (account.getClient() == null)
            return;
        account.setCurrentPlayer(this);
        this.online = true;
        if (this.mount != null)
            send("Re+" + this.mount.getPacket());
        send("Rx" + (this.mount == null ? 0 : this.mount.getExperience()));
        send(getPacket("ASK"));
        //TODO : Bonus pano & job
        send("ZS" + this.alignement.getType().getId());
        send("cC+*%!p$?^#i^@^:");
        //TODO : Gs guild packet
        send("al|");
        send(getPacket("SL"));
        send("eL7667711|0");
        send("AR6bk");
        send("Ow0|1000"); //TODO : System for pods
        send("FO+"); //TODO : seeFriends
        send("ILS2000");
        sendText(configuration.getDefaultMessage(), configuration.getDefaultColor());
        this.account.setOnline();
    }

    public void createGame() {
        send("GCK|1|" + this.name);
        send(getPacket("As"));
        this.position.getMap().addCreature(this);
    }

    @Override
    public String getGm() {
        return getPacket("GM");
    }

    private void configureSpells(String data) {
        this.spells = new HashMap<>();
        this.spellPlace = new HashMap<>();
        String[] spells = data.split(",");
        for (String element : spells) {
            int id = Integer.parseInt(element.split(";")[0]);
            int level = Integer.parseInt(element.split(";")[1]);
            char place = element.split(";")[2].charAt(0);
            this.spells.put(id, gameManager.getSpellTemplate(id).getStats(level));
            this.spellPlace.put(id, place);
        }

    }

    public String parseSpells() {
        StringBuilder builder = new StringBuilder();
        for (int key : this.spells.keySet()) {
            Spell spell = this.spells.get(key);
            builder.append(spell.getTemplate()).append(";").append(spell.getLevel()).append(";");
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
            if (object.getPosition() != ObjectPosition.NO_EQUIPED)
                this.statistics.get(StatsType.STUFF).cumulStatistics(object.getStatistics());
            this.objects.put(object.getId(), object);
        }
    }

    public boolean addObject(Object newObject, boolean save) {
        for (Object object : this.objects.values())
            if (object.getTemplate().getId() == newObject.getTemplate().getId())
                if (object.getStatistics().isSameStatistics(newObject.getStatistics())) {
                    object.setQuantity(object.getQuantity() + newObject.getQuantity());
                    gameManager.updateObject(object);
                    send("OQ" + object.getId() + "|" + object.getQuantity());
                    return false;
                }
        this.objects.put(newObject.getId(), newObject);
        send("OAKO" + newObject.parseItem());
        if (save)
            gameManager.saveObject(newObject);
        save();
        return true;
    }

    public void setObjectQuantity(Object object, int quantity) {
        send("OQ" + object.getId() + "|" + quantity);
        object.setQuantity(quantity);
    }

    public void refreshQuantity() {
        for (Object object : this.objects.values())
            send("OQ" + object.getId() + "|" + object.getQuantity());
    }

    public void removeObject(int id, boolean save) {
        this.objects.remove(id);
        if (save)
            gameManager.removeObject(id);
        send("OR" + id);
    }

    public boolean hasObject(int id) {
        return objects.containsKey(id);
    }

    public void moveObject(String packet) {
        String[] informations = packet.split("" + (char) 0x0A)[0].split("\\|");

        Object object = this.objects.get(Integer.parseInt(informations[0]));
        object.changePlace(ObjectPosition.get(Integer.parseInt(informations[1])));

        send("OM" + object.getId() + "|" + (object.getPosition() == ObjectPosition.NO_EQUIPED ? "" : object.getPosition().id));

        this.statistics.get(StatsType.STUFF).cumulStatistics(object.getStatistics());
        send(getPacket("As"));
        getMap().send("Oa" + this.id + "|" + getPacket("GMS"));
        System.err.println(this.statistics);
    }

    public String parseObject() {
        StringBuilder builder = new StringBuilder();
        objects.values().forEach(object -> builder.append(object.getId()).append(","));
        return builder.toString();
    }

    public boolean boostSpell(int id) {
        if (!spells.containsKey(id))
            return false;
        SpellTemplate spellTemplate = gameManager.getSpellTemplate(id);
        int oldLevel = spells.get(id).getLevel();
        if (spellPoints < oldLevel || oldLevel == 6 || spellTemplate.getStats(oldLevel + 1).getRequiredLevel() > this.level)
            return false;

        spellPoints -= oldLevel;
        spells.put(id, gameManager.getSpellTemplate(id).getStats(oldLevel + 1));
        send("SUK" + id + "~" + (oldLevel + 1));
        send(getPacket("As"));
        save();
        return true;
    }

    public void moveSpell(int spell, char place) {
        this.spells.keySet().stream().filter(key -> this.spellPlace.get(key) != null).filter(key -> this.spellPlace.get(key).equals(place)).forEach(this.spellPlace::remove);
        spellPlace.put(spell, place);
        save();
    }

    public void boostStatistics(int id) {
        this.classe.boostStatistics(this, id);
    }

    public Statistics getTotalStatistics() {
        return new Statistics().cumulStatistics(this.statistics.values());
    }

    public String parseStatistics() {
        Statistics statistics = this.statistics.get(StatsType.BASE);
        return statistics.getEffect(Stats.ADD_VITA) + ";" + statistics.getEffect(Stats.ADD_FORC) + ";" + statistics.getEffect(Stats.ADD_SAGE) + ";" +
                statistics.getEffect(Stats.ADD_INTE) + ";" + statistics.getEffect(Stats.ADD_CHAN) + ";" + statistics.getEffect(Stats.ADD_AGIL);
    }

    public String parseAlignement() {
        return alignement.getType().getId() + ";" + alignement.getHonor() + ";" + alignement.getDeshonnor() + ";" + (alignement.isShowWings() ? 1 : 0);
    }

    public int getInitiative() {
        int maxLife = this.life.getValue() - 50;
        double coef = maxLife / (classe == Classe.SACRIEUR ? 8 : 4);
        coef += statistics.get(StatsType.STUFF).getEffect(Stats.ADD_INIT);
        coef += getTotalStatistics().getEffect(Stats.ADD_AGIL);
        coef += getTotalStatistics().getEffect(Stats.ADD_CHAN);
        coef += getTotalStatistics().getEffect(Stats.ADD_INTE);
        coef += getTotalStatistics().getEffect(Stats.ADD_FORC);
        int initiative = 1;
        if (maxLife != 0)
            initiative = (int) (coef * ((double) (this.life.getKey() - 50) / (double) maxLife));
        if (initiative < 0)
            initiative = 0;

        return initiative;
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

    public void sendGuildInfos(char e) {
        switch (e) {
            case 'M':
                this.send("gIM+" + guild.getMembersGm());
                break;
            case 'G':
                int level = guild.getLevel();
                this.send(("gIG") + (guild.getMembers().size() > 9 ? 1 : 0) + "|" + level + "|" +
                        gameManager.getGuildExperience(level) + "|" + guild.getExperience() + "|" + gameManager.getGuildExperience(level + 1));
                break;
        }
    }

    public void createGuild(String parameters) {
        String[] arguements = parameters.split("\\|");

        if (!gameManager.checkName(arguements[4])) {
            send("gCEan");
            return;
        }

        String compiledEmblem = Integer.toString(Integer.parseInt(arguements[0]), 36) + "," + Integer.toString(Integer.parseInt(arguements[1]), 36) + "," + Integer.toString(Integer.parseInt(arguements[2]), 36) + "," + Integer.toString(Integer.parseInt(arguements[3]), 36);

        if (!gameManager.checkEmblem(compiledEmblem)) {
            send("gCEae");
            return;
        }

        this.guild = new Guild(arguements[4], compiledEmblem);
        this.guild.addMember(new GuildMember(this, 1, 1));
        this.refresh();
    }

    public void speak(String packet, String canal) {
        String message = packet.substring(1);
        if (!account.canSpeak()) return;
        if (this.floodCheck == null) this.floodCheck = new FloodChecker(this);

        if (message.charAt(1) == '.') {
            commandManager.launchCommand(this, message.substring(2).split(" "));
            return;
        }

        floodCheck.speak(packet, canal);
    }

    @Override
    public void speak(String message) {
        speak(message, "*");
    }

    public void createDialog(Npc npc) {
        // send("DCK" + npc.getId());
        npc.speak("Tiens, des items crevard !");
        this.addObject(gameManager.getObjectTemplate(40).createObject(1, false), true);
        this.addObject(gameManager.getObjectTemplate(2473).createObject(1, false), true);
        this.addObject(gameManager.getObjectTemplate(2477).createObject(1, false), true);
        this.addObject(gameManager.getObjectTemplate(2475).createObject(1, false), true);
        this.addObject(gameManager.getObjectTemplate(2476).createObject(1, false), true);
        this.addObject(gameManager.getObjectTemplate(2477).createObject(1, false), true);
        this.addObject(gameManager.getObjectTemplate(2478).createObject(1, false), true);
        this.addObject(gameManager.getObjectTemplate(2474).createObject(1, false), true);

        this.addObject(gameManager.getObjectTemplate(40).createObject(1, true), true);
        this.addObject(gameManager.getObjectTemplate(2473).createObject(1, true), true);
        this.addObject(gameManager.getObjectTemplate(2477).createObject(1, true), true);
        this.addObject(gameManager.getObjectTemplate(2475).createObject(1, true), true);
        this.addObject(gameManager.getObjectTemplate(2476).createObject(1, true), true);
        this.addObject(gameManager.getObjectTemplate(2477).createObject(1, true), true);
        this.addObject(gameManager.getObjectTemplate(2478).createObject(1, true), true);
        this.addObject(gameManager.getObjectTemplate(2474).createObject(1, true), true);
    }

    public void createGroup(Player player) {
        new Group(this, player);
    }

    public void addKamas(long kamas) {
        this.kamas += kamas;
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
        refresh();
        send(getPacket("As"));
        save();
    }

    public void askExchange(String packet) {
        switch (packet.charAt(0)) {
            case '1':
                Player target = factory.get(Integer.parseInt(packet.substring(2)));
                if (target == null || target.getMap() != this.getMap() || !target.isOnline()) {
                    send("EREE");
                    return;
                }
                packet = "ERK" + id + "|" + target.getId() + "|1";

                send(packet);
                target.send(packet);

                inviting = target.getId();
                target.setInviting(id);
                break;
        }
    }

    public void startExchange() {
        Player target = factory.get(inviting);

        if (target == null) return;

        send("ECK1");
        target.send("ECK1");

        new PlayerExchange(this, target);
    }

    public void doExchangeAction(String packet) {
        String[] informations = packet.substring(2).split("\\|");
        switch (packet.charAt(0)) {
            case 'O':
                if (packet.charAt(1) == '+') {
                    int id = Integer.parseInt(informations[0]);
                    int quantity = Integer.parseInt(informations[1]);
                    int quantityInExchange = exchange.getObjectQuantity(this, id);

                    Object object = this.objects.get(id);

                    if (hasObject(id) || (object == null) || (quantity <= 0))
                        return;

                    if (quantity > object.getQuantity() - quantityInExchange)
                        quantity = object.getQuantity() - quantityInExchange;

                    exchange.addObject(id, quantity, this.id);
                } else {
                    int id = Integer.parseInt(informations[0]);
                    int quantity = Integer.parseInt(informations[1]);

                    Object object = this.objects.get(id);

                    if (quantity <= 0 || hasObject(id) || object == null || (quantity > exchange.getObjectQuantity(this, id)))
                        return;

                    exchange.removeObject(id, quantity, id);
                }
                break;
            case 'G':// Kamas
                long kamas = Integer.parseInt(packet.substring(1));
                if (this.kamas < kamas)
                    kamas = this.kamas;
                else if (kamas < 0)
                    return;
                exchange.editKamas(id, kamas);
                break;
        }
    }

    public String getPacketName() {
        return "<a href='asfunction:onHref,ShowPlayerPopupMenu," + name + "'><b>" + name + "</b></a>";
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

    public void refresh() {
        getMap().refreshCreature(this);
    }

    public void changeOrientation(int orientation, boolean send) {
        if (send)
            getMap().send("eD" + id + "|" + orientation);
        this.position.setOrientation(orientation);
    }

    public void changePosition(Cell cell) {
        if (actionManager == null)
            actionManager = new ActionManager(this);

        if (actionManager.getStatus() == ActionManager.Status.MOVING)
            return;

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
        factory.remove(this);
        send(account.getPlayersPacket());
    }

    public void save() {
        factory.update(this);
    }

    public String getPacket(String packet) {
        StringBuilder builder = new StringBuilder();
        switch (packet) {
            case "ALK":
                builder.append("|");
                builder.append(this.id).append(";");
                builder.append(this.name).append(";");
                builder.append(this.level).append(";");
                builder.append(getGfx()).append(";");
                builder.append((getColor(1) != -1 ? Integer.toHexString(getColor(1)) : "-1")).append(";");
                builder.append((getColor(2) != -1 ? Integer.toHexString(getColor(2)) : "-1")).append(";");
                builder.append((getColor(3) != -1 ? Integer.toHexString(getColor(3)) : "-1")).append(";");
                builder.append(getPacket("GMS")).append(";");
                builder.append(0).append(";");
                builder.append("1;");
                builder.append(";");
                builder.append(";");
                return builder.toString();
            case "PM":
                builder.append(this.id).append(";");
                builder.append(this.name).append(";");
                builder.append(getGfx()).append(";");
                builder.append(getColor(1)).append(";");
                builder.append(getColor(2)).append(";");
                builder.append(getColor(3)).append(";");
                builder.append(this.getPacket("GMS")).append(";");
                builder.append(getLife().getKey()).append(",").append(getLife().getValue()).append(";");
                builder.append(this.level).append(";");
                builder.append(getInitiative()).append(";");
                builder.append(getTotalStatistics().getEffect(Stats.ADD_PROS)).append(";");
                builder.append("0");
                return builder.toString();
            case "GM":
                builder.append(getCell().getId());
                builder.append(";").append(getOrientation()).append(";");
                builder.append("0").append(";");
                builder.append(this.id).append(";");
                builder.append(this.name).append(";");
                builder.append(getClasse().getId());
                builder.append((getTitle() > 0 ? ("," + getTitle()) : ""));
                builder.append(";").append(getGfx()).append("^");
                builder.append(getSize());
                builder.append(";");
                builder.append(getSex()).append(";");
                builder.append(getAlignement().getType().getId()).append(",");
                builder.append("0").append(",");
                builder.append((getAlignement().isShowWings() ? getAlignement().getGrade() : "0")).append(",");
                builder.append(this.level + this.id);
                if (getAlignement().isShowWings() && getAlignement().getDeshonnor() > 0)
                    builder.append(",").append(1).append(';');
                else
                    builder.append(";");
                builder.append(getColor(1) == -1 ? "-1" : Integer.toHexString(getColor(1))).append(";");
                builder.append(getColor(2) == -1 ? "-1" : Integer.toHexString(getColor(2))).append(";");
                builder.append(getColor(3) == -1 ? "-1" : Integer.toHexString(getColor(3))).append(";");
                builder.append(this.getPacket("GMS")).append(";");
                builder.append(this.level > 99 ? (this.level > 199 ? (2) : (1)) : (0)).append(";");
                builder.append(";");
                builder.append(";");
                if (getGuild() != null)
                    builder.append(getGuild().getName()).append(";").append(getGuild().getEmblem()).append(";");
                else
                    builder.append(";;");
                builder.append(0).append(";");//Rebuilderiction
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
                getZaaps().forEach(zaap -> builder.append("|").append(gameManager.getMap(zaap.getMap().getId()).getId()).append(";").append(zaap.getCost(getMap())));
                return builder.toString();
            case "ASK":
                builder.append("ASK|");
                builder.append(this.id).append("|").append(this.name).append("|");
                builder.append(this.level).append("|");
                builder.append(getClasse().getId()).append("|");
                builder.append(getSex()).append("|");
                builder.append(getGfx()).append("|");
                builder.append((getColor(1)) == -1 ? "-1" : Integer.toHexString(getColor(1))).append("|");
                builder.append((getColor(2)) == -1 ? "-1" : Integer.toHexString(getColor(2))).append("|");
                builder.append((getColor(3)) == -1 ? "-1" : Integer.toHexString(getColor(3))).append("|");
                builder.append(getPacket("ASKI"));
                return builder.toString();
            case "ASKI":
                if (getObjects().isEmpty()) return "";
                getObjects().values().forEach(object -> builder.append(object.parseItem()));
                return builder.toString();
            case "As":
                builder.append("As");
                builder.append(getExperience()).append(",").append(getGameManager().getPlayerExperience(this.level)).append(",").append(gameManager.getPlayerExperience(this.level + 1)).append("|");
                builder.append(getKamas()).append("|").append(getCapital()).append("|").append(getSpellPoints()).append("|");
                builder.append(getAlignement().getType().getId()).append("~");
                builder.append(getAlignement().getType().getId()).append(",").append(getAlignement().getGrade()).append(",").append(getAlignement().getGrade()).append(",").append(getAlignement().getHonor()).append(",").append(getAlignement().getDeshonnor()).append(",").append(getAlignement().isShowWings() ? "1" : "0").append("|");
                builder.append(getLife().getKey()).append(",").append(getLife().getValue()).append("|");
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
                for (Spell spell : getSpells().values())
                    builder.append(spell.getTemplate()).append("~").append(spell.getLevel()).append("~").append(getSpellPlace().get(spell.getTemplate())).append(";");

                return builder.toString();
        }
        return null;
    }
}
