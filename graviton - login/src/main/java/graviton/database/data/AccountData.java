package graviton.database.data;

import com.google.inject.Inject;
import com.google.inject.Injector;
import graviton.api.Data;
import graviton.database.Database;
import graviton.game.Account;
import graviton.login.Configuration;
import graviton.login.Manager;
import graviton.network.login.LoginClient;
import lombok.extern.slf4j.Slf4j;
import org.jooq.Record;

import static graviton.database.utils.Tables.ACCOUNTS;

/**
 * Created by Botan on 08/07/2015.
 */
@Slf4j
public class AccountData extends Data {
    @Inject
    Injector injector;
    @Inject
    Configuration configuration;
    @Inject
    Manager manager;

    private Database database;

    @Override
    public void initialize() {
        this.database = configuration.getDatabase();
    }

    public boolean isGood(String username, String password, LoginClient client) {
        Record record = database.getRecord(ACCOUNTS, ACCOUNTS.ACCOUNT.equal(username));
        if (record != null)
            if (encrypt(record.getValue(ACCOUNTS.PASSWORD), client.getKey()).equals(password))
                return true;
        return false;
    }

    public final Account load(String arguments) {
        Record record = database.getRecord(ACCOUNTS, ACCOUNTS.ACCOUNT.equal(arguments));

        if (record != null) {
            manager.checkAccount(record.getValue(ACCOUNTS.ID));
            return new Account(record.getValue(ACCOUNTS.ID),
                    record.getValue(ACCOUNTS.ACCOUNT), record.getValue(ACCOUNTS.PASSWORD),
                    record.getValue(ACCOUNTS.PSEUDO), record.getValue(ACCOUNTS.QUESTION), record.getValue(ACCOUNTS.RANK), injector);
        }
        return null;
    }

    public final Account load(String account, String password) {
        Record record = database.getRecord(ACCOUNTS, ACCOUNTS.ACCOUNT.equal(account), ACCOUNTS.PASSWORD.equal(password));
        if (record != null)
            return new Account(record.getValue(ACCOUNTS.ACCOUNT), record.getValue(ACCOUNTS.RANK), injector);
        return null;
    }

    public void updateNickname(Account account) {
        database.getContext().update(ACCOUNTS).set(ACCOUNTS.PSEUDO, account.getPseudo()).where(ACCOUNTS.ID.equal(account.getId()));
    }

    public boolean isAvaiableNickname(String nickname) {
        Record record = database.getRecord(ACCOUNTS, ACCOUNTS.PSEUDO.equal(nickname));
        return record != null;
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

