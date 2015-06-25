package graviton.game;

import com.google.inject.Inject;
import graviton.api.Manager;
import graviton.database.DatabaseManager;
import graviton.enums.DataType;
import graviton.game.client.Account;
import graviton.game.client.player.Player;
import graviton.game.maps.Maps;
import graviton.zone.SubZone;
import graviton.zone.Zone;
import lombok.Data;
import lombok.Getter;

import javax.inject.Singleton;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Botan on 19/06/2015.
 */
@Singleton
@Data
public class GameManager implements Manager {
    @Inject
    private DatabaseManager databaseManager;

    private Map<Integer, Account> accounts;
    private Map<Integer, Player> players;
    private Map<Integer, Maps> maps;
    private Map<Integer,Zone> zones;
    private Map<Integer,SubZone> subZones;

    @Override
    public void configure() {
        this.accounts = new ConcurrentHashMap<>();
        this.players = new ConcurrentHashMap<>();
        this.maps = new ConcurrentHashMap<>();
        this.zones = new ConcurrentHashMap<>();
        this.subZones = new ConcurrentHashMap<>();
    }

    private void configureStaticData() {

    }

    public Maps getMap(int id) {
        if (!maps.containsKey(id))
            return (Maps) databaseManager.getData().get(DataType.MAPS).load(id);
        return maps.get(id);
    }

    @Override
    public void stop() {

    }
}
