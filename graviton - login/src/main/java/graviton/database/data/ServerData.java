package graviton.database.data;

import com.google.inject.Inject;
import com.google.inject.Injector;
import graviton.api.Data;
import graviton.game.Server;
import graviton.login.Configuration;
import graviton.login.Manager;
import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Botan on 08/07/2015.
 */
@Slf4j
public class ServerData extends Data {
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
        loadAll();
    }

    private void loadAll() {
        locker.lock();
        Map<Integer,Server> servers = new HashMap<>();
        try {
            String query = "SELECT * from servers";
            ResultSet resultSet = connection.createStatement().executeQuery(query);
            Server server;
            while (resultSet.next()) {
                server = new Server(resultSet.getInt("id"), resultSet.getString("key"),injector);
                servers.put(server.getId(),server);
            }
            manager.setServers(servers);
        } catch (SQLException e) {
            log.error("Exception > {}", e.getMessage());
        } finally {
            locker.unlock();
        }
    }

    public final String getHostList() {
        StringBuilder sb = new StringBuilder("AH");
        List<Server> list = new ArrayList<>();
        list.addAll(manager.getServers().values());
        list.forEach((server) -> sb.append(server.getId()).append(";").append(server.getState().id).append(";110;1|"));
        return sb.toString();
    }

}

