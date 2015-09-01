package graviton.api;

import graviton.login.Configuration;
import graviton.login.Login;
import graviton.login.Main;
import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by Botan on 08/07/2015.
 */
@Slf4j
public abstract class Data {
    protected final ReentrantLock locker = new ReentrantLock();
    protected final Connection connection = Main.getInstance(Configuration.class).getDatabase().getConnection();
    protected final Login login = Main.getInstance(Login.class);
}
