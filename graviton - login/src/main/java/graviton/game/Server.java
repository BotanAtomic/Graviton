package graviton.game;

import com.google.inject.Inject;
import com.google.inject.Injector;
import graviton.core.GlobalManager;
import graviton.network.exchange.ExchangeClient;
import lombok.Data;

/**
 * Created by Botan on 07/06/2015.
 */
@Data
public class Server {
    @Inject
    GlobalManager globalManager;

    private int port, id;

    private byte state;
    private String ip, key;

    private ExchangeClient client;

    public Server(int id, String key,Injector injector) {
        injector.injectMembers(this);
        this.id = id;
        this.key = key;
        this.state = 0;
    }

    public void setState(byte state) {
        this.state = state;
        globalManager.getLoginClients().forEach(client -> client.write(globalManager.getHostList()));
    }

    public final void send(String packet) {
        client.send(packet);
    }

}
