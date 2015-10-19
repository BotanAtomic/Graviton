package graviton.network.exchange;

import com.google.inject.Inject;
import com.google.inject.Injector;
import graviton.api.Client;
import graviton.game.Server;
import graviton.login.Manager;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.mina.core.session.IoSession;

/**
 * Created by Botan on 07/07/2015.
 */
@Data
@Slf4j
public class ExchangeClient implements Client {
    @Inject
    Manager manager;

    private final long id;
    private final IoSession session;

    private Server server;

    public ExchangeClient(IoSession session,Injector injector) {
        injector.injectMembers(this);
        this.id = session.getId();
        this.session = session;
        manager.addClient(this);
        send("?");
    }

    public void addServer(int server, String key) {
        System.err.println("Addind");
        if (manager.getServers().get(server).getKey().equals(key)) {
            System.err.println("Addind2");
            this.server = manager.getServers().get(server);
            System.err.println("Addind3");
            this.server.setState(Server.State.ONLINE);
            System.err.println("Addind4");
            this.server.setClient(this);
            System.err.println("Addind5");
            send("I");
            System.err.println("Addind6");
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
