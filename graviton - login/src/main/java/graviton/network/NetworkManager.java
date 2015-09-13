package graviton.network;

import com.google.inject.Inject;
import graviton.api.NetworkService;
import graviton.network.exchange.ExchangeNetwork;
import graviton.network.login.LoginNetwork;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Botan on 07/07/2015.
 */
@Data
//
public class NetworkManager {
    private List<NetworkService> services;

    @Inject
    public NetworkManager(ExchangeNetwork exchange, LoginNetwork login) {
        this.services = asList(login, exchange);
    }

    public final void start() {
        services.forEach(NetworkService::start);
    }

    public void stop() {
        services.forEach(NetworkService::stop);
    }

    /**
     * @param networks
     * @return
     */
    private List<NetworkService> asList(NetworkService... networks) {
        List<NetworkService> services = new ArrayList<>();
        for (NetworkService service : networks)
            services.add(service);
        return services;
    }
}
