package graviton.network.game;

import com.google.inject.Inject;
import com.google.inject.Injector;
import graviton.api.InjectSetting;
import graviton.api.NetworkService;
import graviton.database.utils.game.Game;
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
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
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
        GameClient client = getClient(session);
        String packet = message.toString();

        if (client.getKey() != null && !packet.startsWith("Ak"))
            packet = decryptMessage(packet, client.getKey());

        parsePacket(client, packet);
        log.info("[Session {}] receives < {} [{}]", session.getId(), message.toString(), packet);
    }

    @Override
    public void messageSent(IoSession session, Object message) throws Exception {
        log.info("[Session {}] sends > {}", session.getId(), message.toString());
    }

    @Override
    public void inputClosed(IoSession session) throws Exception {
        session.close(true);
    }

    private GameClient getClient(IoSession session) {
        return (GameClient) session.getAttribute("client");
    }

    public void parsePacket(GameClient client, String packet) {
        String[] header = {packet.substring(0, 2), packet.substring(2)};
        try {
            packetManager.getPackets().get(header[0]).parse(client, header[1]);
        } catch (NullPointerException e) {
            if (packetManager.getPackets().containsKey(header[0]))
                log.error("unable to parse packet {}", packet , e);
            else
                log.error("Unknown packet {}", packet);
        }
    }

    public List<GameClient> getClients() {
        return acceptor.getManagedSessions().values().stream().map(session -> (GameClient) session.getAttribute("client")).collect(Collectors.toList());
    }

    private String decryptMessage(String message, String key) {
        int c = Integer.parseInt(Character.toString(message.charAt(1)), 16) * 2;
        StringBuilder builder = new StringBuilder();
        int j = 0;

        for (int i = 2; i < message.length(); i = i + 2)
            builder.append((char) (Integer.parseInt(message.substring(i, i + 2), 16) ^ key.charAt((j++ + c) % 16)));

        try {
            return URLDecoder.decode(builder.toString().replaceAll("%(?![0-9a-fA-F]{2})", "%25").replaceAll("\\+", "%2B"), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            log.error("unable to decrypt packet {} ", message, e);
            return "";
        }
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
