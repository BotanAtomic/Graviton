package graviton.network.game;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import graviton.console.Console;
import graviton.core.Configuration;
import graviton.game.client.Account;
import graviton.game.packet.PacketManager;
import graviton.network.common.NetworkAcceptor;
import lombok.Getter;
import org.apache.mina.core.service.IoHandler;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.textline.LineDelimiter;
import org.apache.mina.filter.codec.textline.TextLineCodecFactory;

import java.nio.charset.Charset;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Botan on 17/06/2015.
 */
@Singleton
public class GameNetworkService extends NetworkAcceptor implements IoHandler {
    private final Console console;
    private
    @Inject
    PacketManager manager;
    @Getter
    private Map<Long, GameClient> clients;
    @Getter
    private Map<Integer, Account> waitingAccount;

    @Inject
    public GameNetworkService(Configuration configuration, Console console) {
        super(configuration.getGamePort());
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
        try {
            manager.getPackets().get(packet.substring(0, 2)).parse(client, packet.substring(2));
        } catch (NullPointerException e) {
            switch (packet) {
                case "AV":
                    client.send("BN");
                    client.send("AV0");
                    break;
                case "Af":
                    client.send("Af" + (1) + ("|") + (1) + ("|") + (1) + ("|") + (1) + ("|") + (1));
                    break;
            }
        }
    }

    @Override
    protected void configure() {
        acceptor.getFilterChain().addLast("codec", new ProtocolCodecFilter(new TextLineCodecFactory(Charset.forName("UTF8"), LineDelimiter.NUL, new LineDelimiter("\n\0"))));
        acceptor.setHandler(this);
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
}
