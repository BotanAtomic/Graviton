package graviton.network.game;

import com.google.inject.Inject;
import com.google.inject.Injector;
import graviton.api.InjectSetting;
import graviton.api.NetworkService;
import graviton.network.PacketManager;
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
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Botan on 17/06/2015.
 */
@Slf4j
public class GameNetwork implements IoHandler, NetworkService {
    private final NioSocketAcceptor acceptor;
    @Inject
    private Injector injector;
    @Inject
    private PacketManager packetManager;
    @InjectSetting("server.port")
    private int port;


    public GameNetwork() {
        this.acceptor = new NioSocketAcceptor();
        this.acceptor.getFilterChain().addLast("codec", new ProtocolCodecFilter(new TextLineCodecFactory(Charset.forName("UTF-8"), LineDelimiter.NUL, new LineDelimiter("\n\0"))));
        this.acceptor.setHandler(this);
    }

    @Override
    public void sessionCreated(IoSession session) throws Exception {
        session.setAttribute("client", new GameClient(session, injector));
        log.info("[Session {}] created", session.getId());
    }

    @Override
    public void sessionOpened(IoSession session) throws Exception {
        log.info("[Session {}] opened", session.getId());
    }

    @Override
    public void sessionClosed(IoSession session) throws Exception {
        getClient(session).kick();
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
        parsePacket(getClient(session), message.toString());
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

    private GameClient getClient(IoSession session) {
        return (GameClient) session.getAttribute("client");
    }

    public void parsePacket(GameClient client, String packet) {
        String[] header = {packet.substring(0, 2),packet.substring(2)};
        if (packetManager.getPackets().containsKey(header[0]))
            packetManager.getPackets().get(header[0]).parse(client, header[1]);
        else
            log.error("Unknown packet {}", packet);
    }

    public List<GameClient> getClients() {
        return acceptor.getManagedSessions().values().stream().map(session -> (GameClient) session.getAttribute("client")).collect(Collectors.toList());
    }

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
        acceptor.getManagedSessions().values().forEach(session -> session.close(true));
        acceptor.unbind();
        acceptor.dispose();
    }
}
