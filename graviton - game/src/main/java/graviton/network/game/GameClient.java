package graviton.network.game;

import com.google.inject.Inject;
import com.google.inject.Injector;
import graviton.game.client.Account;
import graviton.game.client.player.Player;
import lombok.Data;
import org.apache.mina.core.session.IoSession;

/**
 * Created by Botan on 16/06/2015.
 */
@Data
public class GameClient {
    @Inject
    GameNetwork gameNetwork;

    private final long id;
    private final IoSession session;

    private Account account;
    private Player currentPlayer;

    public GameClient(IoSession session,Injector injector) {
        injector.injectMembers(this);
        session.write("HG");
        this.id = session.getId();
        this.session = session;
        gameNetwork.addClient(this);
    }

    public void kick() {
        account.close();
        gameNetwork.removeClient(this);
        session.close(true);
    }

    public void send(String packet) {
        session.write(packet);
    }
}
