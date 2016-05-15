package graviton.factory;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.name.Named;
import graviton.api.Factory;
import graviton.database.Database;
import graviton.enums.DataType;
import graviton.game.client.Account;
import lombok.extern.slf4j.Slf4j;
import org.jooq.Record;
import org.jooq.UpdateSetFirstStep;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static graviton.database.utils.login.Tables.ACCOUNTS;

/**
 * Created by Botan on 25/12/2015.
 */
@Slf4j
public class AccountFactory extends Factory<Account> {
    private final Map<Integer, Account> accounts;
    @Inject
    Injector injector;

    @Inject
    PlayerFactory playerFactory;

    @Inject
    public AccountFactory(@Named("database.login") Database database) {
        super(database);
        this.accounts = new ConcurrentHashMap<>();
    }

    public Account load(int id) {
        Record record = database.getRecord(ACCOUNTS, ACCOUNTS.ID.equal(id));
        return record == null ? null : new Account(record, playerFactory, this, injector);
    }

    private Account load(String name) {
        Record record = database.getRecord(ACCOUNTS, ACCOUNTS.PSEUDO.equal(name));
        return record == null ? null : new Account(record, playerFactory, this, injector);
    }

    public void update(Account account) {
        UpdateSetFirstStep firstStep = database.getDSLContext().update(ACCOUNTS);
        firstStep.set(ACCOUNTS.FRIENDS, account.parseList(true));
        firstStep.set(ACCOUNTS.ENEMIES, account.parseList(false));
        firstStep.set(ACCOUNTS.RANK, account.getRank().id);
        firstStep.set(ACCOUNTS.INFORMATIONS, account.getNewInformations());
        firstStep.set(ACCOUNTS.BANK, account.getBank().getKamas() + ";" + account.getBank().parseToDatabase());
        firstStep.set(ACCOUNTS.MUTE, account.parseMute()).where(ACCOUNTS.ID.equal(account.getId())).execute();
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

    public Account getByName(String name) {
        for (Account account : this.accounts.values())
            if (account.getPseudo().equals(name))
                return account;
        return this.load(name);
    }

    @Override
    public DataType getType() {
        return DataType.ACCOUNT;
    }

    @Override
    public void configure() {

    }

    @Override
    public void save() {
        log.debug("saving accounts...");
        this.accounts.values().forEach(account -> update(account));
    }

}
