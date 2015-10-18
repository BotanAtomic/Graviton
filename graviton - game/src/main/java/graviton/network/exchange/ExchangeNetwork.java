package graviton.network.exchange;

import com.google.inject.Inject;
import graviton.api.NetworkService;
import graviton.core.Configuration;
import graviton.core.Main;
import graviton.database.DatabaseManager;
import graviton.game.GameManager;
import graviton.network.game.GameClient;
import graviton.network.game.GameNetwork;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.service.IoConnector;
import org.apache.mina.core.service.IoHandler;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.transport.socket.nio.NioSocketConnector;

import java.net.InetSocketAddress;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;

/**
 * Created by Botan on 17/06/2015.
 */
@Slf4j
public class ExchangeNetwork implements IoHandler, NetworkService {

    private final Configuration configuration;
    private final IoConnector connector;
    private final GameNetwork gameNetwork;

    private final String IP;
    private final int PORT;

    private final DatabaseManager databaseManager;

    private IoSession session;

    private long time;
    @Getter
    private long responseTime;

    @Inject
    public ExchangeNetwork(Configuration configuration, DatabaseManager databaseManager, GameNetwork gameNetwork) {
        this.IP = configuration.getExchangeIp();
        this.PORT = configuration.getExchangePort();
        this.connector = new NioSocketConnector();
        this.connector.setHandler(this);
        this.configuration = configuration;
        this.databaseManager = databaseManager;
        this.gameNetwork = gameNetwork;
    }

    @Override
    public void start() {
        connector.connect(new InetSocketAddress(IP, PORT));
     }

    @Override
    public void stop() {
        session.close(true);
        connector.dispose(false);
    }


    @Override
    public void sessionCreated(IoSession session) throws Exception {
        this.session = session;
    }

    @Override
    public void sessionOpened(IoSession session) throws Exception {

    }

    @Override
    public void sessionClosed(IoSession session) throws Exception {
        Main.close();
    }

    @Override
    public void sessionIdle(IoSession session, IdleStatus status) throws Exception {

    }

    @Override
    public void exceptionCaught(IoSession session, Throwable cause) throws Exception {

    }

    @Override
    public void messageReceived(IoSession session, Object message) throws Exception {
        String packet = decryptPacket(message);
        if (packet.equals("PONG")) {
            responseTime = (System.currentTimeMillis() - time);
            return;
        }
        parse(packet);
    }

    @Override
    public void messageSent(IoSession session, Object message) throws Exception {
    }

    @Override
    public void inputClosed(IoSession session) throws Exception {
        Main.close();
    }

    public void send(String packet) {
        this.session.write(cryptPacket(packet));
    }

    private IoBuffer cryptPacket(String packet) {
        return IoBuffer.allocate(2048).put(packet.getBytes()).flip();
    }

    private String decryptPacket(Object o) {
        IoBuffer buffer = IoBuffer.allocate(2048).put((IoBuffer) o).flip();
        CharsetDecoder decoder = Charset.forName("UTF-8").newDecoder();
        try {
            return buffer.getString(decoder);
        } catch (CharacterCodingException e) {
            return "undefined";
        }
    }

    private void parse(String packet) {
        switch (packet.charAt(0)) {
            case '?':
                send("S" + configuration.getServerId() + "@" + configuration.getServerKey());
                break;
            case 'E':
                Main.close();
                break;
            case 'R':
                System.exit(0);
                break;
            case 'I':
                send("I" + configuration.getIp() + "@" + configuration.getGamePort());
                break;
            case 'S':
                Main.getInstance(GameManager.class).save();
                break;
            case '+':
                databaseManager.loadAccount(Integer.parseInt(packet.substring(1)));
                break;
            case '-':
                for (GameClient client : gameNetwork.getClients().values())
                    if (client.getAccount().getId() == Integer.parseInt(packet.substring(1))) {
                        client.send("AlEa");
                        client.getSession().close(true);
                    }
                break;
            default:
                log.info("Undefined packet : {}", packet);
        }
    }

    public ExchangeNetwork launchPing() {
        time = System.currentTimeMillis();
        send("PING");
        return this;
    }
}

