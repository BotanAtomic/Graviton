package graviton.network.game;

import com.google.inject.Inject;
import com.google.inject.Injector;
import graviton.common.Utils;
import graviton.game.client.Account;
import graviton.game.client.player.Player;
import lombok.Data;
import org.apache.mina.core.session.IoSession;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

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

    private String key;

    public GameClient(IoSession session, Injector injector) {
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

    public String generateKey() {
        String key = Utils.generateKey();
        this.key = prepareKey(key);
        return 1 + key;
    }

    private String prepareKey(String key) {
        String value = key;
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < key.length(); i += 2)
            builder.append((char) Integer.parseInt(key.substring(i, i + 2), 16));

        try {
            value =  URLDecoder.decode(builder.toString(), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return value;
    }
}