package graviton.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import graviton.common.CryptManager;
import lombok.Data;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Created by Botan on 16/06/2015.
 */
@Data
public class Database {

    private final HikariConfig dataConfig;
    private Connection connection;
    private HikariDataSource dataSource;


    public Database(String host, String name, String user, String pass) {
        dataConfig = new HikariConfig() {
            {
                setDataSourceClassName("com.mysql.jdbc.jdbc2.optional.MysqlDataSource");
                addDataSourceProperty("serverName", CryptManager.decrypt(host));
                addDataSourceProperty("port", 3306);
                addDataSourceProperty("databaseName", CryptManager.decrypt(name));
                addDataSourceProperty("user", CryptManager.decrypt(user));
                addDataSourceProperty("password", CryptManager.decrypt(pass));
            }
        };
    }

    public void configure() {
        dataSource = new HikariDataSource(dataConfig);
        if (!testConnection()) {
            System.err.println("Can't connect to database");
            System.exit(1);
            return;
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

    public void stop() {
        try {
            this.connection.close();
            dataSource.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
