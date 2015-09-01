package graviton.game;

import graviton.login.Login;
import graviton.login.Main;
import graviton.network.exchange.ExchangeClient;
import lombok.Data;

/**
 * Created by Botan on 07/06/2015.
 */
@Data
public class Server {

    private int id, port;
    private State state;
    private String ip, key;
    private ExchangeClient client;

    public Server(int id, String key) {
        this.id = id;
        this.key = key;
        this.state = State.OFFLINE;
        Main.getInstance(Login.class).getServers().put(id,this);
    }

    public void setState(State state) {
        this.state = state;
    }

    public enum State {
        OFFLINE(0),
        ONLINE(1),
        SAVING(2);

        public int id;

        State(int id) {
            this.id = id;
        }
    }
}
