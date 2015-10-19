package graviton.network.application;

import com.google.inject.Inject;
import com.google.inject.Injector;
import graviton.api.NetworkService;
import graviton.login.Manager;
import lombok.extern.slf4j.Slf4j;
import org.apache.mina.core.service.IoHandler;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;

import java.io.IOException;
import java.net.InetSocketAddress;

/**
 * Created by Botan on 18/06/2015.
 * PORT = 5000
 */
@Slf4j
public class ApplicationNetwork implements NetworkService, IoHandler {
    @Inject
    Injector injector;

    private final NioSocketAcceptor acceptor;
    private final Manager manager;

    @Inject
    public ApplicationNetwork(Manager manager) {
        this.acceptor = new NioSocketAcceptor();
        this.manager = manager;
    }

    @Override
    public void sessionCreated(IoSession session) throws Exception {
        injector.injectMembers(new ApplicationClient(session));
    }

    @Override
    public void sessionOpened(IoSession session) throws Exception {
        log.info("[Application {}] connected", session.getId());
    }

    @Override
    public void sessionClosed(IoSession session) throws Exception {
        manager.getClient(session).kick();
        log.info("[Application {}] closed", session.getId());
    }

    @Override
    public void sessionIdle(IoSession session, IdleStatus status) throws Exception {
        log.info("[Application {}] idle", session.getId());
    }

    @Override
    public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
        log.error(cause.getMessage());
    }

    @Override
    public void messageReceived(IoSession session, Object message) throws Exception {
        String packet = decryptPacket(message);
        manager.getClient(session).parsePacket(packet);
        log.info("[Application {}] recev < {}", session.getId(), packet);
    }

    @Override
    public void messageSent(IoSession session, Object message) throws Exception {
        log.info("[Application {}] sent > {}", session.getId(), decryptPacket(message));
    }

    @Override
    public void inputClosed(IoSession session) throws Exception {
        manager.getClient(session).kick();
    }

    @Override
    public void start() {
        acceptor.setHandler(this);
        if (acceptor.isActive())
            return;
        try {
            acceptor.bind(new InetSocketAddress(5000));
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