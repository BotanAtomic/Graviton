package graviton.game;

import graviton.login.Main;
import graviton.login.Manager;
import graviton.network.exchange.ExchangeClient;
import lombok.Getter;
import lombok.Setter;

/**
 * Created by Botan on 07/06/2015.
 */
public class Server {
    private final Manager manager = Main.getInstance(Manager.class);
    @Getter
    @Setter
    private int id, port;
    @Getter
    @Setter
    private State state;
    @Getter
    @Setter
    private String ip, key;
    @Getter
    @Setter
    private ExchangeClient client;

    public Server(int id, String key) {
        this.id = id;
        this.key = key;
        this.state = State.OFFLINE;
        manager.getServers().put(id, this);
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
