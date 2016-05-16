package graviton.factory.type;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.name.Named;
import graviton.api.Factory;
import graviton.database.Database;
import graviton.enums.DataType;
import graviton.factory.FactoryManager;
import graviton.game.client.Account;
import graviton.game.trunk.Trunk;
import lombok.extern.slf4j.Slf4j;
import org.jooq.Record;
import org.jooq.UpdateSetFirstStep;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static graviton.database.utils.login.Tables.ACCOUNTS;
import static graviton.database.utils.game.Tables.BANKS;

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
    public AccountFactory(@Named("database.login") Database database, FactoryManager factoryManager) {
        super(database);
        factoryManager.addFactory(this);
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

    public Trunk loadBank(int account) {
        Trunk bank;

        Record record = database.getRecord(BANKS, BANKS.ACCOUNT.equal(account));
        if (record != null)
            bank = new Trunk(record.getValue(BANKS.DATA), injector);
        else
            bank = new Trunk("", injector);

        return bank;
    }

    private void saveBank(Account account) {
        UpdateSetFirstStep firstStep = database.getDSLContext().update(BANKS);

        if (firstStep.set(BANKS.DATA, account.getBank().getKamas() + ":" + account.getBank().parseToDatabase()).where(BANKS.ACCOUNT.equal(account.getId())).execute() == 0)
            database.getDSLContext().insertInto(BANKS, BANKS.ACCOUNT, BANKS.DATA).values(account.getId(), account.getBank().getKamas() + ":" + account.getBank().parseToDatabase()).execute();

    }

    public void update(Account account) {
        UpdateSetFirstStep firstStep = database.getDSLContext().update(ACCOUNTS);

        if (!account.getFriends().isEmpty())
            firstStep.set(ACCOUNTS.FRIENDS, account.parseList(true));
        if (!account.getEnemies().isEmpty())
            firstStep.set(ACCOUNTS.ENEMIES, account.parseList(false));
        if (!account.getBank().isEmpty())
            saveBank(account);

        firstStep.set(ACCOUNTS.RANK, account.getRank().id);
        firstStep.set(ACCOUNTS.INFORMATIONS, account.getNewInformations());

        firstStep.set(ACCOUNTS.MUTE, account.parseMute()).where(ACCOUNTS.ID.equal(account.getId())).execute();
    }

    @Override
    public Map<Integer, Account> getElements() {
        return this.accounts;
    }

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
