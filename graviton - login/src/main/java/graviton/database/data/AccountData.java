package graviton.database.data;

import com.google.inject.Inject;
import com.google.inject.Injector;
import graviton.api.Data;
import graviton.common.CryptManager;
import graviton.game.Account;
import graviton.login.Configuration;
import graviton.login.Manager;
import graviton.network.login.LoginClient;
import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by Botan on 08/07/2015.
 */
@Slf4j
public class AccountData extends Data {
    @Inject
    Injector injector;
    @Inject
    Configuration configuration;
    @Inject
    Manager manager;

    private Connection connection;

    @Override
    public void initialize() {
        this.connection =  configuration.getDatabase().getConnection();
    }

    public boolean isGood(String username, String password, final LoginClient client) {
        boolean isGood = false;
        try {
            locker.lock();
            if(connection.isClosed())
                configuration.getDatabase().connect();
            String query = "SELECT * from accounts WHERE account = '" + username + "';";
            ResultSet resultSet = connection.createStatement().executeQuery(query);
            if (resultSet.next()) {
                if (CryptManager.encrypt(resultSet.getString("password"), client.getKey()).equals(password))
                    isGood = true;
            }
            resultSet.close();
        } catch (SQLException e) {
            log.error("Exception > {}", e.getMessage());
        } finally {
            locker.unlock();
        }
        return isGood;
    }

    public final Account load(String arguments) {
        Account account = null;
        try {
            locker.lock();
            String query = "SELECT * from accounts WHERE account = '" + arguments + "';";
            ResultSet resultSet = connection.createStatement().executeQuery(query);
            if (resultSet.next()) {
                int id = resultSet.getInt("id");
                manager.checkAccount(id);
                account = new Account(id,
                        resultSet.getString("account"), resultSet.getString("password"),
                        resultSet.getString("pseudo"), resultSet.getString("question"), resultSet.getInt("rank"),injector);
            }
            resultSet.close();
        } catch (SQLException e) {
            log.error("Exception > {}", e.getMessage());
        } finally {
            locker.unlock();
        }
        return account;
    }

    public final Account load(String account,String password) {
        Account selectedAccount = null;
        try {
            locker.lock();
            String query = "SELECT * from accounts WHERE account = '" + account + "' AND password = '" + password + "';";
            ResultSet resultSet = configuration.getDatabase().getConnection().createStatement().executeQuery(query);
            if (resultSet.next()) {
                int id = resultSet.getInt("id");
                selectedAccount =  new Account(resultSet.getString("pseudo"), resultSet.getInt("rank"),injector);
            }
            resultSet.close();
        } catch (SQLException e) {
            log.error("Exception > {}", e.getMessage());
        } finally {
            locker.unlock();
        }
        return selectedAccount;
    }

    public void updateNickname(Account account) {
        try {
            locker.lock();
            String query = "UPDATE accounts SET pseudo = '" + account.getPseudo() + "' WHERE id = '" + account.getId() + "';";
            connection.prepareStatement(query).executeUpdate();
        } catch (SQLException e) {
            log.error("Exception > {}", e.getMessage());
        } finally {
            locker.unlock();
        }
    }

    public boolean isAvaiableNickname(String nickName) {
        boolean isValid = true;
        try {
            locker.lock();
            ResultSet resultSet = connection.createStatement().executeQuery("SELECT pseudo from accounts WHERE pseudo = '" + nickName + "';");
            if (resultSet.next())
                isValid = false;
            resultSet.close();
        } catch (SQLException e) {
            log.error("Exception > {}", e.getMessage());
        } finally {
            locker.unlock();
        }
        return isValid;
    }
}

