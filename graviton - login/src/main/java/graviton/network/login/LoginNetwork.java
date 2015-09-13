package graviton.network.login;

import com.google.inject.Inject;
import graviton.api.NetworkService;
import graviton.login.Configuration;
import graviton.login.Login;
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
import java.util.Random;

/**
 * Created by Botan on 06/07/2015.
 */
@Slf4j
public class LoginNetwork implements NetworkService, IoHandler {
    private final NioSocketAcceptor acceptor;
    private final int port;

    private final Login login;

    @Inject
    public LoginNetwork(Configuration configuration, Login login) {
        this.acceptor = new NioSocketAcceptor();
        this.port = configuration.getLoginPort();
        this.login = login;
    }

    @Override
    public void sessionCreated(IoSession session) throws Exception {
        session.write("HC" + new LoginClient(session, generateKey()).getKey());
        log.info("[Session {}] created", session.getId());
    }

    @Override
    public void sessionOpened(IoSession session) throws Exception {
        log.info("[Session {}] opened", session.getId());
    }

    @Override
    public void sessionClosed(IoSession session) throws Exception {
        log.info("[Session {}] closed", session.getId());
        login.getClient(session).kick();
    }

    @Override
    public void sessionIdle(IoSession session, IdleStatus status) throws Exception {
        log.info("[Session {}] idle", session.getId());
    }

    @Override
    public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
        log.info("[Session {}] exception > {}", session.getId(), cause.getMessage());
        cause.printStackTrace();
    }

    @Override
    public void messageReceived(IoSession session, Object message) throws Exception {
        LoginClient client = (LoginClient) login.getClient(session);
        String packet = (message.toString().contains("\n") ? message.toString().replace("\n", "@") : message.toString());
        if (packet.isEmpty() || packet.equals("1.29.1"))
            return;
        client.parsePacket(packet);
        log.info("[Session {}] recev < {} [{}]", session.getId(), packet, client.getStatut());
    }

    @Override
    public void messageSent(IoSession session, Object message) throws Exception {
        log.info("[Session {}] send > {}", session.getId(), message);
    }

    @Override
    public void inputClosed(IoSession session) throws Exception {
        log.info("[Session {}] input closed", session.getId());
        session.close(true);
    }

    @Override
    public void start() {
        acceptor.setReuseAddress(true);
        acceptor.getFilterChain().addLast("codec", new ProtocolCodecFilter(new TextLineCodecFactory(Charset.forName("UTF8"), LineDelimiter.NUL, new LineDelimiter("\n\0"))));
        acceptor.setHandler(this);
        if (acceptor.isActive())
            return;
        try {
            acceptor.bind(new InetSocketAddress(port));
        } catch (IOException e) {
            log.error("Fail to bind Login acceptor : {}", e);
        }
    }

    @Override
    public void stop() {
        acceptor.getManagedSessions().values().forEach(session -> session.close(true));
        acceptor.unbind();
    }

    private final String generateKey() {
        String alphabet = "abcdefghijklmnopqrstuvwxyz";
        StringBuilder hashKey = new StringBuilder();
        Random rand = new Random();

        for (int i = 0; i < 32; i++)
            hashKey.append(alphabet.charAt(rand.nextInt(alphabet.length())));
        return hashKey.toString();
    }
}