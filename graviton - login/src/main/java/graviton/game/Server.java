package graviton.game;

import graviton.network.NetworkManager;
import graviton.network.exchange.ExchangeClient;
import lombok.Getter;
import lombok.Setter;

/**
 * Created by Botan on 07/06/2015.
 */
public class Server {
    private NetworkManager manager;

    @Getter
    @Setter
    private int id, port;
    @Getter private int state;
    @Getter
    @Setter
    private String ip, key;
    @Getter
    @Setter
    private ExchangeClient client;

    public Server(int id, String key, NetworkManager networkManager) {
        this.manager = networkManager;
        this.id = id;
        this.key = key;
        this.state = 0;
    }

    public void setState(int state) {
        this.state = state;
        manager.sendToAll(manager.getConfig().getDatabase().getServerData().getHostList());
    }
    public void send(Object arg0) {
        if(arg0 instanceof String) {
            this.getClient().send((String) arg0);
        } else {
            this.getClient().getSession().write(arg0);
        }
    }
}
