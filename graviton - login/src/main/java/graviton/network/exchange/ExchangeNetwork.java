package graviton.network.exchange;

import com.google.inject.Inject;
import graviton.api.NetworkService;
import graviton.game.Server;
import graviton.login.Configuration;
import graviton.login.Login;
import lombok.extern.slf4j.Slf4j;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.service.IoHandler;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;

/**
 * Created by Botan on 07/07/2015.
 */
@Slf4j
public class ExchangeNetwork implements NetworkService, IoHandler {
    private final NioSocketAcceptor acceptor;
    private final int port;

    private final Login login;

    @Inject
    public ExchangeNetwork(Configuration configuration, Login login) {
        this.acceptor = new NioSocketAcceptor();
        this.port = configuration.getExchangePort();
        this.login = login;
    }

    @Override
    public void sessionCreated(IoSession session) throws Exception {
        log.info("[(E)Session {}] created", session.getId());
        session.write(cryptPacket("?"));
    }

    @Override
    public void sessionOpened(IoSession session) throws Exception {
        log.info("[(E)Session {}] opened", session.getId());
    }

    @Override
    public void sessionClosed(IoSession session) throws Exception {
        ExchangeClient client = (ExchangeClient) login.getClient(session);
        client.getServer().setState(Server.State.OFFLINE);
        client.kick();
        log.info("[(E)Session {}] closed", session.getId());
    }

    @Override
    public void sessionIdle(IoSession session, IdleStatus status) throws Exception {
        log.info("[(E)Session {}] idle", session.getId());
    }

    @Override
    public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
        cause.printStackTrace();
    }

    @Override
    public void messageReceived(IoSession session, Object message) throws Exception {
        String packet = decryptPacket(message);
        parse(packet, session);
        log.info("[(E)Session {}] recev < {}", session.getId(), packet);
    }

    @Override
    public void messageSent(IoSession session, Object message) throws Exception {
        log.info("[(E)Session {}] send > {}", session.getId(), decryptPacket(message));
    }

    @Override
    public void inputClosed(IoSession session) throws Exception {
        session.close(true);
        log.info("[Session {}] input closed", session.getId());
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

    private final void parse(String packet, IoSession session) {
        String[] finalPacket = packet.substring(1).split("@");
        switch (packet.charAt(0)) {
            case 'S':
                if (new ExchangeClient(session, Integer.parseInt(finalPacket[0]), finalPacket[1]).getServer() == null)
                    session.write(cryptPacket("E"));
                else
                    session.write(cryptPacket("I"));
                break;
            case 'I':
                ExchangeClient client = (ExchangeClient) login.getClient(session);
                client.getServer().setClient(client);
                client.getServer().setIp(finalPacket[0]);
                client.getServer().setPort(Integer.parseInt(finalPacket[1]));
                log.info("[(E)Session {}] server {} is ready to connect", session.getId(), client.getServer().getId());
                break;
            default:
                log.info("[Exchange] Packet server not found -> {}", packet);
        }
    }

    @Override
    public void start() {
        acceptor.setReuseAddress(true);
        acceptor.setHandler(this);
        if (acceptor.isActive())
            return;
        try {
            acceptor.bind(new InetSocketAddress(port));
        } catch (IOException e) {
            log.error("Fail to bind Exchange acceptor : {}", e);
        }
    }

    @Override
    public void stop() {
        acceptor.unbind();
        acceptor.getManagedSessions().values().forEach(session -> session.close(true));
    }
}
