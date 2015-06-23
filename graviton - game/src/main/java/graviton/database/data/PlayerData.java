package graviton.database.data;

import graviton.api.Data;
import graviton.common.StatsID;
import graviton.core.Main;
import graviton.database.Database;
import graviton.game.client.Account;
import graviton.game.client.player.Player;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by Botan on 21/06/2015.
 */
@SuppressWarnings("ALL")
public class PlayerData extends Data<Player> {

    public PlayerData(Database database) {
        super(database.getConnection());
    }

    @Override
    public Player load(Object object) {
        return null;
    }

    @Override
    public boolean create(Player object) {
        String baseQuery = "INSERT INTO players ( `id` , `account`, `name` , `sexe` , `gfx` , `class`,`color1` , `color2` , `color3` , `spellpoints` , `capital` , `level` , `experience` , `map`,`cell`,`server`)"
                + " VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?);";
        try {

            PreparedStatement p = this.connection.prepareStatement(baseQuery);
            p.setInt(1, object.getId());
            p.setInt(2, object.getAccount().getId());
            p.setString(3, object.getName());
            p.setInt(4, object.getSex());
            p.setInt(5, object.getGfx());
            p.setInt(6, object.getClasse().getId());
            p.setInt(7, object.getColor(1));
            p.setInt(8, object.getColor(2));
            p.setInt(9, object.getColor(3));
            p.setInt(10, object.getSpellPoints());
            p.setInt(11, object.getCapital());
            p.setInt(12, object.getLevel());
            p.setLong(13, object.getExperience());
            p.setInt(14, object.getPosition().getFirst().getId());
            p.setInt(15, object.getPosition().getSecond().getId());
            p.setInt(16, Main.getServerId());
            p.execute();
            p.close();
        } catch (SQLException e) {
            console.println(e.getMessage(), true);
            return false;
        }
        return true;
    }

    @Override
    public Player getByResultSet(ResultSet result) throws SQLException {
        int[] colors = {result.getInt("color1"), result.getInt("color2"), result.getInt("color3")};
        Map<Integer, Integer> stats = new HashMap<>();
        stats.put(StatsID.STATS_ADD_VITA, result.getInt("vitalite"));
        stats.put(StatsID.STATS_ADD_FORC, result.getInt("force"));
        stats.put(StatsID.STATS_ADD_SAGE, result.getInt("sagesse"));
        stats.put(StatsID.STATS_ADD_INTE, result.getInt("intelligence"));
        stats.put(StatsID.STATS_ADD_CHAN, result.getInt("chance"));
        stats.put(StatsID.STATS_ADD_AGIL, result.getInt("agilite"));

        Player player = new Player(result.getInt("id"), result.getString("name"),
                result.getInt("sexe"), result.getInt("class"), result.getInt("alignement"),
                result.getInt("honor"), result.getInt("deshonor"), result.getInt("level"),
                result.getInt("gfx"), colors, result.getLong("experience"), result.getInt("size"),
                stats, result.getLong("kamas"), result.getInt("capital"), result.getInt("spellpoints"),
                result.getInt("map"), result.getInt("cell"));
        manager.getPlayers().put(player.getId(), player);
        return player;
    }

    @Override
    public void update(Player object) {

    }

    @Override
    public void delete(Player object) {

    }

    @Override
    public List<Player> loadAll(Object object) {
        List<Player> players = new CopyOnWriteArrayList<>();
        final Account account = (Account) object;
        try {
            locker.lock();
            String query = "SELECT * FROM players WHERE account =" + account.getId();
            ResultSet result = connection.createStatement().executeQuery(query);
            while (result.next())
                players.add(getByResultSet(result));
            players.stream().forEach(player -> player.setAccount(account));
            result.close();
        } catch (SQLException e) {
            console.println(e.getMessage(), true);
        } finally {
            locker.unlock();
        }
        return players;
    }

    @Override
    public boolean exist(Object object) {
        boolean exist = false;
        try {
            locker.lock();
            String query = "SELECT * FROM players WHERE name = '" + object.toString() + "'";
            ResultSet result = connection.createStatement().executeQuery(query);
            exist = result.next();
            console.addToLogs("[PlayerData] > check if player exist [" + query + "]", false);
            result.close();
        } catch (SQLException e) {
            console.println(e.getMessage(), true);
        } finally {
            locker.unlock();
        }
        return exist;
    }

    @Override
    public int getNextId() {
        int id = -1;
        try {
            locker.lock();
            String query = "SELECT MAX(id) AS max FROM players;";
            ResultSet result = this.connection.createStatement().executeQuery(query);
            id = result.next() ? result.getInt("max") + 1 : -1;
            result.close();
        } catch (Exception e) {

        } finally {
            locker.unlock();
        }
        return id;
    }
}
