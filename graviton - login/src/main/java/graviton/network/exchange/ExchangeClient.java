package graviton.network.exchange;

import graviton.api.Client;
import graviton.game.Server;
import lombok.Data;
import org.apache.mina.core.session.IoSession;

/**
 * Created by Botan on 07/07/2015.
 */
@Data
public class ExchangeClient implements Client {
    private final long id;
    private final IoSession session;

    private Server server;

    public ExchangeClient(IoSession session) {
        this.id = session.getId();
        this.session = session;
    }

    @Override
    public void parsePacket(String packet) throws Exception{

    }
}
