package graviton.network.game;

import graviton.game.client.Account;
import graviton.game.client.player.Player;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.apache.mina.core.session.IoSession;

/**
 * Created by Botan on 16/06/2015.
 */
@Data
public class GameClient {

    private final long id;
    private final IoSession session;

    private Account account;
    private Player player;

    public GameClient(IoSession session) {
        this.id = session.getId();
        this.session = session;
    }

    public void kick() {
        session.close(true);
    }

    public void send(String packet) {
        session.write(packet);
    }
}
