package graviton.database;

import com.google.inject.Inject;
import com.google.inject.Injector;
import graviton.game.Account;
import graviton.game.Player;
import graviton.game.Server;
import graviton.core.Manager;
import graviton.network.login.LoginClient;
import org.jooq.*;

import java.util.*;
import java.util.stream.Collectors;

import static graviton.database.utils.Tables.*;

/**
 * Created by Botan on 06/07/2015.
 */
public class Database {
    @Inject
    Injector injector;

    @Inject
    Manager manager;

    private DSLContext dslContext;

    public Database(DSLContext context) {
        this.dslContext = context;
    }

    private Result<Record> getResult(Table<?> table) {
        return dslContext.select().from(table).fetch();
    }

    private Result<Record> getResult(Table<?> table, Condition condition) {
        return dslContext.select().from(table).where(condition).fetch();
    }

    private Record getRecord(Table<?> table, Condition condition, Condition condition2) {
        return dslContext.select().from(table).where(condition).and(condition2).fetchOne();
    }

    private Record getRecord(Table<?> table, Condition condition) {
        return dslContext.select().from(table).where(condition).fetchOne();
    }

    public void stop() {

    }

    /**
     * Data
     **/

    public boolean isGoodAccount(String username, String password, LoginClient client) {
        Record record = getRecord(ACCOUNTS, ACCOUNTS.ACCOUNT.equal(username));
        if (record != null)
            if (encrypt(record.getValue(ACCOUNTS.PASSWORD), client.getKey()).equals(password))
                return true;
        return false;
    }

    public final Account loadAccount(String arguments) {
        Record record = getRecord(ACCOUNTS, ACCOUNTS.ACCOUNT.equal(arguments));

        if (record != null) {
            manager.checkAccount(record.getValue(ACCOUNTS.ID));
            return new Account(record.getValue(ACCOUNTS.ID),
                    record.getValue(ACCOUNTS.ACCOUNT), record.getValue(ACCOUNTS.PASSWORD),
                    record.getValue(ACCOUNTS.PSEUDO), record.getValue(ACCOUNTS.QUESTION), record.getValue(ACCOUNTS.RANK), injector);
        }
        return null;
    }

    public final Account loadAccount(String account, String password) {
        Record record = getRecord(ACCOUNTS, ACCOUNTS.ACCOUNT.equal(account), ACCOUNTS.PASSWORD.equal(password));
        if (record != null)
            return new Account(record.getValue(ACCOUNTS.ACCOUNT), record.getValue(ACCOUNTS.RANK), injector);
        return null;
    }

    public void updateNickname(Account account) {
        this.dslContext.update(ACCOUNTS).set(ACCOUNTS.PSEUDO, account.getPseudo()).where(ACCOUNTS.ID.equal(account.getId())).execute();
    }

    public boolean isAvaiableNickname(String nickname) {
        return getRecord(ACCOUNTS, ACCOUNTS.PSEUDO.equal(nickname)) == null;
    }

    private String encrypt(String pass, String key) {
        final char[] HASH = {'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l',
                'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', 'A',
                'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P',
                'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '0', '1', '2', '3', '4',
                '5', '6', '7', '8', '9', '-', '_'};

        int i = HASH.length;
        StringBuilder crypted = new StringBuilder("#1");
        for (int y = 0; y < pass.length(); y++) {
            char c1 = pass.charAt(y);
            char c2 = key.charAt(y);
            double d = Math.floor(c1 / 16);
            int j = c1 % 16;
            crypted.append(HASH[(int) ((d + c2 % i) % i)]).append(HASH[(j + c2 % i) % i]);
        }
        return crypted.toString();
    }

    public void loadPlayers(Account account) {
        Result<Record> result = getResult(PLAYERS, PLAYERS.ACCOUNT.equal(account.getId()));
        for (Record record : result)
            account.getPlayers().add(new Player(record.getValue(PLAYERS.ID), record.getValue(PLAYERS.NAME), record.getValue(PLAYERS.SERVER), injector));
    }

    public List<Player> getPlayers(String nickname) {
        List<Player> players = new ArrayList<>();
        Result<Record> result = getResult(PLAYERS, PLAYERS.ACCOUNT.equal(dslContext.select(ACCOUNTS.ID).where(ACCOUNTS.PSEUDO.equal(nickname)).fetchOne().getValue(ACCOUNTS.ID)));
        players.addAll(result.stream().map(record -> new Player(record.getValue(PLAYERS.SERVER))).collect(Collectors.toList()));
        return players;
    }

    public void loadServers() {
        Map<Integer, Server> servers = new HashMap<>();
        Result<Record> result = getResult(SERVERS);
        for (Record record : result)
            servers.put(record.getValue(SERVERS.ID), new Server(record.getValue(SERVERS.ID), record.getValue(SERVERS.KEY), injector));
        manager.setServers(Collections.unmodifiableMap(servers));
    }

    public String getHostList() {
        StringBuilder sb = new StringBuilder("AH");
        List<Server> list = new ArrayList<>();
        list.addAll(manager.getServers().values());
        list.forEach((server) -> sb.append(server.getId()).append(";").append(server.getState().id).append(";110;1|"));
        return sb.toString();
    }
}
