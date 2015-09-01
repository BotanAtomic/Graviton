package graviton.login;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import graviton.database.Database;
import lombok.Data;
import lombok.Getter;

import java.io.File;

/**
 * Created by Botan on 06/06/2015.
 */
@Data
public class Configuration {

    private String loginIp, exchangeIp;
    private int loginPort, exchangePort;
    private Database database;

    public Configuration() {
        Config config = ConfigFactory.parseFile(new File("config.conf"));
        if (!config.isEmpty()) {
            configFromFile(config);
            return;
        }

        this.loginIp = "127.0.0.1";
        this.exchangeIp = "127.0.0.1";
        this.loginPort = 699;
        this.exchangePort = 807;
        this.database = new Database("127.0.0.1", "login", "root", "").connect();
    }

    private void configFromFile(Config config) {
        this.loginIp = config.getString("login.network.login.ip");
        this.exchangeIp = config.getString("login.network.exchange.ip");
        this.loginPort = config.getInt("login.network.login.port");
        this.exchangePort = config.getInt("login.network.exchange.port");
        String databaseHost = config.getString("login.database.host");
        String databaseName = config.getString("login.database.name");
        String databaseUser = config.getString("login.database.user");
        String databasePass = config.getString("login.database.pass");
        this.database = new Database(databaseHost,databaseName,databaseUser,databasePass).connect();
    }

}
