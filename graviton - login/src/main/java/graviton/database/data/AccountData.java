package graviton.database.data;

import graviton.api.Data;
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
    private char[] HASH = {'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l',
            'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', 'A',
            'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P',
            'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '0', '1', '2', '3', '4',
            '5', '6', '7', '8', '9', '-', '_'};

    public boolean isGood(String username, String password, final LoginClient client) {
        boolean isGood = false;
        try {
            locker.lock();
            String query = "SELECT * from accounts WHERE account = '" + username + "';";
            ResultSet resultSet = connection.createStatement().executeQuery(query);
            if (resultSet.next()) {
                if (crypt(resultSet.getString("password"), client.getKey()).equals(password))
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
                if (login.getAccounts().get(id) != null) {
                    login.getAccounts().get(id).getClient().send("AlEa");
                    login.getAccounts().get(id).getClient().getSession().close(true);
                }
                if (login.getConnected().get(id) != null) {
                    login.getServers().get(login.getConnected().get(id)).send("-" + id);
                    login.getConnected().remove(id);
                }
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

    private final String crypt(String pass, String key) {
        int i = HASH.length;
        StringBuilder crypted = new StringBuilder("#1");
        for (int y = 0; y < pass.length(); y++) {
            char c1 = pass.charAt(y);
            char c2 = key.charAt(y);
            double d = Math.floor(c1 / 16);
            int j = c1 % 16;
            crypted.append(HASH[(int) ((d + c2 % i) % i)]).append(HASH[(j + c2 % i) % i]);
        }
        return crypted.toString();
    }


}

