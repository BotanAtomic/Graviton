package graviton.database.data;

import graviton.api.Data;
import graviton.database.Database;
import graviton.enums.DataType;
import graviton.enums.DatabaseType;
import graviton.game.client.Account;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * Created by Botan on 21/06/2015.
 */
public class AccountData extends Data<Account> {

    @Override
    public void configure() {
        super.type = DataType.ACCOUNT;
        super.connection = super.databaseManager.getDatabases().get(DatabaseType.LOGIN).getConnection();
    }

    @Override
    public Account load(Object object) {
        Account account = null;
        try {
            locker.lock();
            String query = object instanceof String ?
                    "SELECT * from accounts WHERE account = " + object.toString() + ";" :
                    "SELECT * from accounts WHERE id = " + object.toString() + ";";
            console.addToLogs("[AccountData] > load account [" + query + "]", false);
            ResultSet result = connection.createStatement().executeQuery(query);
            if (result.next())
                account = getByResultSet(result);
            result.close();
        } catch (SQLException e) {
            console.println(e.getMessage(), true);
        } finally {
            locker.unlock();
        }
        return account;
    }

    @Deprecated
    public boolean create(Account object) {
        return false;
    }

    @Override
    public Account getByResultSet(ResultSet result) throws SQLException {
        final Account account = new Account(result.getInt("id"),result.getString("answer"));
        manager.getAccounts().put(account.getId(), account);
        account.loadPlayers();
        return account;
    }

    @Override
    public void update(Account object) {

    }

    @Deprecated
    public void delete(Account object) {

    }

    @Deprecated
    public boolean exist(Object object) {
        return false;
    }

    @Deprecated
    public int getNextId() {
        return 0;
    }

    @Deprecated
    public List<Account> loadAll(Object object) {
        return null;
    }
}
