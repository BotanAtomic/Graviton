package graviton.network.exchange;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import graviton.core.Configuration;
import graviton.database.DatabaseManager;
import graviton.enums.DataType;
import graviton.game.client.Account;
import graviton.network.common.NetworkConnector;
import graviton.network.game.GameNetworkService;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.service.IoHandler;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;

import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;

/**
 * Created by Botan on 17/06/2015.
 */
@Singleton
public class ExchangeNetworkService extends NetworkConnector implements IoHandler {
    private final Configuration configuration;
    @Inject
    DatabaseManager databaseManager;
    @Inject
    GameNetworkService gameNetworkService;
    private IoSession session;

    @Inject
    public ExchangeNetworkService(Configuration configuration) {
        super(configuration.getExchangeIp(), configuration.getExchangePort());
        this.configuration = configuration;
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
        parse(decodePacket(message));
    }

    @Override
    public void messageSent(IoSession session, Object message) throws Exception {
    }

    @Override
    public void inputClosed(IoSession session) throws Exception {

    }

    private void send(String packet) {
        IoBuffer ioBuffer = IoBuffer.allocate(2048);
        ioBuffer.put(packet.getBytes());
        ioBuffer.flip();
        this.session.write(ioBuffer);
    }

    private String decodePacket(Object o) {
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
            case 'W': //Wainting
                switch (packet.charAt(1)) {
                    case 'A':
                        Account account = (Account) databaseManager.getData().get(DataType.ACCOUNT).load(Integer.parseInt(packet.substring(2)));
                        gameNetworkService.addAccount(account);
                        break;
                }
            case 'S': //Server
                switch (packet.charAt(1)) {
                    case '?': //Required
                        send("SK" + configuration.getServerId() + ";" + configuration.getServerKey());
                        break;

                    case 'K': //Ok
                        send("SH" + configuration.getIp() + ";" + configuration.getGamePort());
                        break;

                    case 'R': //Refused
                        System.exit(0);
                        break;
                }
                break;
        }
    }

    @Override
    protected void configure() {
        connector.setHandler(this);
    }
}

