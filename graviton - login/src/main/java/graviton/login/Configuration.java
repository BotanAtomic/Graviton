package graviton.login;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import graviton.database.Database;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.File;

/**
 * Created by Botan on 06/06/2015.
 */
@Slf4j
public final class Configuration {
    /**
     * Method 1 = by file
     * Method 2 = by existing information
     */
    @Getter
    private String loginIp, exchangeIp;
    @Getter
    private int loginPort, exchangePort;
    @Getter
    private Database database;

    public Configuration() {
        final Config config = ConfigFactory.parseFile(new File("config.conf"));
        if (!config.isEmpty()) {
            configFromFile(config);
            log.trace("The login is configured with method 2");
            return;
        }

        this.loginIp = "127.0.0.1";
        this.exchangeIp = "127.0.0.1";
        this.loginPort = 699;
        this.exchangePort = 807;
        this.database = new Database("PRFPOUz8cPFkhmkZatwE6A==", "psUkfKpV6xHmdvuIMk05CQ==", "XtxV1iNc82puQyu1UdWQKg==", "").connect();
        log.trace("The login is configured with method 1");
    }

    private void configFromFile(Config config) {
        this.loginIp = config.getString("login.network.login.ip");
        this.exchangeIp = config.getString("login.network.exchange.ip");
        this.loginPort = config.getInt("login.network.login.port");
        this.exchangePort = config.getInt("login.network.exchange.port");
        this.database = new Database(config.getString("login.database.host"),
                config.getString("login.database.user"),
                config.getString("login.database.name"),
                config.getString("login.database.pass")).connect();

    }

}
