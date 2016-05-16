package graviton.network.application;

import com.google.common.net.InetAddresses;
import com.google.inject.Inject;
import graviton.api.NetworkService;
import graviton.core.GlobalManager;
import graviton.database.Database;
import graviton.game.Account;
import graviton.game.Server;
import lombok.extern.slf4j.Slf4j;
import org.apache.mina.core.buffer.IoBuffer;
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
public class ApplicationNetwork extends NetworkService implements IoHandler {
    private final NioSocketAcceptor acceptor;
    private final GlobalManager globalManager;
    @Inject
    Database database;
    private IoSession client;
    private String remoteIP = null;
    private Server server;

    @Inject
    public ApplicationNetwork(GlobalManager globalManager) {
        globalManager.addManageable(this);
        this.acceptor = new NioSocketAcceptor();
        this.globalManager = globalManager;
    }

    @Override
    public void sessionCreated(IoSession session) throws Exception {
        if (this.client != null) {
            session.write(cryptPacket("A"));
            session.close(true);
            return;
        }
        this.client = session;
    }

    @Override
    public void sessionOpened(IoSession session) throws Exception {
        log.info("[Application {}] connected", session.getId());
    }

    @Override
    public void sessionClosed(IoSession session) throws Exception {
        session.close(true);
        client = null;
        log.info("[Application {}] closed", session.getId());
    }

    @Override
    public void sessionIdle(IoSession session, IdleStatus status) throws Exception {
        log.info("[Application {}] idle", session.getId());
    }

    @Override
    public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
        log.error("[Application {}] error {}", session.getId(), cause.getMessage());
    }

    @Override
    public void messageReceived(IoSession session, Object message) throws Exception {
        String packet = decryptPacket(message);
        if (packet.charAt(0) == 'I') {
            this.setRemoteIp(packet.substring(1));
            return;
        }
        parsePacket(packet);
        log.info("[Application {}] receive < {}", session.getId(), packet);
    }

    @Override
    public void messageSent(IoSession session, Object message) throws Exception {
        log.info("[Application {}] sent > {}", session.getId(), decryptPacket(message));
    }

    @Override
    public void inputClosed(IoSession session) throws Exception {
        session.close(true);
        this.client = null;
    }

    private void parsePacket(String packet) {
        if (remoteIP == null)
            return;
        String finalPacket = packet.substring(1);
        switch (packet.charAt(0)) {
            case 'I':

                break;
            case 'S':
                this.server = globalManager.getServerByKey(finalPacket);
                break;
            case 'C':
                String[] args = finalPacket.split(";");
                Account account = database.loadApplicationAccount(args[0], args[1]);
                if (account != null) {
                    if (account.getRank() < 4) {
                        send("K");
                        return;
                    }
                    send(globalManager.getServerForApplication());
                    return;
                }
                send("E");
                break;
            case 'L':
                server.send(packet);
                break;
        }
    }

    public void send(String packet) {
        client.write(cryptPacket(packet));
    }

    private void setRemoteIp(String ip) {
        if (InetAddresses.isInetAddress(ip)) {
            this.remoteIP = ip;
            log.info("[Application {}] has successfully passed the IP address verification [{}]", client.getId(),ip);
            return;
        }
        log.info("[Application {}] does not correctly passed the IP address verification [{}]", client.getId(),ip);
    }

    private IoBuffer cryptPacket(String packet) {
        IoBuffer ioBuffer = IoBuffer.allocate(2048);
        ioBuffer.put(packet.getBytes());
        ioBuffer.flip();
        return ioBuffer;
    }

    @Override
    public void configure() {
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

    public String applicationIsConnected() {
        return client == null ? "disconnected" : "connected";
    }
}