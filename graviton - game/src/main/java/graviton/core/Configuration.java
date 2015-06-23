package graviton.core;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import graviton.database.Database;
import lombok.Getter;

import javax.inject.Singleton;
import java.io.File;

/**
 * Created by Botan on 16/06/2015.
 */
@Singleton
public class Configuration {

    @Getter
    private boolean canLog;

    @Getter
    private Database loginDatabase;
    @Getter
    private Database gameDatabase;

    @Getter
    private int serverId;
    @Getter
    private String serverKey;
    @Getter
    private String ip, exchangeIp;
    @Getter
    private int gamePort, exchangePort;

    @Getter
    private int startMap, startCell, startlevel, startKamas;
    @Getter
    private String defaultMessage;
    @Getter
    private String defaultColor;

    public Configuration() {
        Config config = ConfigFactory.parseFile(new File("config.conf"));
        if (!config.isEmpty()) {
            configFromFile(config);
            return;
        }
        configure();
    }

    private void configure() {
        this.canLog = true;
        this.loginDatabase = new Database("127.0.0.1", "login", "root", "");
        this.gameDatabase = new Database("127.0.0.1", "game", "root", "");
        this.serverId = 1;
        this.serverKey = "pvp";
        this.ip = "127.0.0.1";
        this.exchangeIp = "127.0.0.1";
        this.gamePort = 501;
        this.exchangePort = 807;
        this.defaultMessage = "Bienvenue sur Horus";
        this.defaultColor = "000000";
        this.startlevel = 200;
        this.startKamas = 1000000;
        this.startMap = 952;
    }

    private void configFromFile(Config config) {
        //TODO : Config from file
    }
}
