package graviton.network.exchange;

import com.google.inject.Inject;
import graviton.api.NetworkService;
import graviton.login.Configuration;
import graviton.login.Login;
import lombok.extern.slf4j.Slf4j;
import org.apache.mina.core.service.IoHandler;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;

import java.io.IOException;
import java.net.InetSocketAddress;

/**
 * Created by Botan on 07/07/2015.
 */
@Slf4j
public class ExchangeNetwork implements NetworkService,IoHandler {
    private final NioSocketAcceptor acceptor;
    private final int port;

    private final Login login;

    @Inject
    public ExchangeNetwork(Configuration configuration,Login login) {
        this.acceptor = new NioSocketAcceptor();
        this.port = configuration.getExchangePort();
        this.login = login;
    }

    @Override
    public void sessionCreated(IoSession session) throws Exception {
        log.info("[(E)Session {}] created", session.getId());
    }

    @Override
    public void sessionOpened(IoSession session) throws Exception {
        log.info("[(E)Session {}] opened", session.getId());
    }

    @Override
    public void sessionClosed(IoSession session) throws Exception {
        log.info("[(E)Session {}] closed", session.getId());
    }

    @Override
    public void sessionIdle(IoSession session, IdleStatus status) throws Exception {
        log.info("[(E)Session {}] idle", session.getId());
    }

    @Override
    public void exceptionCaught(IoSession session, Throwable cause) throws Exception {

    }

    @Override
    public void messageReceived(IoSession session, Object message) throws Exception {

    }

    @Override
    public void messageSent(IoSession session, Object message) throws Exception {

    }

    @Override
    public void inputClosed(IoSession session) throws Exception {

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
            log.warn("Fail to bind Exchange acceptor : {}", e);
        }
    }

    @Override
    public void stop() {
        acceptor.unbind();
        acceptor.getManagedSessions().values().forEach(session -> session.close(true));
    }
}
