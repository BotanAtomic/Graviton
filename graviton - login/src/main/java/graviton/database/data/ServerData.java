package graviton.database.data;

import graviton.api.Data;
import graviton.game.Server;
import lombok.extern.slf4j.Slf4j;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Botan on 08/07/2015.
 */
@Slf4j
public class ServerData extends Data {

    public ServerData() {
        loadAllServers();
    }

    private void loadAllServers() {
        locker.lock();
        List<Server> servers = new ArrayList<>();
        try {
            String query = "SELECT * from servers";
            ResultSet resultSet = connection.createStatement().executeQuery(query);

            while(resultSet.next())
                servers.add(new Server(resultSet.getInt("id"), resultSet.getString("key")));
            servers.forEach((server) -> login.getServers().put(server.getId(),server));
        }catch (SQLException e) {
            log.error("Exception > {}", e.getMessage());
        } finally {
            locker.unlock();
        }
    }

    public String getHostList() {
        StringBuilder sb = new StringBuilder("AH");
        List<Server> list = new ArrayList<>();
        list.addAll(login.getServers().values());
        list.forEach((server) -> sb.append(server.getId()).append(";").append(server.getState().id).append(";110;1|"));
        return sb.toString();
    }

}

