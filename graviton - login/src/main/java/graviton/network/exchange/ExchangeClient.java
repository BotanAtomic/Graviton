package graviton.network.exchange;

import graviton.api.Client;
import graviton.game.Server;
import lombok.Data;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;

/**
 * Created by Botan on 07/07/2015.
 */
@Data
public class ExchangeClient implements Client {
    private final long id;
    private final IoSession session;

    private Server server;

    public ExchangeClient(IoSession session, int serverID, String key) {
        this.id = serverID;
        this.session = session;
        this.login.addClient(this);
        if (login.getServers().get(serverID).getKey().equals(key)) {
            this.server = login.getServers().get(serverID);
            this.server.setState(Server.State.ONLINE);
        }
    }

    @Override
    public void parsePacket(String packet) throws Exception {

    }

    @Override
    public void kick() {
        login.removeClient(this);
        session.close(true);
    }

    @Override
    public void send(String packet) {
        IoBuffer ioBuffer = IoBuffer.allocate(2048);
        ioBuffer.put(packet.getBytes());
        ioBuffer.flip();
        session.write(ioBuffer);
    }
}
