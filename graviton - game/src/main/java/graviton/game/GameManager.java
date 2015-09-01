package graviton.game;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import graviton.api.Manager;
import graviton.core.Main;
import graviton.database.Database;
import graviton.database.DatabaseManager;
import graviton.enums.Classe;
import graviton.enums.DataType;
import graviton.game.client.Account;
import graviton.game.client.player.Player;
import graviton.game.experience.Experience;
import graviton.game.maps.Maps;
import graviton.game.spells.Spell;
import graviton.game.zone.SubZone;
import graviton.game.zone.Zone;
import graviton.network.exchange.ExchangeNetworkService;
import lombok.Data;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Botan on 19/06/2015.
 */
@Data
@Singleton
public class GameManager implements Manager {
    @Inject
    private DatabaseManager databaseManager;

    private ExchangeNetworkService exchangeNetworkService;

    private Experience experience;

    private Map<Classe,Map<Integer,Integer>> classData;

    private Map<Integer, Account> accounts;
    private Map<Integer, Player> players;
    private Map<Integer, Maps> maps;
    private Map<Integer,Zone> zones;
    private Map<Integer,SubZone> subZones;
    private Map<Integer,Spell> spells;

   @Override
    public void configure() {
        this.accounts = new ConcurrentHashMap<>();
        this.players = new ConcurrentHashMap<>();
        this.maps = new ConcurrentHashMap<>();
        this.zones = new ConcurrentHashMap<>();
        this.subZones = new ConcurrentHashMap<>();
        this.classData = new ConcurrentHashMap<>();
        databaseManager.getData().get(DataType.STATIC).loadAll(null);
    }

    public Maps getMap(int id) {
        if (!maps.containsKey(id))
            return (Maps) databaseManager.getData().get(DataType.MAPS).load(id);
        return maps.get(id);
    }

    public long getExperience(int level) {
        if(level > 200)
            level = 200;
        return experience.getData().get(DataType.PLAYER).get(level);
    }

    public void save() {
        if(exchangeNetworkService == null)
            exchangeNetworkService = Main.getInstance(ExchangeNetworkService.class);
        exchangeNetworkService.send("SS2");
        //TODO : Save..
        exchangeNetworkService.send("SS1");
    }

    @Override
    public void stop() {

    }
}
