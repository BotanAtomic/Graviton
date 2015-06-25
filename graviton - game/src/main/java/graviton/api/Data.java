package graviton.api;

import graviton.console.Console;
import graviton.core.Main;
import graviton.database.DatabaseManager;
import graviton.enums.DataType;
import graviton.enums.DatabaseType;
import graviton.game.GameManager;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by Botan on 21/06/2015.
 */
public abstract class Data<T> {

    protected final ReentrantLock locker;
    protected final Console console;
    protected final GameManager manager;
    protected final DatabaseManager databaseManager;

    protected Connection connection;
    public DataType type;

    public Data() {
        this.locker = new ReentrantLock();
        this.console = Main.getInstance(Console.class);
        this.manager = Main.getInstance(GameManager.class);
        this.databaseManager = Main.getInstance(DatabaseManager.class);
    }

    public abstract void configure();

    public abstract T load(Object object);

    public abstract boolean create(T object);

    public abstract T getByResultSet(ResultSet result) throws SQLException;

    public abstract void update(T object);

    public abstract void delete(T object);

    public abstract boolean exist(Object object);

    public abstract int getNextId();

    public abstract List<T> loadAll(Object object);
}
