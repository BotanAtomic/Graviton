package graviton.login.modules;

import com.google.inject.AbstractModule;
import graviton.network.NetworkManager;
import graviton.network.exchange.ExchangeNetwork;

/**
 * Created by Botan on 06/06/2015.
 */
public class NetworkModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(NetworkManager.class).asEagerSingleton();
        bind(ExchangeNetwork.class).asEagerSingleton();
    }
}
