package graviton.core.injector;

import com.google.inject.AbstractModule;
import graviton.game.packet.PacketManager;
import graviton.network.NetworkManager;
import graviton.network.exchange.ExchangeNetwork;
import graviton.network.game.GameNetwork;

/**
 * Created by Botan on 16/06/2015.
 */
public class NetworkModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(NetworkManager.class).asEagerSingleton();
        bind(PacketManager.class).asEagerSingleton();
        bind(GameNetwork.class).asEagerSingleton();
        bind(ExchangeNetwork.class).asEagerSingleton();
    }
}
