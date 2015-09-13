package graviton.database.data;

import graviton.api.Data;
import graviton.game.Account;
import graviton.game.Player;
import lombok.extern.slf4j.Slf4j;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Botan on 08/07/2015.
 */
@Slf4j
public class PlayerData extends Data {

    /**
     * load all players from account
     *
     * @param account
     */
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

    /**
     * System for search friends
     *
     * @param nickname
     * @return
     */
    public List<Player> getPlayers(String nickname) {
        locker.lock();
        List<Player> players = new ArrayList<>();
        try {
            int id = 0;
            ResultSet result = connection.createStatement().executeQuery("SELECT * FROM accounts WHERE pseudo = '" + nickname + "'");
            if (result.next())
                id = result.getInt("id");
            result = connection.createStatement().executeQuery("SELECT * FROM players WHERE account = " + id);
            while (result.next()) {
                players.add(new Player(result.getInt("server")));
            }
        } catch (Exception e) {
            log.error("Exception > {}", e.getMessage());
        } finally {
            locker.unlock();
        }
        return players;
    }
}

