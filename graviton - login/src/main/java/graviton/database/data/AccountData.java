package graviton.database.data;

import graviton.console.Console;
import graviton.database.DatabaseManager;
import graviton.game.Account;
import lombok.Getter;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Botan on 08/06/2015.
 */
public class AccountData {
    private Console console;
    @Getter private Map<String,Account> accounts;
    private DatabaseManager manager;

    public AccountData(DatabaseManager manager) {
        this.accounts = new HashMap<>();
        this.manager = manager;
        this.console = manager.getConsole();
    }

    public void addAccount(Account account) {
        if (!accounts.containsValue(account))
            accounts.put(account.getName(), account);
    }

    public Account load(Object id) {
        Account account = null;
        if (id instanceof Integer) {
            console.println("[Account data] : load account by id : " + id, false);
            try {
                String query = "SELECT * FROM accounts WHERE id = " + id;
                ResultSet resultSet = manager.getConnection().createStatement().executeQuery(query);
                account = loadFromResultSet(resultSet);
                resultSet.close();
            } catch (SQLException e) {
                console.println(e.getMessage(), true);
            }
            return account;
        }
        console.println("[Account data] load account by name : " + id.toString(), false);
        accounts.values().stream().filter(acc -> acc.getName().equals(id.toString())).forEach(acc -> {
            acc.getClient().send("AlEc");
            acc.getClient().kick();
        });
        try {
            String query = "SELECT * FROM accounts WHERE account = '" + id.toString() + "';";
            ResultSet resultSet = manager.getConnection().createStatement().executeQuery(query);
            account = loadFromResultSet(resultSet);
            resultSet.close();
        } catch (SQLException e) {
            console.println(e.getMessage(), true);
        }
        return account;
    }

    public boolean alreadyExist(String nickname) {
        boolean exist = false;
        try {
            String query = "SELECT * FROM accounts WHERE pseudo = '" + nickname + "';";
            ResultSet resultSet = manager.getConnection().createStatement().executeQuery(query);
            if (resultSet.next())
                exist = true;
            resultSet.close();
        } catch (SQLException e) {
            console.println(e.getMessage(), true);
        }
        return exist;
    }

    public Account loadByPseudo(String nickname) {
        Account account = null;
        try {
            String query = "SELECT * FROM accounts WHERE pseudo = '" + nickname + "';";
            ResultSet resultSet = manager.getConnection().createStatement().executeQuery(query);
            if (resultSet.next())
                account = new Account(resultSet.getInt("id"), resultSet.getString("account"));
            resultSet.close();
        } catch (Exception e) {
            console.println(e.getMessage(), true);
        }
        return account;
    }

    public boolean update(Account obj) {
        try {
            String baseQuery = "UPDATE accounts SET" +
                    " account = '" + obj.getName() + "'," +
                    " password = '" + obj.getPassword() + "'," +
                    " pseudo = '" + obj.getPseudo() + "'," +
                    " rank = '" + obj.getRank() + "'," +
                    " WHERE id = " + obj.getId();

            PreparedStatement statement = manager.getConnection().prepareStatement(baseQuery);
            statement.execute();

            return true;
        } catch (SQLException e) {
            console.println(e.getMessage(), true);
        }
        return false;
    }

    private  Account loadFromResultSet(ResultSet resultSet) throws SQLException {
        if (resultSet.next()) {
            return new Account(
                    resultSet.getInt("id"),
                    resultSet.getString("account"),
                    resultSet.getString("password"),
                    resultSet.getString("pseudo"),
                    resultSet.getString("question")
                    );
        }
        return null;
    }
}
