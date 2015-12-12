package graviton.database;

import com.google.inject.Inject;
import com.google.inject.Injector;
import graviton.api.Manager;
import graviton.core.Configuration;
import graviton.game.GameManager;
import graviton.game.alignement.Alignement;
import graviton.game.client.Account;
import graviton.game.client.player.Player;
import graviton.game.common.Action;
import graviton.game.common.Stats;
import graviton.game.creature.monster.MonsterTemplate;
import graviton.game.enums.Classe;
import graviton.game.enums.StatsType;
import graviton.game.experience.Experience;
import graviton.game.maps.Maps;
import graviton.game.maps.Zaap;
import graviton.game.maps.object.InteractiveObjectTemplate;
import graviton.game.object.Object;
import graviton.game.object.ObjectTemplate;
import graviton.game.spells.SpellTemplate;
import graviton.game.statistics.Statistics;
import graviton.game.zone.SubZone;
import graviton.game.zone.Zone;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by Botan on 16/06/2015.
 */
@Slf4j
public class DatabaseManager implements Manager {
    @Inject
    Injector injector;

    final private ReentrantLock locker = new ReentrantLock();

    @Getter
    private final Database loginDatabase, gameDatabase;

    private GameManager manager;
    private Configuration config;


    @Inject
    public DatabaseManager(Configuration config) {
        this.loginDatabase = config.getLoginDatabase();
        this.gameDatabase = config.getGameDatabase();
        this.config = config;
    }

    /**
     * #Account
     **/
    public Account loadAccount(int id) {
        Account account = null;
        try {
            locker.lock();
            String query = "SELECT * from accounts WHERE id = " + id + ";";
            ResultSet result = getLogin().createStatement().executeQuery(query);
            if (result.next())
                account = new Account(result.getInt("id"), result.getString("answer"), result.getString("pseudo"), result.getInt("rank"),injector);
            result.close();
        } catch (SQLException e) {
            log.error("load account {}", e);
        } finally {
            locker.unlock();
        }
        return account;
    }

    public void updateAccount(Account account) {

    }

    /**
     * #Player
     **/
    public boolean createPlayer(Player player) {
        String baseQuery = "INSERT INTO players ( `id` , `account`, `name` , `sex` , `gfx` , `class`,`colors` , `spells`, `spellpoints` , `capital` , `level` , `experience`,`position`,`server`)"
                + " VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?);";
        try {
            locker.lock();
            PreparedStatement preparedStatement = getLogin().prepareStatement(baseQuery);
            preparedStatement.setInt(1, player.getId());
            preparedStatement.setInt(2, player.getAccount().getId());
            preparedStatement.setString(3, player.getName());
            preparedStatement.setInt(4, player.getSex());
            preparedStatement.setInt(5, player.getGfx());
            preparedStatement.setInt(6, player.getClasse().getId());
            preparedStatement.setString(7, player.getColor(1) + ";" + player.getColor(2) + ";" + player.getColor(3));
            preparedStatement.setString(8, player.parseSpells());
            preparedStatement.setInt(9, player.getSpellPoints());
            preparedStatement.setInt(10, player.getCapital());
            preparedStatement.setInt(11, player.getLevel());
            preparedStatement.setLong(12, player.getExperience());
            preparedStatement.setString(13, player.getMap().getId() + ";" + player.getPosition().getCell().getId());
            preparedStatement.setInt(14, config.getServerId());
            preparedStatement.execute();
            preparedStatement.close();
        } catch (SQLException e) {
            log.error("create player {}", e);
            return false;
        } finally {
            locker.unlock();
        }
        return true;
    }

    public List<Player> loadPlayers(Account account) {
        List<Player> players = new CopyOnWriteArrayList<>();
        try {
            locker.lock();
            String query = "SELECT * FROM players WHERE account =" + account.getId();
            ResultSet result = getLogin().createStatement().executeQuery(query);
            while (result.next())
                if (result.getInt("server") == config.getServerId())
                    players.add(getPlayer(result, account));
            result.close();
        } catch (SQLException e) {
            log.error("load players {}", e);
        } finally {
            locker.unlock();
        }
        return players;
    }

    public boolean ifPlayerExist(String name) {
        boolean exist = false;
        try {
            locker.lock();
            String query = "SELECT * FROM players WHERE name = '" + name + "'";
            ResultSet result = getLogin().createStatement().executeQuery(query);
            exist = result.next();
            result.close();
        } catch (SQLException e) {
            log.error("check if player exist {}", e);
        } finally {
            locker.unlock();
        }
        return exist;
    }

    public void deletePlayer(Player player) {
        try {
            locker.lock();
            getLogin().createStatement().execute("DELETE FROM players WHERE id = " + player.getId());
        } catch (SQLException e) {
            log.error("delete player {}", e);
        } finally {
            locker.unlock();
        }
    }

    public void updatePlayer(Player player) {
        Statistics basicStatistics = player.getStatistics().get(StatsType.BASE);
        String statistics = basicStatistics.getEffect(Stats.ADD_VITA) + ";" + basicStatistics.getEffect(Stats.ADD_FORC) + ";" +
                basicStatistics.getEffect(Stats.ADD_SAGE) + ";" +
                basicStatistics.getEffect(Stats.ADD_INTE) + ";" +
                basicStatistics.getEffect(Stats.ADD_CHAN) + ";" +
                basicStatistics.getEffect(Stats.ADD_AGIL);
        Alignement alignement = player.getAlignement();
        String alignementToString = alignement.getType().getId() + ";" + alignement.getHonor() + ";" + alignement.getDeshonnor() + ";" + (alignement.isShowWings() ? 1 : 0);

        String baseQuery = "UPDATE `players` SET"
                + " `name`= ?,"
                + " `sex`= ?,"
                + " `gfx`= ?,"
                + " `colors`= ?,"
                + " `spells`= ?,"
                + " `statistics`= ?,"
                + " `capital`= ?,"
                + " `spellPoints`= ?,"
                + " `items`= ?,"
                + " `position`= ?,"
                + " `alignement`= ?"
                + " WHERE id = ?;";
        try {
            locker.lock();
            PreparedStatement preparedStatement = getLogin().prepareStatement(baseQuery);
            preparedStatement.setString(1, player.getName());
            preparedStatement.setInt(2, player.getSex());
            preparedStatement.setInt(3, player.getGfx());
            preparedStatement.setString(4, player.getColor(1) + ";" + player.getColor(2) + ";" + player.getColor(3));
            preparedStatement.setString(5, player.parseSpells());
            preparedStatement.setString(6, statistics);
            preparedStatement.setInt(7, player.getCapital());
            preparedStatement.setInt(8, player.getSpellPoints());
            preparedStatement.setString(9, player.parseItem());
            preparedStatement.setString(10, player.getMap().getId() + ";" + player.getCell().getId());
            preparedStatement.setString(11, alignementToString);
            preparedStatement.setInt(12, player.getId());
            preparedStatement.executeUpdate();
            preparedStatement.close();
        } catch (Exception e) {
            log.error("update player {}", e);
        } finally {
            locker.unlock();
        }
    }

    public int getNextPlayerId() {
        int id = -1;
        try {
            locker.lock();
            String query = "SELECT MAX(id) AS max FROM players;";
            ResultSet result = getLogin().createStatement().executeQuery(query);
            id = result.next() ? result.getInt("max") + 1 : -1;
            result.close();
        } catch (Exception e) {
            log.error("get next player id {}", e);
        } finally {
            locker.unlock();
        }
        return id;
    }

    private Player getPlayer(ResultSet result, Account account) throws SQLException {
        String colors[] = result.getString("colors").split(";");
        int[] finalColor = {Integer.parseInt(colors[0]), Integer.parseInt(colors[1]), Integer.parseInt(colors[2])};

        String[] statistics = result.getString("statistics").split(";");
        Map<Integer, Integer> stats = new HashMap<>();
        stats.put(Stats.ADD_VITA, Integer.parseInt(statistics[0]));
        stats.put(Stats.ADD_FORC, Integer.parseInt(statistics[1]));
        stats.put(Stats.ADD_SAGE, Integer.parseInt(statistics[2]));
        stats.put(Stats.ADD_INTE, Integer.parseInt(statistics[3]));
        stats.put(Stats.ADD_CHAN, Integer.parseInt(statistics[4]));
        stats.put(Stats.ADD_AGIL, Integer.parseInt(statistics[5]));

        String[] position = result.getString("position").split(";");
        String[] alignement = result.getString("alignement").split(";");
        return new Player(result.getInt("id"), account, result.getString("name"),
                result.getInt("sex"), result.getInt("class"), Integer.parseInt(alignement[0]),
                Integer.parseInt(alignement[1]), Integer.parseInt(alignement[2]), Integer.parseInt(alignement[3]) == 1, result.getInt("level"),
                result.getInt("gfx"), finalColor, result.getLong("experience"), result.getInt("size"),
                stats, result.getString("items"), result.getLong("kamas"), result.getInt("capital"), result.getString("spells"), result.getInt("spellpoints"),
                Integer.parseInt(position[0]), Integer.parseInt(position[1]), injector);
    }

    /**
     * #Maps
     **/

    public Maps loadMap(int id) {
        Maps map = null;
        try {
            locker.lock();
            String query = "SELECT * FROM maps WHERE id = " + id;
            ResultSet mapResult = getGame().createStatement().executeQuery(query);
            if (mapResult.next())
                map = getMap(mapResult);
            mapResult.close();
            if (map != null)
                configureCell(map);
        } catch (SQLException e) {
            log.error("load map {}", e);
        } finally {
            locker.unlock();
        }
        return map;
    }

    public Maps loadMapByPosition(int x1, int y1) {
        Maps map = null;
        try {
            locker.lock();
            ResultSet result = getGame().createStatement().executeQuery("SELECT id, mappos FROM maps");
            while (result.next()) {
                String[] mappos = result.getString("mappos").split(",");
                int x2 = -1, y2 = -1;
                try {
                    x2 = Integer.parseInt(mappos[0]);
                    y2 = Integer.parseInt(mappos[1]);
                } catch (Exception e) {
                }
                if (x1 == x2 && y1 == y2) {
                    map = manager.getMap(result.getShort("id"));
                    break;
                }
            }
            return map;
        } catch (SQLException e) {
            log.error("load map by position {}", e);
        } finally {
            locker.unlock();
        }
        return null;
    }

    private void configureCell(Maps map) throws SQLException {
        String query = "SELECT * FROM cells WHERE map = " + map.getId();
        ResultSet resultSet = getGame().createStatement().executeQuery(query);
        while (resultSet.next())
            map.getCells().get(resultSet.getInt("cell")).setAction(new Action(resultSet.getInt("action"), resultSet.getString("args")));
        resultSet.close();
    }

    public Maps getMap(ResultSet result) throws SQLException {
        Maps map = new Maps(result.getInt("id"), result.getLong("date"),
                result.getInt("width"), result.getInt("heigth"),
                result.getString("places"), result.getString("key"),
                result.getString("mapData"), result.getString("mappos"),injector);
        manager.getMaps().put(map.getId(), map);
        return map;
    }

    /**
     * #Object
     **/

    public ObjectTemplate loadObjectTemplate(int id) {
        ObjectTemplate template = null;
        try {
            locker.lock();
            String query = "SELECT * FROM item_template WHERE id = " + id;
            ResultSet resultSet = getGame().createStatement().executeQuery(query);
            if (resultSet.next())
                template = getObject(resultSet);
            resultSet.close();
        } catch (SQLException e) {
            log.error("load object template {}", e);
        } finally {
            locker.unlock();
        }
        return template;
    }

    public Object loadObject(int id) {
        Object object = null;
        try {
            locker.lock();
            String query = "SELECT * FROM items WHERE id = " + id;
            ResultSet resultSet = getGame().createStatement().executeQuery(query);

            if (resultSet.next()) {
                int template = resultSet.getInt("template");
                int quantity = resultSet.getInt("quantity");
                int position = resultSet.getInt("position");
                String stats = resultSet.getString("stats");
                object = new Object(id, template, quantity, position, stats,injector);
                manager.getObjects().put(id, object);
            }
            resultSet.close();
        } catch (SQLException e) {
            log.error("load object {}", e);
        } finally {
            locker.unlock();
        }
        return object;
    }

    public boolean createObject(Object obj) {
        try {
            String baseQuery = "INSERT INTO `items` VALUES(?,?,?,?,?);";

            PreparedStatement statement = getGame().prepareStatement(baseQuery);

            statement.setInt(1, obj.getId());
            statement.setInt(2, obj.getTemplate().getId());
            statement.setInt(3, obj.getQuantity());
            statement.setInt(4, obj.getPosition().id);
            statement.setString(5, obj.parseEffects());

            statement.executeUpdate();
            statement.close();
            return true;
        } catch (Exception e) {
            log.error("create object {}", e);
        }
        return false;
    }

    public void updateObject(Object object) {
        String baseQuery = "UPDATE `items` SET"
                + " `quantity`= ?,"
                + " `position`= ?,"
                + " `stats`= ?"
                + " WHERE id = ?;";
        try {
            locker.lock();
            PreparedStatement preparedStatement = getGame().prepareStatement(baseQuery);
            preparedStatement.setInt(1, object.getQuantity());
            preparedStatement.setInt(2, object.getPosition().id);
            preparedStatement.setString(3, object.parseEffects());
            preparedStatement.setInt(4, object.getId());
            preparedStatement.executeUpdate();
            preparedStatement.close();
        } catch (Exception e) {
            log.error("update object {}", e);
        } finally {
            locker.unlock();
        }
    }

    public int getNextObjectId() {
        int id = -1;
        try {
            locker.lock();
            String query = "SELECT MAX(id) AS max FROM items;";
            ResultSet result = getGame().createStatement().executeQuery(query);
            id = result.next() ? result.getInt("max") + 1 : -1;
            result.close();
        } catch (Exception e) {
            log.error("get next object id {}", e);
        } finally {
            locker.unlock();
        }
        return id;
    }

    private ObjectTemplate getObject(ResultSet result) throws SQLException {
        return new ObjectTemplate(result.getInt("id"), result.getInt("type"), result.getString("name"),
                result.getInt("level"), result.getString("statsTemplate"), result.getInt("pod"),
                result.getInt("panoplie"), result.getInt("price"), result.getString("condition"),
                result.getString("armeInfos"),injector);
    }

    /**
     * #Monster
     **/

    public MonsterTemplate loadMonsterOnMap(int id) {
        MonsterTemplate monsterTemplate = null;
        try {
            locker.lock();
            String query = "SELECT * FROM monsters WHERE id = " + id;
            ResultSet result = getGame().createStatement().executeQuery(query);
            if(result.next())
                monsterTemplate = new MonsterTemplate(result.getInt("id"), result.getInt("gfxID"));

            result.close();
        } catch (SQLException e) {
            log.error("load account {}", e);
        } finally {
            locker.unlock();
        }
        return monsterTemplate;
    }

    /**
     * #Static
     **/

    public void loadData(GameManager manager) {
        this.manager = manager;
        try {
            locker.lock();
            loadZone();
            loadClassData();
            loadExperience();
            loadSpells();
            loadInteractiveObject();
            loadZaaps();
        } catch (SQLException e) {
            log.error("load all data {}", e);
        } finally {
            locker.unlock();
        }
    }

    private void loadZone() throws SQLException {
        ResultSet result = getGame().createStatement().executeQuery("SELECT * FROM zone;");
        while (result.next())
            manager.getZones().put(result.getInt("id"), (new Zone(result.getInt("id"), result.getString("name"))));
        result = getGame().createStatement().executeQuery("SELECT * FROM subzone;");
        while (result.next())
            manager.getSubZones().put(result.getInt("id"), new SubZone(result.getInt("id"), result.getString("name"), manager.getZones().get(result.getInt("id")), result.getInt("alignement")));
        result.close();
    }

    private void loadZaaps() throws SQLException {
        ResultSet result = getGame().createStatement().executeQuery("SELECT * FROM zaaps;");
        while (result.next())
            manager.getZaaps().add(new Zaap(manager.getMap(result.getInt("map")), result.getInt("cell")));
        result.close();
    }

    private void loadClassData() throws SQLException {
        for(Classe classe : Classe.values())
            combine(classe,classe.getSpells());
    }

    private void combine(Classe classe, int[] spells) {
        final int[] level = {3  ,6  ,9  ,13 ,17 ,21 ,26 ,31 ,36 ,42 ,48 ,54 ,60 ,70 ,80 ,90 ,100,200};
        Map<Integer,Integer> map = new LinkedHashMap<>();
        int count = 0;
        for(int i : level) {
            map.put(i,spells[count]);
            count++;
        }
        manager.getClassData().put(classe,map);
    }

    private void loadExperience() throws SQLException {
        ResultSet result = getGame().createStatement().executeQuery("SELECT  * from experience");
        int level;
        Map<Integer, Long> player = new HashMap<>();
        Map<Integer, Long> pvp = new HashMap<>();
        Map<Integer, Long> mount = new HashMap<>();
        Map<Integer, Long> job = new HashMap<>();
        while (result.next()) {
            level = result.getInt("level");
            player.put(level, result.getLong("player"));
            pvp.put(level, result.getLong("pvp"));
            mount.put(level, result.getLong("mount"));
            job.put(level, result.getLong("job"));
        }
        result.close();
        manager.setExperience(new Experience(player, job, mount, pvp));
    }

    private void loadSpells() throws SQLException {
        ResultSet result = getGame().createStatement().executeQuery("SELECT * FROM spells");
        Map<Integer, SpellTemplate> spells = new ConcurrentHashMap<>();
        SpellTemplate spellTemplate;
        while (result.next()) {
            spellTemplate = new SpellTemplate(result.getInt("id"), result.getInt("sprite"), result.getString("spriteInfos"),result);
            spells.put(spellTemplate.getId(), spellTemplate);
        }
        result.close();
        manager.setSpellTemplates(spells);
    }

    private void loadInteractiveObject() throws SQLException {
        ResultSet result = getGame().createStatement().executeQuery("SELECT * FROM interactive_template");
        InteractiveObjectTemplate template;
        while (result.next()) {
            template = new InteractiveObjectTemplate(result.getInt("id"), result.getInt("respawn"), result.getInt("duration"),
                    result.getInt("unknow"), result.getInt("walkable") == 1);
            manager.getInteractiveObjectTemplates().put(template.getId(), template);
        }
    }

    @Override
    public void load() {
        loginDatabase.configure();
        gameDatabase.configure();
    }

    @Override
    public void unload() {
        loginDatabase.stop();
        gameDatabase.stop();
    }

    private Connection getLogin() {
        return this.loginDatabase.getConnection();
    }

    private Connection getGame() {
        return this.gameDatabase.getConnection();
    }


}
