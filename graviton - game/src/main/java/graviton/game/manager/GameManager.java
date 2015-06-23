package graviton.game.manager;

import com.google.inject.Inject;
import graviton.api.Manager;
import graviton.database.DatabaseManager;
import graviton.enums.DataType;
import graviton.game.client.Account;
import graviton.game.client.player.Player;
import graviton.game.maps.Maps;
import lombok.Getter;

import javax.inject.Singleton;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Botan on 19/06/2015.
 */
@Singleton
public class GameManager implements Manager {
    @Inject
    DatabaseManager databaseManager;

    @Getter
    private Map<Integer, Account> accounts;
    @Getter
    private Map<Integer, Player> players;

    @Getter
    private Map<Integer, Maps> maps;

    @Override
    public void configure() {
        this.accounts = new ConcurrentHashMap<>();
        this.players = new ConcurrentHashMap<>();
        this.maps = new ConcurrentHashMap<>();
    }

    public Maps getMap(int id) {
        if (maps.get(id) == null)
            return (Maps) databaseManager.getData().get(DataType.MAPS).load(id);
        return maps.get(id);
    }

    @Override
    public void stop() {
        accounts.clear();
        players.clear();
    }
}
