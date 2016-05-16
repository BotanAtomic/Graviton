package graviton.game;

import com.google.inject.Inject;
import graviton.api.Factory;
import graviton.api.Manager;
import graviton.enums.DataType;
import graviton.factory.FactoryManager;
import graviton.factory.type.*;
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
import graviton.game.trunk.Trunk;
import graviton.network.exchange.ExchangeNetwork;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by Botan on 19/06/2015.
 */
@Data
@Slf4j
public class GameManager implements Manager {
    private boolean started = false;

    @Inject
    FactoryManager factoryManager;

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final ScheduledExecutorService jobScheduler = Executors.newScheduledThreadPool(1);

    private ExchangeNetwork exchangeNetwork;

    private Experience experience;

    private Map<Classe, Map<Integer, Integer>> classData;

    private List<Admin> admins;

    @Inject
    public GameManager(graviton.core.Manager manager) {
        manager.add(this);
        this.admins = new ArrayList<>();
    }

    @Override
    public void load() {
        this.factoryManager.getFactorys().values().forEach(Factory::configure);
        log.info("{} factorys configured",this.factoryManager.getFactorys().size());
        this.scheduleActions();
    }

    public Player getPlayer(java.lang.Object object) {
        if (object instanceof Integer)
            return getPlayerFactory().get(object);
        else
            return getPlayerFactory().getByName((String) object);
    }

    public Zaap getZaap(int map) {
        return getMapFactory().getZaap(map);
    }

    public Map<Integer, ?> getElements(DataType type) {
        return this.factoryManager.get(type).getElements();
    }

    public Maps getMap(int id) {
        return getMapFactory().get(id);
    }

    public Maps getMapByPosition(int x1, int y1) {
        return getMapFactory().getByPosition(x1, y1);
    }

    public SpellTemplate getSpellTemplate(int id) {
        return getSpellFactory().get(id);
    }

    public Account getAccount(int id) {
        return getAccountFactory().get(id);
    }

    public ObjectTemplate getObjectTemplate(int id) {
        return getObjectFactory().get((java.lang.Object) id);
    }

    public InteractiveObjectTemplate getInteractiveObjectTemplates(int id) {
        return getObjectFactory().get(id);
    }

    public Object getObject(int id) {
        return getObjectFactory().load(id);
    }

    public void updateObject(Object object) {
        getObjectFactory().update(object);
    }

    public void saveObject(Object object) {
        getObjectFactory().create(object);
    }

    public void removeObject(int id) {
        getObjectFactory().remove(id);
    }

    public NpcTemplate getNpcTemplate(int id) {
        return getNpcFactory().get(id);
    }

    public NpcQuestion getNpcQuestion(int id) {
        return getNpcFactory().getQuestion(id);
    }

    public NpcAnswer getNpcAnswer(int id) {
        return getNpcFactory().getAnswer(id);
    }

    public Map<Classe, Map<Integer, Integer>> getClassData() {
        return getPlayerFactory().getClassData();
    }

    public List<Zaap> getZaaps() {
        return getMapFactory().getZaaps();
    }

    public boolean checkName(String name) {
        if(getGuildFactory() == null) {
            System.err.println("OKI");
            return false;
        }
        return getGuildFactory().check(name, true);
    }

    public boolean checkEmblem(String emblem) {
        return getGuildFactory().check(emblem, false);
    }

    public MonsterTemplate getMonster(int id) {
        return getMonsterFactory().get(id);
    }

    public void updateTrunk(Trunk trunk) {
        getMapFactory().updateTrunk(trunk);
    }

    public long getPlayerExperience(int level) {
        if (level > 200) level = 200;
        return experience.getData().get(DataType.PLAYER).get(level);
    }

    public long getGuildExperience(int level) {
        if (level > 200) level = 200;
        return experience.getData().get(DataType.GUILD).get(level);
    }

    public long getMountExperience(int level) {
        if (level > 100) level = 100;
        return experience.getData().get(DataType.MOUNT).get(level);
    }

    public long getJobExperience(int level) {
        if (level > 100) level = 100;
        return experience.getData().get(DataType.JOB).get(level);
    }

    public void save() {
        exchangeNetwork.send("C2");
        send("Im1164");
        this.factoryManager.getFactorys().values().forEach(Factory::save);
        exchangeNetwork.send("C1");
        send("Im1165");
    }

    public void send(String packet) {
        getPlayerFactory().send(packet);
    }

    public String getAdminsName() {
        if (admins.isEmpty()) return "";
        String[] name = {" ["};
        admins.forEach(admin -> name[0] += admin.getAccount().getPseudo() + "(" + admin.getRank().id + ")/");
        return name[0].substring(0, name[0].length() - 1) + "]";
    }

    @Override
    public void unload() {
        save();
    }

    private void scheduleActions() {
        this.scheduler.scheduleAtFixedRate(() -> save(), 1, 1, TimeUnit.HOURS);

        this.scheduler.scheduleAtFixedRate(() -> getAccountFactory().getElements().values().forEach(account -> {
            if ((((System.currentTimeMillis() - account.getClient().getSession().getLastWriteTime()) / 1000) > 550)) {
                account.send("M01|");
                account.getClient().getSession().close(true);
            }
        }), 10, 10, TimeUnit.MINUTES);

        log.info("scheduled actions loaded");
    }


    public MapFactory getMapFactory() {
        return (MapFactory) this.factoryManager.get(DataType.MAPS);
    }

    public ObjectFactory getObjectFactory() {
        return (ObjectFactory) this.factoryManager.get(DataType.OBJECT);
    }

    private PlayerFactory getPlayerFactory() {
        return (PlayerFactory) this.factoryManager.get(DataType.PLAYER);
    }

    private SpellFactory getSpellFactory() {
        return (SpellFactory) this.factoryManager.get(DataType.SPELL);
    }

    private AccountFactory getAccountFactory() {
        return (AccountFactory) this.factoryManager.get(DataType.ACCOUNT);
    }

    private NpcFactory getNpcFactory() {
        return (NpcFactory) this.factoryManager.get(DataType.NPC);
    }

    public GuildFactory getGuildFactory() {
        return (GuildFactory) this.factoryManager.get(DataType.GUILD);
    }

    private MonsterFactory getMonsterFactory() {
        return (MonsterFactory) this.factoryManager.get(DataType.MONSTER);
    }

    public JobFactory getJobFactory() {
        return (JobFactory) this.factoryManager.get(DataType.JOB);
    }

}
