package graviton.login;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import graviton.database.Database;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.File;

/**
 * Created by Botan on 06/06/2015.
 */
@Data
@Slf4j
public final class Configuration {
    /**
     * Method 1 = by file
     * Method 2 = by existing information
     */

    private String loginIp, exchangeIp;
    private int loginPort, exchangePort;
    private Database database;

    public Configuration() {
        final Config config = ConfigFactory.parseFile(new File("config.conf"));
        if (!config.isEmpty()) {
            configFromFile(config);
            log.debug("The login is configured with method 2");
            return;
        }

        this.loginIp = "127.0.0.1";
        this.exchangeIp = "127.0.0.1";
        this.loginPort = 699;
        this.exchangePort = 807;
        this.database = new Database("PRFPOUz8cPFkhmkZatwE6A==", "XtxV1iNc82puQyu1UdWQKg==", "psUkfKpV6xHmdvuIMk05CQ==", "").connect();
        log.debug("The login is configured with method 1");
    }

    private void configFromFile(Config config) {
        //TODO : configuration by file
    }

}
