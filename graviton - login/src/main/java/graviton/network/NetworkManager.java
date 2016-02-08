package graviton.network;

import com.google.inject.Inject;
import graviton.api.NetworkService;
import graviton.network.application.ApplicationNetwork;
import graviton.network.exchange.ExchangeNetwork;
import graviton.network.login.LoginNetwork;
import lombok.Data;

import java.util.Arrays;
import java.util.List;

/**
 * Created by Botan on 07/07/2015.
 */
@Data
public class NetworkManager {
    private final List<NetworkService> services;

    @Inject
    public NetworkManager(ExchangeNetwork exchange, LoginNetwork login, ApplicationNetwork applicationNetwork) {
        this.services = Arrays.asList(login, exchange, applicationNetwork);
    }

    public void start() {
        services.forEach(NetworkService::start);
    }

    public void stop() {
        services.forEach(NetworkService::stop);
    }

}
