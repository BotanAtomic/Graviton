package graviton.network;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import graviton.api.Manager;
import graviton.api.NetworkService;
import graviton.network.exchange.ExchangeNetwork;
import graviton.network.game.GameNetwork;

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
    public NetworkManager(ExchangeNetwork exchangeNetwork, GameNetwork gameNetwork) {
        this.services = asList(exchangeNetwork, gameNetwork);
    }

    @Override
    public void start() {
        services.forEach(NetworkService::start);
    }

    @Override
    public void stop() {
        services.forEach(NetworkService::stop);
    }

    private final List<NetworkService> asList(NetworkService... networks) {
        List<NetworkService> services = new ArrayList<>();
        Collections.addAll(services, networks);
        return services;
    }
}
