package graviton.login;

import com.google.inject.Inject;
import graviton.console.Console;
import graviton.database.DatabaseManager;
import graviton.network.NetworkManager;
import lombok.Getter;

import java.sql.SQLException;

/**
 * Created by Botan on 06/06/2015.
 */

public class LoginManager {
    @Inject DatabaseManager databaseManager;
    @Inject NetworkManager networkManager;

    @Getter
    private boolean running;
    private Console console;

    @Inject
    public LoginManager(Console console) {
        this.console = console;
    }

    public LoginManager start() {
        try {
            databaseManager.configure();
        } catch (SQLException e) {
            console.println("Cannot connect to database : \n" + e.toString(), true);
            System.exit(1);
        }
        networkManager.configure();
        console.initializeEmulatorName();
        this.running = true;
        console.initialize(this);
        return this;
    }

    public void stop() {
        this.running = false;
        networkManager.stop();
        databaseManager.stop();
    }

}
