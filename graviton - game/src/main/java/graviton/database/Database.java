package graviton.database;

import lombok.Data;
import lombok.Getter;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Created by Botan on 16/06/2015.
 */
@Data
public class Database {
    private final String host, name, user, pass;
    private Connection connection;

    public Database(String host, String name, String user, String pass) {
        this.host = host;
        this.name = name;
        this.user = user;
        this.pass = pass;
    }

    public void configure() {
        try {
            this.connection = DriverManager.getConnection("jdbc:mysql://" + host + "/" + name, user, pass);
            if (!connection.isValid(1000)) {
                System.err.println("Unable to connect to database : " + name);
                System.exit(0);
            }
            this.connection.setAutoCommit(true);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void stop() {
        try {
            this.connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
