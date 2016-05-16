package graviton.network.exchange;

import com.google.inject.Inject;
import graviton.api.InjectSetting;
import graviton.api.NetworkService;
import graviton.common.Scanner;
import graviton.factory.type.AccountFactory;
import graviton.game.GameManager;
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
    private final GameManager gameManager;
    private final IoConnector connector;
    private final GameNetwork gameNetwork;
    private final Scanner scanner;
    private final AccountFactory accountFactory;

    @InjectSetting("exchange.ip")
    public String ip;
    @InjectSetting("exchange.port")
    public int port;
    @InjectSetting("server.ip")
    public String serverIp;
    @InjectSetting("server.port")
    public int serverPort;
    @InjectSetting("server.id")
    public int serverId;
    @InjectSetting("server.key")
    public String serverKey;

    private IoSession session;

    private long time;
    @Getter
    private long responseTime;

    @Inject
    public ExchangeNetwork(AccountFactory accountFactory, GameNetwork gameNetwork, GameManager gameManager, Scanner scanner) {
        this.connector = new NioSocketConnector();
        this.connector.setHandler(this);
        this.gameManager = gameManager;
        gameManager.setExchangeNetwork(this);
        this.accountFactory = accountFactory;
        this.gameNetwork = gameNetwork;
        this.scanner = scanner;
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
        System.exit(0);
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
        System.exit(0);
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
                send("S" + serverId + "@" + serverKey);
                break;
            case 'E':
                log.error("connection refused");
                System.exit(0);
                break;
            case 'R':
                System.exit(0);
                break;
            case 'I':
                send("I" + serverIp + "@" + serverPort);
                break;
            case 'S':
                gameManager.save();
                break;
            case '+':
                accountFactory.load(Integer.parseInt(packet.substring(1)));
                break;
            case '-':
                gameNetwork.getClients().stream().filter(client -> client.getAccount().getId() == Integer.parseInt(packet.substring(1))).forEach(client -> {
                    client.send("AlEa");
                    client.getSession().close(true);
                });
                break;
            case 'L':
                send("R" + scanner.launch(packet.substring(1)));
                break;
            default:
                if (packet.equals("PONG")) {
                    responseTime = (System.currentTimeMillis() - time);
                    return;
                }
                log.info("Undefined packet : {}", packet);
        }
    }

    public ExchangeNetwork launchPing() {
        time = System.currentTimeMillis();
        send("PING");
        return this;
    }
}

