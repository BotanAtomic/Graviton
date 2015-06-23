package graviton.database;

import com.google.inject.Inject;
import graviton.console.Console;
import graviton.login.Configuration;
import graviton.network.NetworkManager;
import lombok.Getter;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Created by Botan on 06/06/2015.
 */
public class DatabaseManager {
    @Getter private final Database database;
    @Getter
    private Console console;
    @Getter
    private Configuration config;
    @Getter private Connection connection;
    @Getter
    private NetworkManager networkManager;

    @Inject
    public DatabaseManager(Configuration config, Console console, NetworkManager manager) {
        this.database = config.getDatabase();
        this.config = config;
        this.console = console;
        this.networkManager = manager;
    }

    public boolean configure() throws SQLException {
        this.connection = DriverManager.getConnection("jdbc:mysql://" +
                        this.database.getHost() + "/" +
                        this.database.getName(),
                this.database.getUser(),
                this.database.getPass());
        if (!connection.isValid(1000))
            return false;
        connection.setAutoCommit(true);
        database.configure(this);
        return true;
    }

    public void stop() {
        try {
            connection.close();
        } catch (SQLException exception) {
            console.println("Cannot close database : \n" + exception.toString(), true);
        }
    }
}
