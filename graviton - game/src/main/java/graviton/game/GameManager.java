package graviton.game;

import com.google.inject.Inject;
import graviton.api.Manager;
import graviton.database.DatabaseManager;
import graviton.enums.DataType;
import graviton.game.admin.Admin;
import graviton.game.client.Account;
import graviton.game.client.player.Player;
import graviton.game.enums.Classe;
import graviton.game.experience.Experience;
import graviton.game.maps.Maps;
import graviton.game.maps.Zaap;
import graviton.game.maps.object.InteractiveObjectTemplate;
import graviton.game.object.Object;
import graviton.game.object.ObjectTemplate;
import graviton.game.spells.SpellTemplate;
import graviton.game.zone.SubZone;
import graviton.game.zone.Zone;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by Botan on 19/06/2015.
 */
@Data
@Slf4j
public class GameManager implements Manager {
    private final ReentrantLock locker = new ReentrantLock();

    @Inject
    private DatabaseManager databaseManager;

    private Experience experience;

    private Map<Classe, Map<Integer, Integer>> classData;

    private Map<Integer, Account> accounts;
    private Map<Integer, Player> players;

    private Map<Integer, Maps> maps;
    private Map<Integer, Zone> zones;
    private Map<Integer, SubZone> subZones;
    private Map<Integer, SpellTemplate> spellTemplates;
    private Map<Integer, ObjectTemplate> objectTemplates;
    private Map<Integer, Object> objects;
    private Map<Integer, InteractiveObjectTemplate> interactiveObjectTemplates;

    private List<Admin> admins;
    private List<Zaap> zaaps;

    @Override
    public void load() {
        this.accounts = new ConcurrentHashMap<>();
        this.players = new ConcurrentHashMap<>();
        this.maps = new ConcurrentHashMap<>();
        this.zones = new ConcurrentHashMap<>();
        this.subZones = new ConcurrentHashMap<>();
        this.classData = new ConcurrentHashMap<>();
        this.objectTemplates = new ConcurrentHashMap<>();
        this.objects = new ConcurrentHashMap<>();
        this.zaaps = new CopyOnWriteArrayList<>();
        this.interactiveObjectTemplates = new ConcurrentHashMap<>();
        this.admins = new ArrayList<>();
        databaseManager.loadData(this);
    }

    public Maps getMap(int id) {
        if (!maps.containsKey(id))
            return databaseManager.loadMap(id);
        return maps.get(id);
    }

    public ObjectTemplate getObjectTemplate(int id) {
        if (!objectTemplates.containsKey(id))
            return databaseManager.loadObjectTemplate(id);
        return objectTemplates.get(id);
    }

    public Object getObject(int id) {
        if (!objects.containsKey(id))
            return databaseManager.loadObject(id);
        return objects.get(id);
    }

    public long getPlayerExperience(int level) {
        if (level > 200) level = 200;
        return experience.getData().get(DataType.PLAYER).get(level);
    }

    public long getMountExperience(int level) {
        if (level > 100) level = 100;
        return experience.getData().get(DataType.MOUNT).get(level);
    }

    public void save() {

    }

    public void send(String packet) {
        locker.lock();
        getOnlinePlayers().forEach(player -> player.send(packet));
        locker.unlock();
    }

    public List<Player> getOnlinePlayers() {
        List<Player> onlinePlayers = new ArrayList<>();
        players.values().stream().filter(Player::isOnline).forEach(onlinePlayers::add);
        return onlinePlayers;
    }

    public void sendToPlayers(String packet) {
        locker.lock();
        getOnlinePlayers().forEach(player -> player.getAccount().send(packet));
        locker.unlock();
    }

    public void sendToAdmins(String packet) {
        locker.lock();
        admins.forEach(admin -> admin.getAccount().send(packet));
        locker.unlock();
    }

    public Player getPlayer(String name) {
        final Player[] player = {null};
        this.players.values().stream().filter(player1 -> player1.getName().equals(name)).forEach(playerSelected -> player[0] = playerSelected);
        return player[0];
    }

    public Player getPlayer(int id) {
        final Player[] player = {null};
        this.players.values().stream().filter(player1 -> player1.getId() == id).forEach(playerSelected -> player[0] = playerSelected);
        return player[0];
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
        sendToPlayers("cs<font color='#000000'>" + message + "</font>");
    }

    public void ban(Player muted, Player player, int time, String reason) {

    }

    @Override
    public void unload() {
        /** Usuless **/
    }
}
