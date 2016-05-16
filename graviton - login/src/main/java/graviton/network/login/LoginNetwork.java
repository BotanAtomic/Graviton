package graviton.network.login;

import com.google.inject.Inject;
import com.google.inject.Injector;
import graviton.api.InjectSetting;
import graviton.api.NetworkService;
import graviton.core.GlobalManager;
import graviton.database.Database;
import graviton.network.security.GravitonFilter;
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
import java.util.Collection;
import java.util.UUID;

/**
 * Created by Botan on 06/07/2015.
 */
@Slf4j
public class LoginNetwork extends NetworkService implements IoHandler {
    private final NioSocketAcceptor acceptor;
    private final GlobalManager globalManager;
    @Inject
    Injector injector;
    @InjectSetting("login.port")
    private int port;

    @Inject
    public LoginNetwork(GlobalManager globalManager, Database database) {
        globalManager.addManageable(this);

        this.acceptor = new NioSocketAcceptor();
        this.acceptor.setReuseAddress(true);
        this.acceptor.getFilterChain().addFirst("blacklist", new GravitonFilter((byte) 3, (short) 1, database));
        this.acceptor.getFilterChain().addLast("codec", new ProtocolCodecFilter(new TextLineCodecFactory(Charset.forName("UTF8"), LineDelimiter.NUL, new LineDelimiter("\n\0"))));
        this.acceptor.setHandler(this);
        this.globalManager = globalManager;
    }

    @Override
    public void sessionCreated(IoSession session) throws Exception {
        session.write("HC" + new LoginClient(session, generateKey(), injector).getKey());
        log.info("[Session {}] created", session.getId());
    }

    @Override
    public void sessionOpened(IoSession session) throws Exception {
        if (!session.isClosing())
            log.info("[Session {}] opened", session.getId());
    }

    @Override
    public void sessionClosed(IoSession session) throws Exception {
        LoginClient client = (LoginClient) session.getAttribute("client");
        if(client != null)
            client.kick();
        log.info("[Session {}] closed", session.getId());
    }

    @Override
    public void sessionIdle(IoSession session, IdleStatus status) throws Exception {
        log.info("[Session {}] idle", session.getId());
    }

    @Override
    public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
        if (cause instanceof IOException)
            return;
        log.error("[Session {}] has encountered an error : {}", session.getId(), cause);
    }

    @Override
    public void messageReceived(IoSession session, Object message) throws Exception {
        String packet = message.toString();

        if (packet.startsWith("1.2"))
            return;

        ((LoginClient) session.getAttribute("client")).parsePacket(packet);
        log.info("[Session {}] receive < {}", session.getId(), packet);
    }

    @Override
    public void messageSent(IoSession session, Object message) throws Exception {
        log.info("[Session {}] send > {}", session.getId(), message);
    }

    @Override
    public void inputClosed(IoSession session) throws Exception {
        session.close(true);
        log.info("[Session {}] input closed", session.getId());
    }

    @Override
    public void configure() {
        try {
            acceptor.bind(new InetSocketAddress(port));
        } catch (IOException e) {
            log.error("Fail to bind Manager acceptor : {}", e);
        }
    }

    @Override
    public void stop() {
        acceptor.getManagedSessions().values().forEach(session -> session.close(true));
        acceptor.unbind();
    }

    private String generateKey() {
        return UUID.randomUUID().toString().replace("-","");
    }

    public Collection<IoSession> getSessions() {
        return this.acceptor.getManagedSessions().values();
    }
}
