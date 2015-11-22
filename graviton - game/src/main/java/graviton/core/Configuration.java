package graviton.core;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import graviton.database.Database;
import lombok.Data;

import java.io.File;

/**
 * Created by Botan on 16/06/2015.
 */
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
        this.loginDatabase = new Database("PRFPOUz8cPFkhmkZatwE6A==", "XtxV1iNc82puQyu1UdWQKg==", "psUkfKpV6xHmdvuIMk05CQ==", "");
        this.gameDatabase = new Database("PRFPOUz8cPFkhmkZatwE6A==", "M7jst2i9joSes1wu7XiQjw==", "psUkfKpV6xHmdvuIMk05CQ==", "");
        this.serverId = 1;
        this.serverKey = "pvp";
        this.ip = "127.0.0.1";
        this.gamePort = 100;
        this.exchangeIp = "127.0.0.1";
        this.exchangePort = 807;
        this.defaultMessage = "Bienvenue sur Horus";
        this.defaultColor = "000000";
        this.startlevel = 80;
        this.startKamas = 1000000;
        this.startMap = 952;
        this.startCell = 400;
    }

    private void configFromFile(Config config) {
        //TODO : Config from file
    }
}
