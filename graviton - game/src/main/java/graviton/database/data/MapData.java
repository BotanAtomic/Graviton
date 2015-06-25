package graviton.database.data;

import graviton.api.Data;
import graviton.common.Action;
import graviton.database.Database;
import graviton.enums.DataType;
import graviton.enums.DatabaseType;
import graviton.game.maps.Maps;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * Created by Botan on 22/06/2015.
 */
public class MapData extends Data<Maps> {

    @Override
    public void configure() {
        super.type = DataType.MAPS;
        super.connection = super.databaseManager.getDatabases().get(DatabaseType.GAME).getConnection();
    }

    @Override
    public Maps load(Object object) {
        Maps maps = null;
        try {
            locker.lock();
            String query = "SELECT * FROM maps WHERE id = " + object;
            console.addToLogs("[MapData] > load map [" + query + "]", false);
            ResultSet mapResult = connection.createStatement().executeQuery(query);
            if (mapResult.next())
                maps = getByResultSet(mapResult);
            mapResult.close();
            if (maps == null)
                console.println("[MapData] > map [" + object + "] is null", true);
            else
                loadCell(maps);
        } catch (SQLException e) {
            console.println(e.getMessage(), true);
        } finally {
            locker.unlock();
        }
        return maps;
    }

    @Override
    public boolean create(Maps object) {
        return false;
    }

    public void loadCell(Maps map) throws SQLException{
        String query = "SELECT * FROM cells WHERE map = " + map.getId();
        ResultSet resultSet = connection.createStatement().executeQuery(query);
        while(resultSet.next())
            map.getCells().get(resultSet.getInt("cell")).setAction(new Action(resultSet.getInt("action"), resultSet.getString("args")));
        resultSet.close();
    }

    @Override
    public Maps getByResultSet(ResultSet result) throws SQLException {
        final Maps map = new Maps(result.getInt("id"),result.getLong("date"),
                result.getInt("width"), result.getInt("heigth"),
                result.getString("places"),result.getString("key"),
                result.getString("mapData"));
        manager.getMaps().put(map.getId(),map);
        return map;
    }

    @Override
    public void update(Maps object) {

    }

    @Override
    public void delete(Maps object) {

    }

    @Override
    public boolean exist(Object object) {
        return false;
    }

    @Override
    public int getNextId() {
        return 0;
    }

    @Override
    public List<Maps> loadAll(Object object) {
        return null;
    }
}
