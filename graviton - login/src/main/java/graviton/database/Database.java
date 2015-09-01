package graviton.database;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Created by Botan on 06/07/2015.
 */
@Data
@Slf4j
public class Database {
    private String ip,name,user,pass;
    private Connection connection;

    public Database(String ip, String name, String user, String pass) {
        this.ip = ip;
        this.name = name;
        this.user = user;
        this.pass = pass;
    }

    public Database connect() {
        try {
            this.connection = DriverManager.getConnection("jdbc:mysql://" + ip + "/" + name, user, pass);
            if (!connection.isValid(1000)) {
                System.err.println("Unable to connect to database : " + name);
                System.exit(0);
            }
            this.connection.setAutoCommit(true);
            //TODO ; add to log -> successfully connect to database
        } catch (SQLException e) {
            e.printStackTrace();
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
}
