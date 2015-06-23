package graviton.network.common;

import graviton.api.NetworkService;
import org.apache.mina.core.service.IoAcceptor;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;

import java.io.IOException;
import java.net.InetSocketAddress;

/**
 * Created by Botan on 20/06/2015.
 */
public abstract class NetworkAcceptor implements NetworkService {
    protected final IoAcceptor acceptor;

    private int port;

    protected NetworkAcceptor(int port) {
        this.port = port;
        this.acceptor = new NioSocketAcceptor();
        configure();
    }

    protected abstract void configure();

    @Override
    public void start() {
        try {
            acceptor.bind(new InetSocketAddress(port));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void stop() {
        acceptor.getManagedSessions().values().stream().filter(session -> !session.isClosing()).forEach(session -> session.close(false));
        acceptor.dispose(false);
    }
}
