package graviton.database.data;

import graviton.api.Data;
import graviton.common.Stats;
import graviton.core.Main;
import graviton.enums.DataType;
import graviton.enums.DatabaseType;
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

public class PlayerData extends Data<Player> {

    @Override
    public void configure() {
        super.type = DataType.PLAYER;
        super.connection = super.databaseManager.getDatabases().get(DatabaseType.LOGIN).getConnection();
    }

    @Override
    public Player load(Object object) {
        return null;
    }

    @Override
    public boolean create(Player object) {
        String baseQuery = "INSERT INTO players ( `id` , `account`, `name` , `sex` , `gfx` , `class`,`color1` , `color2` , `color3` , `spellpoints` , `capital` , `level` , `experience`, `map`,`cell`,`server`)"
                + " VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?);";
        try {
            locker.lock();
            PreparedStatement preparedStatement = this.connection.prepareStatement(baseQuery);
            preparedStatement.setInt(1, object.getId());
            preparedStatement.setInt(2, object.getAccount().getId());
            preparedStatement.setString(3, object.getName());
            preparedStatement.setInt(4, object.getSex());
            preparedStatement.setInt(5, object.getGfx());
            preparedStatement.setInt(6, object.getClasse().getId());
            preparedStatement.setInt(7, object.getColor(1));
            preparedStatement.setInt(8, object.getColor(2));
            preparedStatement.setInt(9, object.getColor(3));
            preparedStatement.setInt(10, object.getSpellPoints());
            preparedStatement.setInt(11, object.getCapital());
            preparedStatement.setInt(12, object.getLevel());
            preparedStatement.setLong(13, object.getExperience());
            preparedStatement.setInt(14, object.getPosition().getMap().getId());
            preparedStatement.setInt(15, object.getPosition().getCell().getId());
            preparedStatement.setInt(16, Main.getServerId());
            preparedStatement.execute();
            preparedStatement.close();
        } catch (SQLException e) {
            console.println(e.getMessage(), true);
            locker.unlock();
            return false;
        } finally {
            locker.unlock();
        }
        return true;
    }

    @Override
    public Player getByResultSet(ResultSet result) throws SQLException {
        int[] colors = {result.getInt("color1"), result.getInt("color2"), result.getInt("color3")};
        Map<Integer, Integer> stats = new HashMap<>();
        stats.put(Stats.ADD_VITA, result.getInt("vitalite"));
        stats.put(Stats.ADD_FORC, result.getInt("force"));
        stats.put(Stats.ADD_SAGE, result.getInt("sagesse"));
        stats.put(Stats.ADD_INTE, result.getInt("intelligence"));
        stats.put(Stats.ADD_CHAN, result.getInt("chance"));
        stats.put(Stats.ADD_AGIL, result.getInt("agilite"));

        Player player = new Player(result.getInt("id"),result.getInt("account"), result.getString("name"),
                result.getInt("sex"), result.getInt("class"), result.getInt("alignement"),
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
        try {
            locker.lock();
            String query = "DELETE FROM players WHERE id = "+ object.getId();
            connection.createStatement().execute(query);
            manager.getPlayers().remove(this);
        } catch (SQLException e) {
            console.println(e.getMessage(), true);
        } finally {
            locker.unlock();
        }
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
                if(result.getInt("server") == Main.getServerId())
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
