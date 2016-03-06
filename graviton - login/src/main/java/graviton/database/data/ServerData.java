package graviton.database.data;

import com.google.inject.Inject;
import com.google.inject.Injector;
import graviton.database.Database;
import graviton.game.Server;
import graviton.login.Configuration;
import graviton.login.Manager;
import lombok.extern.slf4j.Slf4j;
import org.jooq.Record;
import org.jooq.Result;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static graviton.database.utils.Tables.SERVERS;

/**
 * Created by Botan on 08/07/2015.
 */
@Slf4j
public class ServerData {
    @Inject
    Injector injector;
    @Inject
    Configuration configuration;
    @Inject
    Manager manager;

    private Database database;

    public void initialize() {
        this.database = configuration.getDatabase();
        loadAll();
    }

    private void loadAll() {
        Map<Integer, Server> servers = new HashMap<>();
        Result<Record> result = database.getResult(SERVERS);
        for (Record record : result)
            servers.put(record.getValue(SERVERS.ID), new Server(record.getValue(SERVERS.ID), record.getValue(SERVERS.KEY), injector));
        manager.setServers(servers);
    }

    public final String getHostList() {
        StringBuilder sb = new StringBuilder("AH");
        List<Server> list = new ArrayList<>();
        list.addAll(manager.getServers().values());
        list.forEach((server) -> sb.append(server.getId()).append(";").append(server.getState().id).append(";110;1|"));
        return sb.toString();
    }

}

