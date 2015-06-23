package graviton.core.injector;

import com.google.inject.AbstractModule;
import graviton.game.packet.PacketManager;
import graviton.network.NetworkManager;
import graviton.network.exchange.ExchangeNetworkService;
import graviton.network.game.GameNetworkService;

/**
 * Created by Botan on 16/06/2015.
 */
public class NetworkModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(NetworkManager.class).asEagerSingleton();
        bind(PacketManager.class).asEagerSingleton();
        bind(GameNetworkService.class).asEagerSingleton();
        bind(ExchangeNetworkService.class).asEagerSingleton();
    }
}
