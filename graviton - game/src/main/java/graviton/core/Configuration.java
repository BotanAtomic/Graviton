package graviton.core;

import com.google.inject.Singleton;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import graviton.database.Database;
import lombok.Data;

import java.io.File;

/**
 * Created by Botan on 16/06/2015.
 */
@Singleton
@Data
public class Configuration {

    private Database loginDatabase;
    private Database gameDatabase;

    private int serverId;
    private String serverKey;
    private String ip, exchangeIp;
    private int gamePort, exchangePort;

    private int startMap, startCell, startlevel, startKamas;
    private String defaultMessage,defaultColor;

    public Configuration() {
        Config config = ConfigFactory.parseFile(new File("config.conf"));
        if (!config.isEmpty()) {
            configFromFile(config);
            return;
        }
        configure();
    }

    private void configure() {
        this.loginDatabase = new Database("127.0.0.1", "login", "root", "");
        this.gameDatabase = new Database("127.0.0.1", "game", "root", "");
        this.serverId = 1;
        this.serverKey = "pvp";
        this.ip = "127.0.0.1";
        this.exchangeIp = "127.0.0.1";
        this.gamePort = 100;
        this.exchangePort = 807;
        this.defaultMessage = "Bienvenue sur Horus";
        this.defaultColor = "000000";
        this.startlevel = 80;
        this.startKamas = 1000000;
        this.startMap = 10098;
        this.startCell = 200;
    }

    private void configFromFile(Config config) {
        //TODO : Config from file
    }
}
