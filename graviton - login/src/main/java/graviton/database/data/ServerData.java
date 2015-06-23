package graviton.database.data;

import graviton.console.Console;
import graviton.database.DatabaseManager;
import graviton.game.Server;
import lombok.Getter;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Botan on 08/06/2015.
 */
public class ServerData {
    private Console console;
    private DatabaseManager manager;
    @Getter private Map<Integer,Server> servers;

    public ServerData(DatabaseManager manager) {
        this.manager = manager;
        this.console = manager.getConsole();
        this.servers = new HashMap<>();
        loadAllServers();
    }

    private void loadAllServers() {
        List<Server> servers = new ArrayList<>();
        try {
            String query = "SELECT * from servers";
            ResultSet resultSet = manager.getConnection().createStatement().executeQuery(query);

            while(resultSet.next())
                servers.add(new Server(resultSet.getInt("id"), resultSet.getString("key"), manager.getNetworkManager()));
            servers.forEach((server) -> this.servers.put(server.getId(),server));
        }catch (SQLException e) {
            console.println(e.getMessage(), true);
        }
    }

    public String getHostList() {
        StringBuilder sb = new StringBuilder("AH");
        List<Server> list = new ArrayList<>();
        list.addAll(this.servers.values());
        list.forEach((server) -> sb.append(server.getId()).append(";").append(server.getState()).append(";110;1|"));
        return sb.toString();
    }

}
