package graviton.network.game;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import graviton.api.NetworkService;
import graviton.console.Console;
import graviton.core.Configuration;
import graviton.game.client.Account;
import graviton.game.packet.PacketParser;
import lombok.Getter;
import org.apache.mina.core.service.IoAcceptor;
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
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Botan on 17/06/2015.
 */
@Singleton
public class GameNetworkService implements IoHandler,NetworkService {
    private final Console console;
    // TODO :  @Inject PacketManager manager;
    @Getter
    private Map<Long, GameClient> clients;
    @Getter
    private Map<Integer, Account> waitingAccount;

    private final NioSocketAcceptor acceptor;

    @Inject
    public GameNetworkService(Configuration configuration, Console console) {
        this.acceptor = new NioSocketAcceptor();
        this.acceptor.setBacklog(configuration.getGamePort());
        this.acceptor.getFilterChain().addLast("codec", new ProtocolCodecFilter(new TextLineCodecFactory(Charset.forName("UTF8"), LineDelimiter.NUL, new LineDelimiter("\n\0"))));
        this.acceptor.setHandler(this);
        this.clients = new ConcurrentHashMap<>();
        this.waitingAccount = new ConcurrentHashMap<>();
        this.console = console;
    }

    @Override
    public void sessionCreated(IoSession session) throws Exception {
        console.println("[Session " + session.getId() + "] as created");
        clients.put(session.getId(), new GameClient(session));
        session.write("HG");
    }

    @Override
    public void sessionOpened(IoSession session) throws Exception {
        console.println("[Session " + session.getId() + "] opened...");
    }

    @Override
    public void sessionClosed(IoSession session) throws Exception {
        if (clients.containsKey(session.getId()))
            clients.remove(session);
        console.println("[Session " + session.getId() + "] closed...");
    }

    @Override
    public void sessionIdle(IoSession session, IdleStatus status) throws Exception {
        console.println("[Session " + session.getId() + "] idle...");
    }

    @Override
    public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
        console.println(cause.getMessage(), true);
    }

    @Override
    public void messageReceived(IoSession session, Object message) throws Exception {
        console.println("[Session " + session.getId() + "] recv < " + message.toString());
        checkPacket(clients.get(session.getId()), message.toString());
    }

    @Override
    public void messageSent(IoSession session, Object message) throws Exception {
        console.println("[Session " + session.getId() + "] send > " + message.toString());
    }

    @Override
    public void inputClosed(IoSession session) throws Exception {

    }

    private void checkPacket(GameClient client, String packet) {
        PacketParser.parse(client,packet);
    }

    public void addAccount(Account account) {
        this.waitingAccount.put(account.getId(), account);
    }

    public void removeAccount(Account account) {
        if (this.waitingAccount.containsKey(account.getId()))
            this.waitingAccount.remove(account);
    }

    public Account getAccount(int id) {
        return waitingAccount.get(id);
    }

    @Override
    public void start() {

        try {
            acceptor.bind(new InetSocketAddress(acceptor.getBacklog()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void stop() {

    }
}
