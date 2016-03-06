package graviton.login.modules;

import com.google.inject.AbstractModule;
import graviton.database.data.AccountData;
import graviton.database.data.PlayerData;
import graviton.database.data.ServerData;
import graviton.network.NetworkManager;
import graviton.network.application.ApplicationNetwork;
import graviton.network.exchange.ExchangeNetwork;

/**
 * Created by Botan on 06/06/2015.
 */
public class NetworkModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(NetworkManager.class).asEagerSingleton();
        bind(ExchangeNetwork.class).asEagerSingleton();
        bind(ApplicationNetwork.class).asEagerSingleton();

        bind(AccountData.class).asEagerSingleton();
        bind(PlayerData.class).asEagerSingleton();
        bind(ServerData.class).asEagerSingleton();
    }
}
