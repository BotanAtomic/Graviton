package graviton.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import graviton.utils.Encryption;
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
    private Connection connection;

    private HikariDataSource dataSource;
    private HikariConfig dataConfig;

    public Database(String ip, String name, String user, String pass) {
        dataConfig = new HikariConfig() {
            {
                setDataSourceClassName("com.mysql.jdbc.jdbc2.optional.MysqlDataSource");
                addDataSourceProperty("serverName", Encryption.decrypt(ip));
                addDataSourceProperty("port", 3306);
                addDataSourceProperty("databaseName", Encryption.decrypt(name));
                addDataSourceProperty("user", Encryption.decrypt(user));
                addDataSourceProperty("password", Encryption.decrypt(pass));
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
