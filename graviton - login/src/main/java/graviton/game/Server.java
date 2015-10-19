package graviton.game;

import com.google.inject.Inject;
import com.google.inject.Injector;
import graviton.login.Manager;
import graviton.network.exchange.ExchangeClient;
import lombok.Data;

/**
 * Created by Botan on 07/06/2015.
 */
@Data
public class Server {
    @Inject
    Manager manager;

    private int id, port;
    private State state;
    private String ip, key;
    private ExchangeClient client;

    public Server(int id, String key,Injector injector) {
        injector.injectMembers(this);
        this.id = id;
        this.key = key;
        this.state = State.OFFLINE;
    }

    public static State getState(int id) {
        switch (id) {
            case 0:
                return State.OFFLINE;
            case 1:
                return State.ONLINE;
            case 2:
                return State.SAVING;
            default:
                return null;
        }
    }

    public void setState(State state) {
        this.state = state;
        String hostList = manager.getHostList();
        manager.getLoginClients().forEach(client -> client.send(hostList));
    }

    public final void send(String packet) {
        client.send(packet);
    }

    public enum State {
        OFFLINE(0),
        ONLINE(1),
        SAVING(2);

        public final int id;

        State(int id) {
            this.id = id;
        }
    }
}
