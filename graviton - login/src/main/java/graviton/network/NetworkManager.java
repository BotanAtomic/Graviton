package graviton.network;

import com.google.inject.Inject;
import graviton.api.NetworkService;
import graviton.network.application.ApplicationNetwork;
import graviton.network.exchange.ExchangeNetwork;
import graviton.network.login.LoginNetwork;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Botan on 07/07/2015.
 */
@Data
public class NetworkManager {
    private List<NetworkService> services;

    @Inject
    public NetworkManager(ExchangeNetwork exchange,LoginNetwork login,ApplicationNetwork application) {
        this.services = asList(login,exchange,application);
    }

    public void start() {
        services.forEach(NetworkService::start);
    }

    public void stop() {
        services.forEach(NetworkService::stop);
    }

    private List<NetworkService> asList(NetworkService...n) {
        List<NetworkService> services = new ArrayList<>();
        for(NetworkService service : n)
            services.add(service);
        return services;
    }
}
