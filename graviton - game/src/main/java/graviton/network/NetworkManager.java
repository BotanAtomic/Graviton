package graviton.network;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import graviton.api.Manager;
import graviton.api.NetworkService;
import graviton.network.exchange.ExchangeNetworkService;
import graviton.network.game.GameNetworkService;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Botan on 16/06/2015.
 */
@Singleton
public class NetworkManager implements Manager {

    private final List<NetworkService> services;

    @Inject
    public NetworkManager(ExchangeNetworkService exchangeNetworkService, GameNetworkService gameNetworkService) {
        this.services = asList(exchangeNetworkService, gameNetworkService);
    }

    @Override
    public void configure() {
        services.stream().forEach(NetworkService::start);
    }

    @Override
    public void stop() {
        services.stream().forEach(NetworkService::stop);
    }

    private List<NetworkService> asList(NetworkService... a) {
        List<NetworkService> services = new ArrayList<>();
        for(NetworkService service : a)
            services.add(service);
        return services;
    }
}
