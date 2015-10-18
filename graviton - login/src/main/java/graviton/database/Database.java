package graviton.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import graviton.common.CryptManager;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Created by Botan on 06/07/2015.
 */
@Data
@Slf4j
public class Database {
    private final HikariConfig dataConfig;
    private Connection connection;
    private HikariDataSource dataSource;

    public Database(String ip, String user, String name, String pass) {
        dataConfig = new HikariConfig() {
            {
                setDataSourceClassName("com.mysql.jdbc.jdbc2.optional.MysqlDataSource");
                addDataSourceProperty("serverName", CryptManager.decrypt(ip));
                addDataSourceProperty("port", 3306);
                addDataSourceProperty("databaseName", CryptManager.decrypt(name));
                addDataSourceProperty("user", CryptManager.decrypt(user));
                addDataSourceProperty("password", CryptManager.decrypt(pass));
            }
        };
    }

    public Database connect() {
        dataSource = new HikariDataSource(dataConfig);
        if (!testConnection()) {
            log.error("Can't connect to database");
            System.exit(1);
            return null;
        }
        return this;
    }

    public void stop() {
        try {
            this.connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private boolean testConnection() {
        try {
            connection = dataSource.getConnection();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
