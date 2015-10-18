package graviton.network.exchange;

import graviton.api.Client;
import graviton.game.Server;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.mina.core.session.IoSession;

/**
 * Created by Botan on 07/07/2015.
 */
@Data
@Slf4j
public class ExchangeClient implements Client {
    private final long id;
    private final IoSession session;

    private Server server;

    public ExchangeClient(IoSession session) {
        this.id = session.getId();
        this.session = session;
        send("?");
        manager.addClient(this);
    }

    public void addServer(int server, String key) {
        if (manager.getServers().get(server).getKey().equals(key)) {
            this.server = manager.getServers().get(server);
            this.server.setState(Server.State.ONLINE);
            this.server.setClient(this);
            send("I");
            return;
        }
        send("E");
    }

    @Override
    public void parsePacket(String packet) {
        String[] finalPacket = packet.substring(1).split("@");
        switch (packet.charAt(0)) {
            case 'S':
                addServer(Integer.parseInt(finalPacket[0]), finalPacket[1]);
                break;
            case 'I':
                server.setIp(finalPacket[0]);
                server.setPort(Integer.parseInt(finalPacket[1]));
                log.info("[(E)Session {}] server {} is ready to connect", session.getId(), server.getId());
                break;
            case 'C':
                server.setState(Server.getState(Integer.parseInt(finalPacket[0])));
            default:
                log.info("[Exchange] Packet server not found -> {}", packet);
        }
    }

    @Override
    public void kick() {
        server.setState(Server.State.OFFLINE);
        manager.removeClient(this);
        session.close(true);
    }

    @Override
    public void send(String packet) {
        session.write(cryptPacket(packet));
    }
}
