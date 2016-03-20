package graviton.game;

import com.google.inject.Inject;
import graviton.api.Factory;
import graviton.api.Manager;
import graviton.enums.DataType;
import graviton.factory.*;
import graviton.game.admin.Admin;
import graviton.game.client.Account;
import graviton.game.client.player.Player;
import graviton.game.creature.monster.MonsterTemplate;
import graviton.game.creature.npc.NpcAnswer;
import graviton.game.creature.npc.NpcQuestion;
import graviton.game.creature.npc.NpcTemplate;
import graviton.game.enums.Classe;
import graviton.game.experience.Experience;
import graviton.game.maps.Maps;
import graviton.game.maps.Zaap;
import graviton.game.maps.object.InteractiveObjectTemplate;
import graviton.game.object.Object;
import graviton.game.object.ObjectTemplate;
import graviton.game.spells.Animation;
import graviton.game.spells.SpellTemplate;
import graviton.game.trunks.Trunk;
import graviton.network.exchange.ExchangeNetwork;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by Botan on 19/06/2015.
 */
@Data
@Slf4j
public class GameManager implements Manager {

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final ReentrantLock locker = new ReentrantLock();

    @Inject
    PlayerFactory playerFactory;
    @Inject
    MapFactory mapFactory;
    @Inject
    AccountFactory accountFactory;
    @Inject
    ObjectFactory objectFactory;
    @Inject
    SpellFactory spellFactory;
    @Inject
    NpcFactory npcFactory;
    @Inject
    GuildFactory guildFactory;
    @Inject
    MonsterFactory monsterFactory;

    private ExchangeNetwork exchangeNetwork;

    private Experience experience;

    private Map<DataType, Factory<?>> factorys;

    private Map<Classe, Map<Integer, Integer>> classData;

    private List<Admin> admins;

    @Inject
    public GameManager() {
        this.admins = new ArrayList<>();
    }

    @Override
    public void load() {
        this.factorys = getFactorys(playerFactory, npcFactory, accountFactory, objectFactory, spellFactory, mapFactory, guildFactory, monsterFactory);
        this.factorys.values().forEach(Factory::configure);
        this.scheduleActions();
    }

    public Map<Integer, ?> getElements(DataType type) {
        return this.factorys.get(type).getElements();
    }

    public Maps getMap(int id) {
        return mapFactory.get(id);
    }

    public Maps getMapByPosition(int x1, int y1) {
        return mapFactory.getByPosition(x1, y1);
    }

    public SpellTemplate getSpellTemplate(int id) {
        return spellFactory.get(id);
    }

    public Account getAccount(int id) {
        return accountFactory.get(id);
    }

    public ObjectTemplate getObjectTemplate(int id) {
        return objectFactory.get((java.lang.Object) id);
    }

    public InteractiveObjectTemplate getInteractiveObjectTemplates(int id) {
        return objectFactory.get(id);
    }

    public Object getObject(int id) {
        return objectFactory.load(id);
    }

    public void updateObject(Object object) {
        objectFactory.update(object);
    }

    public void saveObject(Object object) {
        objectFactory.create(object);
    }

    public void removeObject(int id) {
        objectFactory.remove(id);
    }

    public NpcTemplate getNpcTemplate(int id) {
        return npcFactory.get(id);
    }

    public NpcQuestion getNpcQuestion(int id) {
        return npcFactory.getQuestion(id);
    }

    public NpcAnswer getNpcAnswer(int id) {
        return npcFactory.getAnswer(id);
    }

    public Map<Classe, Map<Integer, Integer>> getClassData() {
        return this.playerFactory.getClassData();
    }

    public List<Zaap> getZaaps() {
        return mapFactory.getZaaps();
    }

    public boolean checkName(String name) {
        return guildFactory.check(name, true);
    }

    public boolean checkEmblem(String emblem) {
        return guildFactory.check(emblem, false);
    }

    public MonsterTemplate getMonster(int id) {
        return monsterFactory.get(id);
    }

    public void updateTrunk(Trunk trunk) {
        mapFactory.updateTrunk(trunk);
    }

    public long getPlayerExperience(int level) {
        if (level > 200) level = 200;
        return experience.getData().get(DataType.PLAYER).get(level);
    }

    public long getGuildExperience(int level) {
        if (level > 200) return -1;
        return experience.getData().get(DataType.MOUNT).get(level);
    }

    public Animation getAnimation(int id) {
        return spellFactory.getAnimations().get(id);
    }

    public long getMountExperience(int level) {
        if (level > 100) level = 100;
        return experience.getData().get(DataType.MOUNT).get(level);
    }

    public void save() {
        try {
            locker.lock();
            exchangeNetwork.send("C2");
            getOnlinePlayers().forEach(player -> player.send("Im1164"));
            this.factorys.forEach((dataType, factory) -> factory.save());
            exchangeNetwork.send("C1");
            getOnlinePlayers().forEach(player -> player.send("Im1165"));
        } finally {
            locker.unlock();
        }
    }

    public void send(String packet) {
        playerFactory.send(packet);
    }

    public List<Player> getOnlinePlayers() {
        return playerFactory.getOnlinePlayers();
    }

    public void sendToAdmins(String packet) {
        try {
            locker.lock();
            admins.forEach(admin -> admin.getAccount().send(packet));
        } finally {
            locker.unlock();
        }
    }

    public Player getPlayer(java.lang.Object object) {
        return playerFactory.get(object);
    }

    public String getAdminsName() {
        if (admins.isEmpty()) return "";
        String[] name = {" ["};
        admins.forEach(admin -> name[0] += admin.getAccount().getPseudo() + "(" + admin.getRank().id + ")/");
        return name[0].substring(0, name[0].length() - 1) + "]";
    }

    public void mute(Player muted, Player player, int time, String reason) {
        String message = "Le joueur <b>" + muted.getPacketName() + "</b> s'est fait muter " +
                "pour <b>" + time + "</b> minutes " +
                "par <b>" + player.getName() + "</b>" +
                "pour la raison suivante : <b>" + reason + "</b>";
        send("cs<font color='#000000'>" + message + "</font>");
    }

    public void ban(Player muted, Player player, int time, String reason) {

    }

    private Map<DataType, Factory<?>> getFactorys(Factory... a) {
        Map<DataType, Factory<?>> factorys = new ConcurrentHashMap<>();
        for (Factory factory : a)
            factorys.put(factory.getType(), factory);
        return factorys;
    }

    @Override
    public void unload() {

    }

    private void scheduleActions() {
        this.scheduler.scheduleAtFixedRate(() -> save(), 1, 1, TimeUnit.HOURS);
    }
}
