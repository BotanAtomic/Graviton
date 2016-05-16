package graviton.database;

import com.google.inject.Inject;
import com.google.inject.Injector;
import graviton.api.Manageable;
import graviton.core.GlobalManager;
import graviton.game.Account;
import graviton.game.Player;
import graviton.game.Server;
import graviton.network.login.LoginClient;
import org.jooq.*;

import java.util.*;
import java.util.stream.Collectors;

import static graviton.database.utils.Tables.*;

/**
 * Created by Botan on 06/07/2015.
 */
public class Database implements Manageable {
    @Inject
    Injector injector;

    @Inject
    GlobalManager globalManager;

    private DSLContext dslContext;

    private List<String> bannedIp;

    public Database(DSLContext context) {
        this.dslContext = context;
        this.bannedIp = new ArrayList();
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

    @Override
    public void configure() {
        loadBannedIp();
        loadServers();
    }

    public void stop() {

    }

    /** Data **/

    public final Account loadAccount(String arguments, String password,String key) {
        Record record = getRecord(ACCOUNTS, ACCOUNTS.ACCOUNT.equal(arguments));

        if (record != null) {
            if(!encrypt(record.getValue(ACCOUNTS.PASSWORD),key).equals(password))
                return null;
            globalManager.checkAccount(record.getValue(ACCOUNTS.ID));
            return new Account(record.getValue(ACCOUNTS.ID),
                    record.getValue(ACCOUNTS.ACCOUNT), record.getValue(ACCOUNTS.PASSWORD),
                    record.getValue(ACCOUNTS.PSEUDO), record.getValue(ACCOUNTS.QUESTION), record.getValue(ACCOUNTS.RANK), injector);
        }
        return null;
    }

    public final Account loadApplicationAccount(String account, String password) {
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

    public void banIp(String ip) {
        this.dslContext.insertInto(BAN,BAN.IP).values(ip).execute();
        bannedIp.add(ip);
    }

    public void loadBannedIp() {
        for(Record record : getResult(BAN))
            bannedIp.add(record.getValue(BAN.IP));
    }

    public boolean isBanned(String ip) {
        if(!bannedIp.contains(ip))
            return false;
        return true;
    }

    public void loadPlayers(Account account) {
        Result<Record> result = getResult(PLAYERS, PLAYERS.ACCOUNT.equal(account.getId()));
        for (Record record : result)
            account.getPlayers().add(new Player(record.getValue(PLAYERS.ID), record.getValue(PLAYERS.NAME), record.getValue(PLAYERS.SERVER), injector));
    }

    public List<Player> getPlayers(String nickname) {
        Record result = getRecord(ACCOUNTS, ACCOUNTS.PSEUDO.equal(nickname));
        if (result != null)
            return getResult(PLAYERS, PLAYERS.ACCOUNT.equal(result.getValue(ACCOUNTS.ID))).stream().map(record -> new Player(record.getValue(PLAYERS.SERVER))).collect(Collectors.toList());
        else
            return null;
    }

    public void loadServers() {
        Map<Integer, Server> servers = new HashMap<>();
        Result<Record> result = getResult(SERVERS);
        for (Record record : result)
            servers.put(record.getValue(SERVERS.ID), new Server(record.getValue(SERVERS.ID), record.getValue(SERVERS.KEY), injector));
        globalManager.setServers(Collections.unmodifiableMap(servers));
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
}
