package graviton.database.data;

import graviton.api.Data;
import graviton.game.Account;
import graviton.game.Player;
import graviton.network.login.LoginClient;
import lombok.extern.slf4j.Slf4j;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by Botan on 08/07/2015.
 */
@Slf4j
public class PlayerData extends Data {

    public void loadAll(Account account) {
        locker.lock();
        try {
            ResultSet result = connection.createStatement().executeQuery("SELECT * FROM players WHERE account = " + account.getId());
            while (result.next())
                account.getPlayers().add(new Player(result.getInt("id"), result.getString("name"), result.getInt("server")));
            result.close();
        } catch (Exception e) {
            log.error("Exception > {}", e.getMessage());
        } finally {
            locker.unlock();
        }
    }
}

