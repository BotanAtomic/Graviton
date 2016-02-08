package graviton.network.game;

import com.google.inject.Inject;
import com.google.inject.Injector;
import graviton.api.NetworkService;
import graviton.core.Configuration;
import graviton.game.PacketManager;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.mina.core.service.IoHandler;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.textline.LineDelimiter;
import org.apache.mina.filter.codec.textline.TextLineCodecFactory;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Botan on 17/06/2015.
 */
@Slf4j
public class GameNetwork implements IoHandler, NetworkService {
    @Inject
    private Injector injector;
    @Inject
    private PacketManager packetManager;

    private final NioSocketAcceptor acceptor;
    private final int PORT;
    @Getter
    private final Map<Long, GameClient> clients;

    @Inject
    public GameNetwork(Configuration configuration) {
        this.acceptor = new NioSocketAcceptor();
        this.PORT = configuration.getGamePort();
        this.acceptor.getFilterChain().addLast("codec", new ProtocolCodecFilter(new TextLineCodecFactory(Charset.forName("UTF-8"), LineDelimiter.NUL, new LineDelimiter("\n\0"))));
        this.acceptor.setHandler(this);
        this.clients = new ConcurrentHashMap<>();
    }

    @Override
    public void sessionCreated(IoSession session) throws Exception {
        new GameClient(session,injector);
        log.info("[Session {}] created", session.getId());
    }

    @Override
    public void sessionOpened(IoSession session) throws Exception {
        log.info("[Session {}] opened", session.getId());
    }

    @Override
    public void sessionClosed(IoSession session) throws Exception {
        getClient(session.getId()).kick();
        log.info("[Session {}] closed", session.getId());
    }

    @Override
    public void sessionIdle(IoSession session, IdleStatus status) throws Exception {
        log.info("[Session {}] idle", session.getId());
    }

    @Override
    public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
        log.error("[Session {}] has encountered an error : {}", session.getId(), cause);
    }

    @Override
    public void messageReceived(IoSession session, Object message) throws Exception {
        packetManager.parse(clients.get(session.getId()), message.toString());
        log.info("[Session {}] recev < {}", session.getId(), message.toString());
    }

    @Override
    public void messageSent(IoSession session, Object message) throws Exception {
        log.info("[Session {}] send > {}", session.getId(), message.toString());
    }

    @Override
    public void inputClosed(IoSession session) throws Exception {
        session.close(true);
    }

    public void addClient(GameClient client) {
        this.clients.put(client.getId(), client);
    }

    public void removeClient(GameClient client) {
        this.clients.remove(client.getId());
    }

    private GameClient getClient(long id) {
        if (clients.get(id) != null)
            return clients.get(id);
        return null;
    }

    @Override
    public void start() {
        try {
            acceptor.bind(new InetSocketAddress(PORT));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void stop() {
        acceptor.unbind();
        acceptor.dispose();
        clients.values().forEach(client -> client.getSession().close(true));
        clients.clear();
    }
}
