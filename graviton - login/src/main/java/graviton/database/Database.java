package graviton.database;

import graviton.database.data.AccountData;
import graviton.database.data.PlayerData;
import graviton.database.data.ServerData;
import lombok.Getter;

/**
 * Created by Botan on 06/06/2015.
 */
public final class Database {
    @Getter private final String host, name, user, pass;
    /**
     * All data
     **/
    @Getter private AccountData accountData;
    @Getter private PlayerData playerData;
    @Getter private ServerData serverData;

    public Database(String host, String name, String user,String pass) {
        this.host = host;
        this.name = name;
        this.user = user;
        this.pass = pass;
    }

    public void configure(DatabaseManager manager) {
        this.accountData = new AccountData(manager);
        this.playerData = new PlayerData(manager);
        this.serverData = new ServerData(manager);
    }

}
