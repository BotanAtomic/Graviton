package graviton.network;

import com.google.inject.Inject;
import graviton.api.Manager;
import graviton.api.NetworkService;
import graviton.network.exchange.ExchangeNetworkService;
import graviton.network.game.GameNetworkService;

import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Botan on 16/06/2015.
 */
@Singleton
public class NetworkManager implements Manager {

    private final List<NetworkService> services;

    @Inject
    public NetworkManager(ExchangeNetworkService exchangeNetworkService, GameNetworkService gameNetworkService) {
        services = getNewList(exchangeNetworkService, gameNetworkService);
    }

    @Override
    public void configure() {
        services.stream().forEach(NetworkService::start);
    }

    @Override
    public void stop() {
        services.stream().forEach(NetworkService::stop);
    }

    private List<NetworkService> getNewList(NetworkService... a) {
        List<NetworkService> services = new ArrayList<>();
        Collections.addAll(services, a);
        return services;
    }
}
