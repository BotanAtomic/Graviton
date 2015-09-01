package graviton.network.game;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import graviton.api.NetworkService;
import graviton.console.Console;
import graviton.core.Configuration;
import graviton.core.Main;
import graviton.game.GameManager;
import graviton.game.packet.PacketManager;
import lombok.Data;
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
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Botan on 17/06/2015.
 */
@Data
@Singleton
public class GameNetworkService implements IoHandler, NetworkService {
    @Inject
    private PacketManager packetManager;

    private final Console console;
    private final NioSocketAcceptor acceptor;
    private final GameManager manager;
    private final Calendar calendar;

    private final int port;
    private Map<Long, GameClient> clients;

    @Inject
    public GameNetworkService(Console console, Configuration configuration, GameManager manager) {
        this.acceptor = new NioSocketAcceptor();
        this.port = configuration.getGamePort();
        this.acceptor.getFilterChain().addLast("codec", new ProtocolCodecFilter(new TextLineCodecFactory(Charset.forName("UTF8"), LineDelimiter.NUL, new LineDelimiter("\n\0"))));
        this.acceptor.setHandler(this);
        this.clients = new ConcurrentHashMap<>();
        this.console = console;
        this.manager = manager;
        this.calendar = GregorianCalendar.getInstance();
    }

    @Override
    public void sessionCreated(IoSession session) throws Exception {
        clients.put(session.getId(), new GameClient(session));
        session.write("HG");
        console.println("[Session " + session.getId() + "] as created");
    }

    @Override
    public void sessionOpened(IoSession session) throws Exception {
        console.println("[Session " + session.getId() + "] opened...");
    }

    @Override
    public void sessionClosed(IoSession session) throws Exception {
        if (clients.containsKey(session.getId()))
            clients.remove(session);
        console.println("[Session " + session.getId() + "] closed...");
    }

    @Override
    public void sessionIdle(IoSession session, IdleStatus status) throws Exception {
        console.println("[Session " + session.getId() + "] idle...");
    }

    @Override
    public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
        console.println(cause.getMessage(), true);
    }

    @Override
    public void messageReceived(IoSession session, Object message) throws Exception {
        console.println("[Session " + session.getId() + "] recv < " + message.toString());
        checkPacket(clients.get(session.getId()), message.toString());
    }

    @Override
    public void messageSent(IoSession session, Object message) throws Exception {
        console.println("[Session " + session.getId() + "] send > " + message.toString());
    }

    @Override
    public void inputClosed(IoSession session) throws Exception {

    }

    private void checkPacket(GameClient client, String packet) {
        switch (packet.substring(0, 2)) {
            case "AL":
                client.send(client.getAccount().getPlayersPacket());
                break;
            case "AV":
                client.send("AV0");
                break;
            case "BD":
                client.send("BD" + calendar.get(Calendar.YEAR) + "|" + calendar.get(Calendar.MONTH) + "|" + calendar.get(Calendar.DAY_OF_MONTH));
                client.send("BT" + (new Date().getTime() + 3600000));
                break;
            case "Af":
                client.send("Af" + (1) + ("|") + (1) + ("|") + (1) + ("|") + (1) + ("|") + (1));
                break;
            case "GC":
                client.getPlayer().createGame();
                break;
            default:
                packetManager.parse(client,packet);
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

    }
}
