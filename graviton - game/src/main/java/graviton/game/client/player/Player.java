package graviton.game.client.player;

import com.google.inject.Inject;
import com.google.inject.Injector;
import graviton.factory.PlayerFactory;
import graviton.game.GameManager;
import graviton.game.action.player.ActionManager;
import graviton.game.action.player.CommandManager;
import graviton.game.alignement.Alignement;
import graviton.game.client.Account;
import graviton.game.client.player.packet.Packets;
import graviton.game.common.FloodChecker;
import graviton.game.common.Stats;
import graviton.game.creature.Creature;
import graviton.game.creature.Position;
import graviton.game.creature.mount.Mount;
import graviton.game.creature.npc.Npc;
import graviton.game.creature.npc.NpcQuestion;
import graviton.game.enums.Classe;
import graviton.game.enums.IdType;
import graviton.game.enums.StatsType;
import graviton.game.exchange.api.Exchange;
import graviton.game.exchange.player.PlayerExchange;
import graviton.game.fight.Fight;
import graviton.game.fight.Fightable;
import graviton.game.fight.Fighter;
import graviton.game.group.Group;
import graviton.game.guild.Guild;
import graviton.game.guild.GuildMember;
import graviton.game.job.Job;
import graviton.game.maps.Cell;
import graviton.game.maps.Maps;
import graviton.game.maps.Zaap;
import graviton.game.object.Object;
import graviton.game.object.ObjectPosition;
import graviton.game.object.ObjectType;
import graviton.game.object.panoply.PanoplyTemplate;
import graviton.game.spells.Spell;
import graviton.game.spells.SpellTemplate;
import graviton.game.statistics.Statistics;
import lombok.Data;
import org.jooq.Record;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static graviton.database.utils.login.Tables.PLAYERS;

/**
 * Created by Botan on 19/06/2015.
 */
@Data
public class Player implements Creature, Fightable {
    private int id;
    private Account account;
    private Classe classe;
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

    private int askedCreature;

    private Mount mount;

    private Map<Integer, Job> jobs;

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
        this.configureStatistics(record);
        this.size = record.getValue(PLAYERS.SIZE);
        this.kamas = record.getValue(PLAYERS.KAMAS);
        this.capital = record.getValue(PLAYERS.CAPITAL);
        this.spellPoints = record.getValue(PLAYERS.SPELLPOINTS);
        Maps playerMap = gameManager.getMap(Integer.parseInt(record.getValue(PLAYERS.POSITION).split(";")[0]));
        this.position = new Position(playerMap, playerMap.getCell(Integer.parseInt(record.getValue(PLAYERS.POSITION).split(";")[1])), -1);
        this.objects = new ConcurrentHashMap<>();
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
        if (items != null && !items.isEmpty())
            this.setStuff(items);
        factory.add(this);
    }

    public Player(String name, byte sex, byte classeId, int[] colors, Account account, Injector injector) {
        injector.injectMembers(this);
        this.account = account;
        this.send("TB");
        this.id = factory.getNextId();
        this.name = name;
        this.sex = sex;
        this.classe = Classe.values()[classeId - 1];
        this.alignement = new Alignement(this);
        this.level = factory.getStartLevel();
        this.gfx = (classeId * 10) + sex;
        this.colors = colors;
        this.experience = gameManager.getPlayerExperience(level);
        this.size = 100;
        this.configureStatistics(null);
        this.kamas = factory.getStartKamas();
        this.capital = (level - 1) * 5;
        this.spellPoints = level - 1;
        final Maps maps = gameManager.getMap(factory.getStartMap());
        final Cell cell = maps.getCell(factory.getStartCell());
        this.position = new Position(maps, cell, -1);
        this.objects = new ConcurrentHashMap<>();
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

    /**
     * Configuation
     **/

    private void configureJob(String data) {
        this.jobs = new HashMap<>();
        if (data == null || data.isEmpty()) return;
    }

    private void configureStatistics(Record record) {
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
            if (type == StatsType.BASE)
                this.statistics.put(type, new Statistics(this, stats));
            else
                this.statistics.put(type, new Statistics());
        }
    }

    public void joinGame() {
        account.setCurrentPlayer(this);
        try {
            send(getPacket(Packets.ASK));
            send(getPacket(Packets.OS));
            send(getPacket(Packets.As));
            this.position.getMap().addCreature(this);

            if (this.mount != null)
                send("Re+" + this.mount.getPacket());
            send("Rx" + (this.mount == null ? 0 : this.mount.getExperience()));

            send(getPacket(Packets.SL));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void createGame() {
        send("GCK|1|" + this.name);
        send("al|");
        send("FO+");
        send("ILS2000");
        send("ZS" + this.alignement.getType().getId());
        send("cC+*%!p$?^#i^@^:");
        //TODO : Gs guild packet
        send("eL7667711|0");
        refreshPods();
        send("AR6bk");
        this.account.update();
        send("Im0152;" + this.getAccount().getInformations());
        send("Im0153;" + this.account.getNetworkAddress());
        sendText("Bienvenue sur Horus", "000000");
        this.online = true;
        this.account.setOnline();
    }

    private void configureSpells(String data) {
        this.spells = new HashMap<>();
        this.spellPlace = new HashMap<>();
        int id;
        for (String element : data.split(",")) {
            id = Integer.parseInt(element.split(";")[0]);
            this.spells.put(id, this.gameManager.getSpellTemplate(id).getStats(Integer.parseInt(element.split(";")[1])));
            this.spellPlace.put(id, element.split(";")[2].charAt(0));
        }
    }

    /**
     * Objects
     **/
    private void setStuff(String objects) {
        for (String data : objects.split(",")) {
            Object object = this.gameManager.getObject(Integer.parseInt(data));
            if (object.getObjectPosition() != ObjectPosition.NO_EQUIPED)
                this.statistics.get(StatsType.STUFF).accumulateStatistics(object.getStatistics());
            this.objects.put(object.getId(), object);
        }

    }

    public boolean addObject(Object newObject, boolean save) {
        final boolean[] value = {true};
        Object sameObject = getSameObject(newObject);

        if (sameObject != null) {
            sameObject.setQuantity(sameObject.getQuantity() + newObject.getQuantity());
            this.gameManager.updateObject(sameObject);
            this.send("OQ" + sameObject.getId() + "|" + sameObject.getQuantity());
            value[0] = false;
        } else {
            this.objects.put(newObject.getId(), newObject);
            this.send("OAKO" + newObject.parseItem());
        }
        if (save) {
            this.gameManager.saveObject(newObject);
            this.save();
        }
        this.refreshPods();
        return value[0];
    }

    public List<Integer> getNumberOfEquippedObject(int panoply) {
        return getEquippedObjects().stream().filter(object -> object.getTemplate().getPanoplyTemplate() != null && object.getTemplate().getPanoplyTemplate().getId() == panoply).map(object -> object.getTemplate().getId()).collect(Collectors.toList());
    }

    public List<Object> getEquippedObjects() {
        return this.objects.values().stream().filter(allObject -> allObject.getTemplate().getPanoplyTemplate() != null && allObject.getObjectPosition() != ObjectPosition.NO_EQUIPED).map(object -> object).collect(Collectors.toList());
    }

    public List<PanoplyTemplate> getPanoplys() {
        List<PanoplyTemplate> templates = new ArrayList<>();
        getEquippedObjects().stream().filter(object -> !templates.contains(object.getTemplate().getPanoplyTemplate())).forEach(object -> templates.add(object.getTemplate().getPanoplyTemplate()));
        return templates;
    }

    public Object getSameObject(Object object) {
        final Object[] similarObject = {null};
        this.objects.values().stream().filter(object1 -> object1.getStatistics().isSameStatistics(object.getStatistics())).filter(object2 -> object.getTemplate().getId() == object2.getTemplate().getId()).filter(object3 -> object3.getObjectPosition() == ObjectPosition.NO_EQUIPED).forEach(sameObject -> similarObject[0] = sameObject);
        return similarObject[0];
    }

    public void setObjectQuantity(Object object, int quantity) {
        if (quantity > 0) {
            send("OQ" + object.getId() + "|" + quantity);
            object.setQuantity(quantity);
        } else
            removeObject(object.getId(), true);
    }

    public void refreshQuantity() {
        this.objects.values().forEach(object -> send("OQ" + object.getId() + "|" + object.getQuantity()));
    }

    public void removeObject(int id, boolean save) {
        this.objects.remove(id);
        send("OR" + id);
        if (save) {
            gameManager.removeObject(id);
            save();
        }
    }

    public void removeObject(int id, int quantity) {
        Object object = this.objects.get(id);

        if (object.getQuantity() <= quantity)
            this.removeObject(id, true);
        else
            this.setObjectQuantity(object, object.getQuantity() - quantity);
        this.refreshPods();
    }

    public boolean hasObject(int id) {
        return objects.containsKey(id);
    }

    public Object getObjectByTemplate(int id) {
        final Object[] object = {null};
        objects.values().stream().filter(object1 -> object1.getTemplate().getId() == id).forEach(object2 -> object[0] = object2);
        return object[0];
    }

    public synchronized void moveObject(String packet) {
        String[] informations = packet.split("\\|");

        int position = Integer.parseInt(informations[1]);
        boolean equipable = position != -1 && !(position >= 35 && position <= 57) ? ObjectPosition.get(position) != null ? true : false : false;

        int quantity;
        if ((quantity = (informations.length > 2 && !equipable) ? Integer.parseInt(informations[2]) : 1) < 1)
            return;

        Object object = this.objects.get(Integer.parseInt(informations[0]));

        if (object == null || !ObjectPosition.get(position).contains(object.getTemplate().getType().getValue()))
            return;

        System.err.println("equipable = " + equipable);
        Object sameObject = this.getSameObjectByPosition(object, position);

        if (sameObject != null)
            if (!sameObject.getStatistics().isSameStatistics(object.getStatistics()) || sameObject.getTemplate().getId() != object.getTemplate().getId()) {
                Object newObject = object.getClone(quantity, position);
                this.setObjectQuantity(object, object.getQuantity() - quantity);
                this.objects.put(newObject.getId(), newObject);
                this.send("OAKO" + newObject.parseItem());
                this.send(getPacket(Packets.OS));
                this.send(getPacket(Packets.As));
                return;
            }

        if (sameObject != null || quantity < object.getQuantity()) {
            if (sameObject != null && !equipable) {
                if (quantity >= object.getQuantity()) {
                    this.removeObject(object.getId(), true);
                    System.err.println(packet + " STAPE 1/2");
                } else {
                    this.setObjectQuantity(object, object.getQuantity() - quantity);
                    System.err.println(packet + " STAPE 2/2");
                }
                this.setObjectQuantity(sameObject, sameObject.getQuantity() + quantity);
                System.err.println(packet + " STAPE 2");
            } else if (sameObject == null || equipable) {
                Object equipped;
                if ((equipped = getObjectByPosition(position)) != null) {
                    removeObject(equipped.getId(), true);
                    this.setObjectQuantity(sameObject, sameObject.getQuantity() + 1);
                }

                if (object.getTemplate().getType() == ObjectType.ANNEAU && (equipped = getObjectByPosition(position == ObjectPosition.ANNEAU2.id ? ObjectPosition.ANNEAU1.id : ObjectPosition.ANNEAU2.id)) != null) {
                    if (equipped.getTemplate().getPanoplyTemplate() != null && object.getTemplate().getPanoplyTemplate() != null)
                        if (equipped.getTemplate().getPanoplyTemplate().getId() == object.getTemplate().getPanoplyTemplate().getId()) {
                            removeObject(equipped.getId(), true);
                            this.setObjectQuantity(sameObject, sameObject.getQuantity() + 1);
                        }
                }
                System.err.println(packet + " STAPE 3");
                Object newObject = object.getClone(quantity, position);
                this.setObjectQuantity(object, object.getQuantity() - quantity);
                this.objects.put(newObject.getId(), newObject);
                this.send("OAKO" + newObject.parseItem());
                System.err.println(packet + " STAPE 3 OK");
            }
            this.save();
            System.err.println(packet + " STAPE 4");
        } else {
            object.changePlace(ObjectPosition.get(position), position >= 35 && position <= 57 ? position : 0);
            this.send("OM" + object.getId() + "|" + (object.getPosition().getKey() == ObjectPosition.NO_EQUIPED ? object.getPosition().getValue() == 0 ? "" : object.getPosition().getValue() : object.getPosition().getKey().id));
            System.err.println(packet + " STAPE 5 OK");
        }

        if (!(position >= 35 && position <= 57)) {
            if (position != -1) {
                this.statistics.get(StatsType.STUFF).accumulateStatistics(object.getStatistics());
            } else if (position == -1)
                this.statistics.get(StatsType.STUFF).removeStatistics(object.getStatistics());
            if (object.getTemplate().getPanoplyTemplate() != null && this.getNumberOfEquippedObject(object.getTemplate().getPanoplyTemplate().getId()).isEmpty())
                send("OS-" + object.getTemplate().getPanoplyTemplate().getId());
            if (object.getTemplate().getType() == ObjectType.COIFFE || object.getTemplate().getType() == ObjectType.CAPE || object.getTemplate().getType() == ObjectType.FAMILIER)
                this.getMap().send("Oa" + this.id + "|" + getPacket(Packets.GMS));
        }
        this.send(getPacket(Packets.OS));
        this.send(getPacket(Packets.As));
    }

    public void removeObject(String packet) {
        String[] informations = packet.split("\\|");
        this.removeObject(Integer.parseInt(informations[0]), Integer.parseInt(informations[1]));
    }

    public String parseObject() {
        if (this.objects.isEmpty()) return "";
        StringBuilder builder = new StringBuilder();
        objects.values().forEach(object -> builder.append(object.getId()).append(","));
        return builder.toString().substring(0, builder.length() - 1);
    }

    /**
     * Spell
     **/
    public boolean boostSpell(int id) {
        SpellTemplate spellTemplate = gameManager.getSpellTemplate(id);
        int oldLevel = spells.get(id).getLevel();

        if (spellPoints < oldLevel || oldLevel == 6 || spellTemplate.getStats(oldLevel + 1).getRequiredLevel() > this.level)
            return false;

        spellPoints -= oldLevel;
        spells.put(id, gameManager.getSpellTemplate(id).getStats(oldLevel + 1));
        send("SUK" + id + "~" + (oldLevel + 1));
        send(getPacket(Packets.As));
        save();
        return true;
    }

    public void moveSpell(int spell, char place) {
        this.spells.keySet().stream().filter(key -> this.spellPlace.get(key) != null).filter(key -> this.spellPlace.get(key).equals(place)).forEach(this.spellPlace::remove);
        spellPlace.put(spell, place);
        save();
    }

    public String parseSpells() {
        StringBuilder builder = new StringBuilder();
        this.spells.keySet().forEach(spellId -> {
            Spell spell = this.spells.get(spellId);
            builder.append(spell.getTemplate()).append(";").append(spell.getLevel()).append(";");
            builder.append(this.spellPlace.get(spellId) != null ? this.spellPlace.get(spellId) : "_");
            builder.append(",");
        });
        return builder.toString().substring(0, builder.length() - 1);
    }

    public void learnSpell(int spell, int level) {

    }

    public void boostStatistics(int id) {
        this.classe.boostStatistics(this, id);
        this.refreshPods();
    }

    public void refreshPods() {
        send("Ow" + this.getPodsUsed() + "|" + this.getMaxPods());
    }

    public void resetStatistics() {
        this.statistics.get(StatsType.BASE).getEffects().clear();
        capital = (level - 1) * 5;
        send(getPacket(Packets.As));
        send("Im023;" + capital);
        save();
    }

    public Statistics getStuffStatistics() {
        return new Statistics().accumulateStatistics(Arrays.asList(this.statistics.get(StatsType.PANOPLY), this.statistics.get(StatsType.STUFF)));
    }

    public Statistics getTotalStatistics() {
        return new Statistics().accumulateStatistics(this.statistics.values());
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
        coefficient += getTotalStatistics().getEffect(Stats.ADD_AGIL) + getTotalStatistics().getEffect(Stats.ADD_CHAN) + getTotalStatistics().getEffect(Stats.ADD_INTE);
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

    public Object getObjectByPosition(int position) {
        final Object[] value = {null};
        if (position >= 35 && position <= 57)
            this.objects.values().stream().filter(object -> object.getShortcut() == position).forEach(object1 -> value[0] = object1);
        else
            this.objects.values().stream().filter(object -> object.getObjectPosition().id == position && object.getShortcut() == 0).forEach(object1 -> value[0] = object1);
        return value[0];
    }

    public Object getSameObjectByPosition(Object object, int position) {
        final Object[] value = {null};
        if (position >= 35 && position <= 57)
            this.objects.values().stream().filter(object1 -> object1.getShortcut() == position && object.getStatistics().isSameStatistics(object1.getStatistics())).forEach(object1 -> value[0] = object1);
        else
            this.objects.values().stream().filter(item -> item.getTemplate().getId() == object.getTemplate().getId()).filter(item -> item.getStatistics().isSameStatistics(object.getStatistics())).filter(item -> item.getObjectPosition().id == -1).forEach(item -> value[0] = item);
        return value[0];
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

    @Override
    public void speak(String message) {
        speak("*|" + message, "*");
    }

    public void createDialog(Npc npc) {
        send("DCK" + npc.getId());
        NpcQuestion question = gameManager.getNpcQuestion(npc.getTemplate().getInitQuestion());
        if (question == null) {
            send("DV");
            return;
        }
        send("DQ" + question.getDQPacket(this));
        this.askedCreature = npc.getId();
        setActionState(ActionManager.Status.DIALOG);
    }

    public void createDialog(int questionId) {
        NpcQuestion question = gameManager.getNpcQuestion(questionId);
        if (question == null) {
            this.askedCreature = 0;
            setActionState(ActionManager.Status.WAITING);
            send("DV");
            return;
        }
        send("DQ" + question.getDQPacket(this));
    }

    public void quitDialog() {
        this.askedCreature = 0;
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
        send(getPacket(Packets.As));
        save();
    }

    public void askDefy(String arguments) {
        if (isBusy() || fight != null) {
            send("GA;903;" + this.id + ";o");
            return;
        }

        this.askedCreature = Integer.parseInt(arguments);

        if (!getMap().supportFight()) {
            send("GA;903;" + this.id + ";p");
            return;
        }

        Player askedPlayer = factory.get(askedCreature);

        if (askedPlayer == null || askedPlayer.getFight() != null || askedPlayer.isBusy()) {
            send("GA;903;" + this.id + ";z");
            return;
        }
        getActionManager().setStatus(ActionManager.Status.DEFYING);
        getMap().send("GA;900;" + this.id + ";" + askedCreature);
    }

    public void acceptDefy(String argument) {

    }

    public void askExchange(String packet) {
        String[] data = packet.split("\\|");
        switch (Integer.parseInt(data[0])) {

            case 0:
                Npc npc = (Npc) getMap().getCreatures(IdType.NPC).get(Integer.parseInt(data[1]));
                assert npc != null;
                send("ECK0|".concat(String.valueOf(npc.getId())));
                send("EL".concat(npc.getTemplate().getSellObjectsPacket()));
                break;

            case 1:
                Player target = factory.get(Integer.parseInt(packet.substring(2)));
                if (target == null || target.getMap() != this.getMap() || !target.isOnline() || target.isBusy()) {
                    send("EREE");
                    return;
                }
                packet = "ERK" + id + "|" + target.getId() + "|1";
                send(packet);
                target.send(packet);
                new PlayerExchange(this, target);
                askedCreature = target.getId();
                target.setAskedCreature(id);
                break;

            case 11: {

                break;
            }
        }
    }

    public void startExchange() {
        Player target = factory.get(askedCreature);
        assert target != null;
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

    public void setActionState(ActionManager.Status state) {
        this.actionManager.setStatus(state);
    }

    public void createAction(int id, String arguments) {
        this.actionManager.createAction(id, arguments);
    }

    public void openZaap() {
        send(getPacket(Packets.WC));
    }

    public void useZaap(int id) {
        Maps maps = gameManager.getMap(id);
        Zaap zaap = gameManager.getZaap(maps.getId());

        int cost = zaap.getCost(getMap());
        send("WV");

        if (this.kamas >= cost) {
            changePosition(maps.getCell(zaap.getCell()));
            this.kamas -= cost;
            send("Im046;" + cost);
            send(getPacket(Packets.As));
        } else
            send("Im01;" + "Il vous faut " + (cost - kamas) + " kamas en plus pour pouvoir utiliser ce zaap.");
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
        } else
            getMap().changeCell(this, cell);
        save();
    }

    public void changePosition(int map, int cell) {
        Maps maps = gameManager.getMap(map);
        if (maps == null)
            return;
        changePosition(maps.getCell(cell));
    }

    public boolean isBusy() {
        return !isOnline() || actionManager.getStatus() != ActionManager.Status.WAITING;
    }

    public String getPacketName() {
        return "<a href='asfunction:onHref,ShowPlayerPopupMenu," + name + "'><b>" + name + "</b></a>";
    }

    public String getValue(String value) {
        if (value.equals("name"))
            return this.getName();
        if (value.equals("bankCost"))
            return String.valueOf(account.getBankPrice());
        return "";
    }

    public int getPodsUsed() {
        final int[] pod = {0};
        this.objects.values().forEach(object -> pod[0] += object.getTemplate().getUsedPod() * object.getQuantity());
        return pod[0];
    }

    public int getMaxPods() {
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

    public int getLife(boolean max) {
        return life[max ? 1 : 0];
    }

    public String getPacket(Packets packet) {
        return factory.getPackets().get(packet).perform(this);
    }

    @Override
    public String getGm() {
        return getPacket(Packets.GM);
    }

    public void refresh() {
        getMap().refreshCreature(this);
    }

    public void send(String packet) {
        if (!packet.contains("\n"))
            this.account.getClient().send(packet);
        else
            for (String newPacket : packet.split("\n"))
                this.account.getClient().send(newPacket);
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

    public boolean checkAttribut(String attribut) {
        if (attribut == null)
            return true;
        switch (attribut.toLowerCase()) {
            case "group":
                return this.getGroup() != null;
            case "guild":
                return this.getGuild() != null;
            case "rank":
                return account.getRank().id != 0;
        }
        return false;
    }

    public void delete() {
        account.getPlayers().remove(this);
        factory.remove(this);
        send(account.getPlayersPacket());
    }

    public void save() {
        factory.update(this);
    }

    @Override
    public void setFighter(Fighter fighter) {

    }

    @Override
    public String getFightGm() {
        return null;
    }

    @Override
    public IdType getType() {
        return null;
    }
}
