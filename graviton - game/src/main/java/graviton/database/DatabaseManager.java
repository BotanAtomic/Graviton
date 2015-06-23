package graviton.database;

import com.google.inject.Inject;
import graviton.api.Data;
import graviton.api.Manager;
import graviton.console.Console;
import graviton.core.Configuration;
import graviton.database.data.AccountData;
import graviton.database.data.MapData;
import graviton.database.data.PlayerData;
import graviton.enums.DataType;
import graviton.enums.DatabaseType;
import lombok.Getter;

import javax.inject.Singleton;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Botan on 16/06/2015.
 */

@Singleton
public class DatabaseManager implements Manager {
    private final Map<DatabaseType, Database> databases;
    @Getter
    private Console console;
    @Getter
    private Configuration config;
    /**
     * All data
     **/
    @Getter
    private Map<DataType, Data<?>> data;

    @Inject
    public DatabaseManager(Configuration config, Console console) {
        this.databases = getNewMap(config);
        this.config = config;
        this.console = console;
    }

    @Override
    public void configure() {
        this.databases.values().stream().forEach(Database::configure);
        this.data = getNewMapOfData();
    }

    @Override
    public void stop() {
        this.databases.values().stream().forEach(Database::stop);
    }

    private Map<DataType, Data<?>> getNewMapOfData() {
        Map<DataType, Data<?>> list = new ConcurrentHashMap<>();
        list.put(DataType.ACCOUNT, new AccountData(databases.get(DatabaseType.LOGIN)));
        list.put(DataType.PLAYER, new PlayerData(databases.get(DatabaseType.LOGIN)));
        list.put(DataType.MAPS, new MapData(databases.get(DatabaseType.GAME)));
        return list;
    }

    private Map<DatabaseType, Database> getNewMap(Configuration config) {
        Map<DatabaseType, Database> databases = new HashMap<>();
        databases.put(DatabaseType.LOGIN, config.getLoginDatabase());
        databases.put(DatabaseType.GAME, config.getGameDatabase());
        return databases;
    }
}
