package graviton.factory;

import com.google.inject.Inject;
import com.google.inject.Injector;
import graviton.api.Factory;
import graviton.core.Configuration;
import graviton.database.Database;
import graviton.enums.DataType;
import graviton.enums.DatabaseType;
import graviton.game.client.Account;
import graviton.game.client.player.Player;
import graviton.game.enums.Classe;
import graviton.game.object.Object;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.jooq.UpdateSetFirstStep;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import static graviton.database.utils.game.Tables.ITEMS;
import static graviton.database.utils.login.Tables.PLAYERS;

/**
 * Created by Botan on 24/12/2015.
 */
@Slf4j
public class PlayerFactory extends Factory<Player> {
    @Inject
    Injector injector;
    @Inject
    Configuration configuration;

    @Getter
    private Map<Classe, Map<Integer, Integer>> classData;

    private final Map<Integer, Player> players;

    private final Lock locker;

    public PlayerFactory() {
        super(DatabaseType.LOGIN);
        this.players = new ConcurrentHashMap<>();
        this.locker = new ReentrantLock();
    }

    public List<Player> load(Account account) {
        List<Player> players = new CopyOnWriteArrayList<>();
        players.addAll(database.getResult(PLAYERS, PLAYERS.ACCOUNT.equal(account.getId())).stream().filter(record -> record.getValue(PLAYERS.SERVER) == (configuration.getServerId())).map(record -> new Player(account, record,injector)).collect(Collectors.toList()));
        return players;
    }

    public int getNextId() {
        return database.getNextId(PLAYERS, PLAYERS.ID);
    }

    public boolean checkName(String name) {
        return database.getRecord(PLAYERS, PLAYERS.NAME.equal(name)) != null;
    }

    public void remove(Player player) {
        database.remove(PLAYERS, PLAYERS.ID.equal(player.getId()));
        deleteObject(player.getObjects().values());
    }

    private void deleteObject(Collection<Object> objects) {
        objects.forEach(object1 -> getGameDatabase().remove(ITEMS, ITEMS.ID.equal(object1.getId())));
    }

    public boolean create(Player player) {
        int id = database.getDSLContext().insertInto(PLAYERS, PLAYERS.ID, PLAYERS.ACCOUNT, PLAYERS.NAME, PLAYERS.SEX, PLAYERS.GFX, PLAYERS.CLASS,
                PLAYERS.COLORS, PLAYERS.SPELLS, PLAYERS.SPELLPOINTS, PLAYERS.CAPITAL, PLAYERS.LEVEL, PLAYERS.EXPERIENCE, PLAYERS.POSITION, PLAYERS.SERVER)
                .values(player.getId(),
                        player.getAccount().getId(),
                        player.getName(),
                        player.getSex(),
                        player.getGfx(),
                        player.getClasse().getId(),
                        player.getColor(1) + ";" + player.getColor(2) + ";" + player.getColor(3),
                        player.parseSpells(),
                        player.getSpellPoints(),
                        player.getCapital()
                        , player.getLevel(),
                        player.getExperience(),
                        player.getMap().getId() + ";" + player.getPosition().getCell().getId(),
                        configuration.getServerId()).execute();
        return id > 0;
    }

    public void update(Player player) {
        UpdateSetFirstStep firstStep = database.getDSLContext().update(PLAYERS);
        firstStep.set(PLAYERS.NAME, player.getName());
        firstStep.set(PLAYERS.SEX, player.getSex());
        firstStep.set(PLAYERS.GFX, player.getGfx());
        firstStep.set(PLAYERS.COLORS, player.getColor(1) + ";" + player.getColor(2) + ";" + player.getColor(3));
        firstStep.set(PLAYERS.SPELLS, player.parseSpells());
        firstStep.set(PLAYERS.STATISTICS, player.parseStatistics());
        firstStep.set(PLAYERS.CAPITAL, player.getCapital());
        firstStep.set(PLAYERS.SPELLPOINTS, player.getSpellPoints());
        firstStep.set(PLAYERS.ITEMS, player.parseObject());
        firstStep.set(PLAYERS.POSITION, player.getMap().getId() + ";" + player.getCell().getId());
        firstStep.set(PLAYERS.ALIGNEMENT, player.parseAlignement());
        firstStep.set(PLAYERS.KAMAS, player.getKamas());
        firstStep.set(PLAYERS.SPELLPOINTS, player.getGfx()).where(PLAYERS.ID.equal(player.getId())).execute();
    }


    @Override
    public DataType getType() {
        return DataType.PLAYER;
    }

    @Override
    public Map<Integer, Player> getElements() {
        return this.players;
    }


    @Override
    public void configure() {
        this.classData = (Map<Classe, Map<Integer, Integer>>) decodeObject("classData");
        super.configureDatabase();
    }

    public void add(Player player) {
        this.players.put(player.getId(), player);
    }

    public void delete(int id) {
        this.players.remove(id);
    }

    public List<Player> getOnlinePlayers() {
        List<Player> onlinePlayers = new ArrayList<>();
        this.players.values().stream().filter(Player::isOnline).forEach(onlinePlayers::add);
        return onlinePlayers;
    }

    @Override
    public Player get(java.lang.Object object) {
        final Player[] player = {null};
        try {
            locker.lock();

            if (object instanceof Integer)
                player[0] = this.players.get(object);
            else
                this.players.values().stream().filter(player1 -> player1.getName().equals(object)).forEach(playerSelected -> player[0] = playerSelected);

        } finally {
            locker.unlock();
        }
        return player[0];
    }

    private Database getGameDatabase() {
        return configuration.getGameDatabase();
    }

    public void send(String packet) {
        try {
            locker.lock();
            getOnlinePlayers().forEach(player -> player.send(packet));
        } finally {
            locker.unlock();
        }
    }
}
