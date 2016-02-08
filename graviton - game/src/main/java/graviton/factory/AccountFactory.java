package graviton.factory;

import com.google.inject.Inject;
import com.google.inject.Injector;
import graviton.api.Factory;
import graviton.core.Configuration;
import graviton.enums.DataType;
import graviton.enums.DatabaseType;
import graviton.game.client.Account;
import lombok.extern.slf4j.Slf4j;
import org.jooq.Record;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static graviton.database.utils.login.Tables.ACCOUNTS;

/**
 * Created by Botan on 25/12/2015.
 */
@Slf4j
public class AccountFactory extends Factory<Account> {
    @Inject
    Injector injector;
    @Inject
    Configuration configuration;

    private final Map<Integer, Account> accounts;

    public AccountFactory() {
        super(DatabaseType.LOGIN);
        this.accounts = new ConcurrentHashMap<>();
    }

    public Account load(int id) {
        Record record = configuration.getLoginDatabase().getRecord(ACCOUNTS, ACCOUNTS.ID.equal(id));
        if (record != null)
            return new Account(record.getValue(ACCOUNTS.ID), record.getValue(ACCOUNTS.ANSWER), record.getValue(ACCOUNTS.PSEUDO), record.getValue(ACCOUNTS.RANK), injector);
        return null;
    }

    @Override
    public Map<Integer, Account> getElements() {
        return this.accounts;
    }

    @Override
    public Account get(Object object) {
        if (!this.accounts.containsKey(object))
            return load((int) object);
        return this.accounts.get(object);
    }

    @Override
    public DataType getType() {
        return DataType.ACCOUNT;
    }

    @Override
    public void configure() {
        super.configureDatabase();
    }

}
