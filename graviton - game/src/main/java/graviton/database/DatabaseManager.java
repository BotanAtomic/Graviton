package graviton.database;

import com.google.common.reflect.ClassPath;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import graviton.api.Data;
import graviton.api.Manager;
import graviton.core.Configuration;
import graviton.enums.DataType;
import graviton.enums.DatabaseType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Created by Botan on 16/06/2015.
 */

@Singleton
@lombok.Data
public class DatabaseManager implements Manager {

    private final Map<DatabaseType, Database> databases;
    private Map<DataType, Data<?>> data;
    private Configuration config;

    @Inject
    public DatabaseManager(Configuration config) {
        this.databases = getNewMap(config);
        this.config = config;
    }

    @Override
    public void configure() {
        this.databases.values().forEach(Database::configure);
        this.data = getNewMapOfData();
    }

    @Override
    public void stop() {
        this.databases.values().stream().forEach(Database::stop);
    }

    private Map<DataType, Data<?>> getNewMapOfData() {
        Map<DataType, Data<?>> list = new ConcurrentHashMap<>();
        try {
            List<Class<?>> allClass = new ArrayList<>(ClassPath.from(Thread.currentThread().getContextClassLoader()).getTopLevelClasses().stream().filter(info -> info.getName().startsWith("graviton.database.data.")).map(ClassPath.ClassInfo::load).collect(Collectors.toList()));
            allClass.stream().forEach(clazz -> {
                try {
                    Data<?> data = (Data) clazz.newInstance();
                    data.configure();
                    list.put(data.type, data);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    private Map<DatabaseType, Database> getNewMap(Configuration config) {
        Map<DatabaseType, Database> databases = new HashMap<DatabaseType, Database>() {{
            put(DatabaseType.LOGIN, config.getLoginDatabase());
            put(DatabaseType.GAME, config.getGameDatabase());
        }};
        return databases;
    }
}
