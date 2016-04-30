package graviton.network.exchange;

import com.google.inject.Inject;
import com.google.inject.Injector;
import graviton.api.InjectSetting;
import graviton.api.NetworkService;
import graviton.core.GlobalManager;
import lombok.extern.slf4j.Slf4j;
import org.apache.mina.core.service.IoHandler;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Created by Botan on 07/07/2015.
 */
@Slf4j
public class ExchangeNetwork extends NetworkService implements IoHandler {
    private final NioSocketAcceptor acceptor;
    private final GlobalManager globalManager;

    @Inject
    Injector injector;

    @InjectSetting("exchange.port")
    private int port;

    @Inject
    public ExchangeNetwork(GlobalManager globalManager) {
        globalManager.addManageable(this);
        this.acceptor = new NioSocketAcceptor();
        this.globalManager = globalManager;
    }

    @Override
    public void sessionCreated(IoSession session) throws Exception {
        log.info("[(E)Session {}] created", session.getId());
        new ExchangeClient(session, injector);
    }

    @Override
    public void sessionOpened(IoSession session) throws Exception {
        log.info("[(E)Session {}] opened", session.getId());
    }

    @Override
    public void sessionClosed(IoSession session) throws Exception {
        ExchangeClient client = (ExchangeClient) session.getAttribute("client");
        client.kick();
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
        String packet = decryptPacket(message);
        ExchangeClient client = (ExchangeClient) session.getAttribute("client");

        if (packet.equals("PING"))
            client.send("PONG");
        else
            client.parsePacket(packet);

        log.info("[(E)Session {}] receive < {}", session.getId(), packet);
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

    @Override
    public void configure() {
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

    public Collection<ExchangeClient> getSessions() {
        return this.acceptor.getManagedSessions().values().stream().map(session -> (ExchangeClient) session.getAttribute("client")).collect(Collectors.toCollection(ArrayList::new));
    }
}
