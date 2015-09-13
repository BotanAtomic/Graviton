package graviton.network.exchange;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import graviton.api.NetworkService;
import graviton.core.Configuration;
import graviton.core.Main;
import graviton.database.DatabaseManager;
import graviton.enums.DataType;
import graviton.network.game.GameClient;
import graviton.network.game.GameNetworkService;
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
@Singleton
public class ExchangeNetworkService implements IoHandler,NetworkService {

    private final Configuration configuration;
    private final IoConnector connector;

    private final String ip;
    private final int port;

    private final DatabaseManager databaseManager;
    private final GameNetworkService gameNetworkService;

    private IoSession session;

    @Inject
    public ExchangeNetworkService(Configuration configuration,DatabaseManager databaseManager,GameNetworkService gameNetworkService) {
        this.ip = configuration.getExchangeIp();
        this.port = configuration.getExchangePort();
        this.connector = new NioSocketConnector();
        this.connector.setHandler(this);
        this.configuration = configuration;
        this.databaseManager = databaseManager;
        this.gameNetworkService = gameNetworkService;
    }

    @Override
    public void start() {
        connector.connect(new InetSocketAddress(ip, port));
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
        System.exit(1);
    }

    @Override
    public void sessionIdle(IoSession session, IdleStatus status) throws Exception {

    }

    @Override
    public void exceptionCaught(IoSession session, Throwable cause) throws Exception {

    }

    @Override
    public void messageReceived(IoSession session, Object message) throws Exception {
        parse(decryptPacket(message));
    }

    @Override
    public void messageSent(IoSession session, Object message) throws Exception {
    }

    @Override
    public void inputClosed(IoSession session) throws Exception {

    }

    public void send(String packet) {
        this.session.write(cryptPacket(packet));
    }

    private IoBuffer cryptPacket(String packet) {
        IoBuffer ioBuffer = IoBuffer.allocate(2048);
        ioBuffer.put(packet.getBytes());
        ioBuffer.flip();
        return ioBuffer;
    }

    private String decryptPacket(Object o) {
        IoBuffer buffer = IoBuffer.allocate(2048);
        buffer.put((IoBuffer) o);
        buffer.flip();
        CharsetDecoder cd = Charset.forName("UTF-8").newDecoder();
        try {
            return buffer.getString(cd);
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
                System.exit(1);
                break;
            case 'I':
                send("I" + configuration.getIp() + "@" + configuration.getGamePort());
                break;
            case '+':
                databaseManager.getData().get(DataType.ACCOUNT).load(Integer.parseInt(packet.substring(1)));
                break;
            case '-':
                int id = Integer.parseInt(packet.substring(1));
                for (GameClient client : Main.getInstance(GameNetworkService.class).getClients().values()) { //TODO : lambda
                    if (client.getAccount() != null)
                        if (client.getAccount().getId() == id) {
                            client.kick();
                            break;
                        }
                }
                break;
            default:
                System.err.println("Undefined packet : " + packet);
        }
    }

}

