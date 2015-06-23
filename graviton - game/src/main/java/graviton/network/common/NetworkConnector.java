package graviton.network.common;

import graviton.api.NetworkService;
import org.apache.mina.core.service.IoConnector;
import org.apache.mina.transport.socket.nio.NioSocketConnector;

import java.net.InetSocketAddress;

/**
 * Created by Botan on 20/06/2015.
 */
public abstract class NetworkConnector implements NetworkService {
    protected final IoConnector connector;

    private String ip;
    private int port;

    protected NetworkConnector(String ip, int port) {
        this.ip = ip;
        this.port = port;
        this.connector = new NioSocketConnector();
        configure();
    }

    protected abstract void configure();

    @Override
    public void start() {
        connector.connect(new InetSocketAddress(ip, port));
    }

    @Override
    public void stop() {
        connector.getManagedSessions().values().stream().filter(session -> !session.isClosing()).forEach(session -> session.close(false));
        connector.dispose(false);
    }
}
