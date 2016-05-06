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
    private final long id;
    private final IoSession session;
    @Inject
    GameNetwork gameNetwork;
    private Account account;
    private Player currentPlayer;

    public GameClient(IoSession session,Injector injector) {
        session.write("HG");
        injector.injectMembers(this);
        this.id = session.getId();
        this.session = session;
    }

    public void kick() {
        account.close();
    }

    public void send(String packet) {
        session.write(packet);
    }
}
