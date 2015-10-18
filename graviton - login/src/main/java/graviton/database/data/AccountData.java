package graviton.database.data;

import graviton.api.Data;
import graviton.common.CryptManager;
import graviton.game.Account;
import graviton.network.login.LoginClient;
import lombok.extern.slf4j.Slf4j;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by Botan on 08/07/2015.
 */
@Slf4j
public class AccountData extends Data {

    public boolean isGood(String username, String password, final LoginClient client) {
        boolean isGood = false;
        try {
            locker.lock();
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
                        resultSet.getString("pseudo"), resultSet.getString("question"), resultSet.getInt("rank"));
            }
            resultSet.close();
        } catch (SQLException e) {
            log.error("Exception > {}", e.getMessage());
        } finally {
            locker.unlock();
        }
        return account;
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

