package graviton.database.data;

import graviton.api.Data;
import graviton.enums.DataType;
import graviton.enums.DatabaseType;
import graviton.zone.Zone;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * Created by Botan on 25/06/2015.
 */
public class ZoneData extends Data<Zone> {

    @Override
    public void configure() {
        super.type = DataType.ZONE;
        super.connection = super.databaseManager.getDatabases().get(DatabaseType.GAME).getConnection();
    }

    @Override
    public Zone load(Object object) {
        return null;
    }

    @Override
    public boolean create(Zone object) {
        return false;
    }

    @Override
    public Zone getByResultSet(ResultSet result) throws SQLException {
        return null;
    }

    @Override
    public void update(Zone object) {

    }

    @Override
    public void delete(Zone object) {

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
    public List<Zone> loadAll(Object object) {
        return null;
    }
}
