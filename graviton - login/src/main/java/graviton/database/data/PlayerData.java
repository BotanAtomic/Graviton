package graviton.database.data;

import graviton.console.Console;
import graviton.database.DatabaseManager;
import graviton.game.Account;
import graviton.game.Player;
import graviton.game.Server;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Botan on 08/06/2015.
 */
public class PlayerData {
    private Console console;
    private DatabaseManager manager;

    public PlayerData(DatabaseManager manager) {
        this.manager = manager;
        this.console = manager.getConsole();
    }


    public List<Account> getAccountByPseudo(String pseudo) {
        List<Account> accounts = new ArrayList<>();
        try {
            ResultSet resultSet = manager.getConnection().createStatement().executeQuery("SELECT account FROM players WHERE pseudo =" + pseudo);
            while (resultSet.next())
                accounts.add(manager.getDatabase().getAccountData().load(resultSet.getInt("account")));
            resultSet.close();
        } catch (SQLException e) {
            console.println(e.getMessage(), true);
        }
        return accounts;
    }
    public Player load(Object obj) {
        try {
            if (obj instanceof Account) {
                Account account = (Account) obj;
                ResultSet result = manager.getConnection().createStatement().executeQuery("SELECT * FROM players WHERE account = " + account.getId());
                while (result.next()) {
                    account.getPlayers().add(new Player(result.getInt("id"), result.getString("name"), result.getInt("server")));
                }
                result.close();
            }
        } catch (SQLException e) {
            console.println(e.getMessage(), true);
        }
        return null;
    }

    public Map<Server, ArrayList<Integer>> loadAllPlayersByAccountId(int notServer, int account) {
        Map<Server, ArrayList<Integer>> maps = new HashMap<>();
        try {
            ResultSet resultSet = manager.getConnection().createStatement().executeQuery("SELECT id,server FROM players WHERE account = '" + account + "' AND NOT server = '" + notServer + "';");

            while (resultSet.next()) {
                Server server = manager.getDatabase().getServerData().getServers().get(resultSet.getInt("server"));
                int id = resultSet.getInt("id");

                if (maps.get(server) == null) {
                    ArrayList<Integer> array = new ArrayList<>();
                    array.add(id);
                    maps.put(server, array);
                } else
                    maps.get(server).add(id);
            }
            resultSet.close();
        } catch (SQLException e) {
            console.println(e.getMessage(), true);
        }
        return maps;
    }

    public int isLogged(Account account) {
        int logged = 0;
        try {
            ResultSet resultSet = manager.getConnection().createStatement().executeQuery("SELECT * FROM players WHERE account = " + account.getId());
            while (resultSet.next())
                if (resultSet.getInt("logged") == 1)
                    logged = resultSet.getInt("server");
            resultSet.close();
        } catch (Exception e) {
            console.println(e.getMessage(), true);
        }
        return logged;
    }
}
