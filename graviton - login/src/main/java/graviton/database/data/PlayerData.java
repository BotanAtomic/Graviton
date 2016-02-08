package graviton.database.data;

import com.google.inject.Inject;
import com.google.inject.Injector;
import graviton.api.Data;
import graviton.database.Database;
import graviton.game.Account;
import graviton.game.Player;
import graviton.login.Configuration;
import lombok.extern.slf4j.Slf4j;
import org.jooq.Record;
import org.jooq.Result;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static graviton.database.utils.Tables.ACCOUNTS;
import static graviton.database.utils.Tables.PLAYERS;

/**
 * Created by Botan on 08/07/2015.
 */
@Slf4j
public class PlayerData extends Data {
    @Inject
    Injector injector;
    @Inject
    Configuration configuration;

    private Database database;

    @Override
    public void initialize() {
        this.database = configuration.getDatabase();
    }

    /**
     * load all players from account
     *
     * @param account
     */
    public void loadAll(Account account) {
        Result<Record> result = database.getResult(PLAYERS, PLAYERS.ACCOUNT.equal(account.getId()));
        for (Record record : result)
            account.getPlayers().add(new Player(record.getValue(PLAYERS.ID), record.getValue(PLAYERS.NAME), record.getValue(PLAYERS.SERVER), injector));
    }

    /**
     * System for search friends
     *
     * @param nickname
     * @return
     */
    public List<Player> getPlayers(String nickname) {
        List<Player> players = new ArrayList<>();
        Result<Record> result = database.getResult(PLAYERS, PLAYERS.ACCOUNT.equal(database.getContext().select(ACCOUNTS.ID).where(ACCOUNTS.PSEUDO.equal(nickname)).fetchOne().getValue(ACCOUNTS.ID)));
        players.addAll(result.stream().map(record -> new Player(record.getValue(PLAYERS.SERVER))).collect(Collectors.toList()));
        return players;
    }
}

