package graviton.game.client.player;

import com.google.inject.Inject;
import com.google.inject.Injector;
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
import graviton.game.creature.npc.NpcQuestion;
import graviton.game.enums.Classe;
import graviton.game.enums.ObjectPosition;
import graviton.game.enums.StatsType;
import graviton.game.exchange.Exchange;
import graviton.game.fight.Fight;
import graviton.game.fight.Fighter;
import graviton.game.group.Group;
import graviton.game.guild.Guild;
import graviton.game.guild.GuildMember;
import graviton.game.job.Job;
import graviton.game.maps.Cell;
import graviton.game.maps.Maps;
import graviton.game.maps.Zaap;
import graviton.game.object.Object;
import graviton.game.spells.Animation;
import graviton.game.spells.Spell;
import graviton.game.spells.SpellTemplate;
import graviton.game.statistics.Statistics;
import lombok.Data;
import org.jooq.Record;

import java.util.*;

import static graviton.database.utils.login.Tables.PLAYERS;

/**
 * Created by Botan on 19/06/2015.
 */
@Data
public class Player implements Creature, Fighter {
    private final int id;
    private final Account account;
    private final Classe classe;
    @Inject
    GameManager gameManager;
    @Inject
    CommandManager commandManager;
    @Inject
    PlayerFactory factory;
    private String name;
    private int sex;

    private boolean online;

    private Guild guild;
    private Group group;
    private FloodChecker floodCheck;
    private ActionManager actionManager;
    private Exchange exchange;
    private Fight fight;

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
    private int[] life = {0, 0}; //current & max

    private Position position;
    private Map<Integer, Object> objects;
    private List<Zaap> zaaps;
    private Map<Integer, Spell> spells;
    private Map<Integer, Character> spellPlace;
    private int inviting;

    private Mount mount;

    private Map<Integer,Job> jobs;

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
        this.title = record.getValue(PLAYERS.TITLE);
        int life = ((this.level - 1) * 5 + 50) + getTotalStatistics().getEffect(Stats.ADD_VITA);
        this.life[0] = life;
        this.life[1] = life;
        this.online = false;
        this.configureSpells(record.getValue(PLAYERS.SPELLS));
        this.configureJob(record.getValue(PLAYERS.JOBS));
        this.zaaps.addAll(gameManager.getZaaps());
        this.actionManager = new ActionManager(this);
        String items = record.getValue(PLAYERS.ITEMS);
        if (objects != null && items != null && !items.isEmpty())
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
        this.level = factory.getStartLevel();
        this.gfx = classeId * 10 + sex;
        this.colors = colors;
        this.experience = gameManager.getPlayerExperience(level);
        this.size = 100;
        this.configureStatisctics(null);
        this.kamas = factory.getStartKamas();
        this.capital = (level - 1) * 5;
        this.spellPoints = level - 1;
        final Maps maps = gameManager.getMap(factory.getStartMap());
        final Cell cell = maps.getCell(factory.getStartCell());
        this.position = new Position(maps, cell, -1);
        this.objects = new HashMap<>();
        this.zaaps = new ArrayList<>();
        this.online = false;
        this.title = 0;
        int life = ((this.level - 1) * 5 + 50) + getTotalStatistics().getEffect(Stats.ADD_VITA);
        this.life[0] = life;
        this.life[1] = life;
        this.spells = classe.getStartSpells(gameManager, level);
        this.spellPlace = classe.getStartPlace(gameManager);
        this.zaaps.addAll(gameManager.getZaaps());
        this.actionManager = new ActionManager(this);
        if (factory.create(this))
            factory.add(this);
    }

    private void configureJob(String data) {
        this.jobs = new HashMap<>();
        if(data == null) return;

        int id;
        String[] arguments;
        for(String job : data.split("\\|")) {
            arguments = job.split(",");
            id = Integer.parseInt(arguments[0]);
            this.jobs.put(id,new Job(id,Long.parseLong(arguments[1]),this));
        }
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
        refreshPods();
        send("FO+"); //TODO : seeFriends
        send("ILS2000");
        sendText("Bienvenue sur Horus", "000000");
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

    public Object getSameObject(Object object) {
        for (Object similar : this.objects.values())
            if (object.getTemplate().getId() == similar.getTemplate().getId() && object.getStatistics().isSameStatistics(similar.getStatistics()))
                return similar;
        return null;
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

    public void removeObject(int id,int quantity) {
        Object object = this.objects.get(id);

        if(object != null) {
            if((object.getQuantity() - quantity) <= 0) {
                removeObject(id,true);
                return;
            }
            object.setQuantity(object.getQuantity() - quantity);
            refreshQuantity();
        }
    }

    public boolean hasObject(int id) {
        return objects.containsKey(id);
    }

    public Object getObjectByTemplate(int id) {
        for(Object object : objects.values())
            if (object.getTemplate().getId() == id)
                return object;
        return null;
    }

    public void moveObject(String packet) {
        String[] informations = packet.split("" + (char) 0x0A)[0].split("\\|");

        Object object = this.objects.get(Integer.parseInt(informations[0]));
        object.changePlace(ObjectPosition.get(Integer.parseInt(informations[1])));

        send("OM" + object.getId() + "|" + (object.getPosition() == ObjectPosition.NO_EQUIPED ? "" : object.getPosition().id));

        this.statistics.get(StatsType.STUFF).cumulStatistics(object.getStatistics());
        send(getPacket("As"));
        getMap().send("Oa" + this.id + "|" + getPacket("GMS"));
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
        refreshPods();
    }

    public void refreshPods() {
        send("Ow" + this.getPodsUsed() + "|" + this.getMaxPods());
    }

    public void resetStatistics() {
        this.statistics.get(StatsType.BASE).getEffects().clear();
        capital = (level - 1) * 5;
        send(getPacket("As"));
        send("Im023;" + capital);
        factory.update(this);
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
        int maxLife = this.life[1] - 50;
        double coefficient = maxLife / (classe == Classe.SACRIEUR ? 8 : 4);
        coefficient += statistics.get(StatsType.STUFF).getEffect(Stats.ADD_INIT);
        coefficient += getTotalStatistics().getEffect(Stats.ADD_AGIL);
        coefficient += getTotalStatistics().getEffect(Stats.ADD_CHAN);
        coefficient += getTotalStatistics().getEffect(Stats.ADD_INTE);
        coefficient += getTotalStatistics().getEffect(Stats.ADD_FORC);
        int initiative = 1;
        if (maxLife != 0)
            initiative = (int) (coefficient * ((double) (this.life[0] - 50) / (double) maxLife));
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

    public void startFight(Fight fight) {

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

    public void launchAnimation() {
        int[] aleatories = {345, 53, 319, 380, 381, 210, 342, 337, 334, 330, 294, 293, 281, 372};
        Animation animation = gameManager.getAnimation(aleatories[new Random().nextInt(aleatories.length)]);
        send("GA;" + 228 + ";" + id + ";" + getCell().getId() + "," + animation.getGA());
    }

    @Override
    public void speak(String message) {
        speak("*|" + message, "*");
    }

    public void createDialog(Npc npc) {
        send("DCK" + npc.getId());
        NpcQuestion question = gameManager.getNpcQuestion(npc.getTemplate().getInitQuestion());
        if(question == null) {
            send("DV");
            return;
        }
        send("DQ" + question.getDQPacket(this));
        this.inviting = npc.getId();
        setActionState(ActionManager.Status.DIALOG);
    }

    public void createDialog(int questionId) {
        NpcQuestion question = gameManager.getNpcQuestion(questionId);
        if(question == null) {
            this.inviting = 0;
            setActionState(ActionManager.Status.WAITING);
            send("DV");
            return;
        }
        send("DQ" + question.getDQPacket(this));
    }

    public void quitDialog() {
        this.inviting = 0;
        this.send("DV");
        this.setActionState(ActionManager.Status.WAITING);
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

    public boolean isBusy() {
        return !isOnline() || actionManager.getStatus() != ActionManager.Status.WAITING;
    }

    public void askExchange(String packet) {
        switch (packet.charAt(0)) {
            case '1':
                Player target = factory.get(Integer.parseInt(packet.substring(2)));
                if (target == null || target.getMap() != this.getMap() || !target.isOnline() || target.isBusy()) {
                    send("EREE");
                    return;
                }
                packet = "ERK" + id + "|" + target.getId() + "|1";
                send(packet);
                target.send(packet);
                new PlayerExchange(this, target);
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

                    if (!hasObject(id) || (object == null) || (quantity <= 0))
                        return;

                    if (quantity > object.getQuantity() - quantityInExchange)
                        quantity = object.getQuantity() - quantityInExchange;

                    exchange.addObject(id, quantity, this.id);
                } else {
                    int id = Integer.parseInt(informations[0]);
                    int quantity = Integer.parseInt(informations[1]);

                    exchange.removeObject(id, quantity, this.id);
                }
                break;
            case 'G':// Kamas
                exchange.editKamas(id, Integer.parseInt(packet.substring(1)));
                break;
        }
    }

    public String getPacketName() {
        return "<a href='asfunction:onHref,ShowPlayerPopupMenu," + name + "'><b>" + name + "</b></a>";
    }

    public void setActionState(ActionManager.Status state) {
        this.actionManager.setStatus(state);
    }

    public void createAction(int id, String arguments) {
        this.actionManager.createAction(id, arguments);
    }

    public void sendText(String... arguments) {
        String message = arguments[0];
        String color;
        try {
            color = arguments[1];
        } catch (ArrayIndexOutOfBoundsException e) {
            color = "000000";
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
            if (zaap.getMap().getId() == maps.getId()) {
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
        System.err.println(cell);
        System.err.println(cost);
        if (cell != -1)
            changePosition(maps.getCell(cell));
    }

    public String getValue(String value) {
        if(value.equals("name"))
            return this.getName();
        if(value.equals("bankCost"))
            return String.valueOf(account.getBankPrice());
        return "";
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
        if (actionManager.getStatus() == ActionManager.Status.MOVING)
            return;

        if (this.getMap().getId() != cell.getMap().getId()) {
            this.getMap().removeCreature(this);
            this.position.setCell(cell);
            cell.getMap().addCreature(this);
            return;
        }
        getMap().changeCell(this, cell);
        factory.update(this);
    }

    public void changePosition(int map,int cell) {
        Maps maps = gameManager.getMap(map);
        if(maps == null)
            return;
        changePosition(maps.getCell(cell));
    }

    private int getPodsUsed() {
        int pod = 0;
        for(Object object : this.objects.values())
            pod += (object.getTemplate().getUsedPod() * object.getQuantity());
        return pod;
    }

    private int getMaxPods() {
        return this.getTotalStatistics().getEffect(Stats.ADD_PODS) + this.getTotalStatistics().getEffect(Stats.ADD_FORC) * 5 + 1000;
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

    public int getLife(boolean max) {
        return life[max ? 1 : 0];
    }

    public String getPacket(String packet) {
        return factory.getPackets().get(packet).get(this);
    }
}
