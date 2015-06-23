package graviton.network;

import com.google.inject.Inject;
import graviton.console.Console;
import graviton.login.Configuration;
import graviton.network.application.ApplicationNetwork;
import graviton.network.exchange.ExchangeClient;
import graviton.network.exchange.ExchangeNetworkHandler;
import graviton.network.login.LoginClient;
import graviton.network.login.LoginNetworkHandler;
import graviton.network.login.packet.PacketParser;
import lombok.Getter;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.textline.LineDelimiter;
import org.apache.mina.filter.codec.textline.TextLineCodecFactory;
import org.apache.mina.transport.socket.SocketAcceptor;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by Botan on 06/06/2015.
 */
public class NetworkManager {
    @Getter private Configuration config;
    @Getter
    private Console console;

    /** Login **/
    @Getter private Map<Long, LoginClient> loginClients;
    @Getter private NioSocketAcceptor loginAcceptor;

    /** Exchange **/
    @Getter private Map<Long, ExchangeClient> exchangeClients;
    @Getter private SocketAcceptor exchangeAcceptor;

    @Getter
    private PacketParser parser;

    private Lock locker;

    @Inject
    public NetworkManager(Configuration config, Console console) {
        this.exchangeClients = new HashMap<>();
        this.loginClients = new HashMap<>();
        this.config = config;
        this.console = console;
        this.locker = new ReentrantLock();
        this.parser = new PacketParser(config, console);
        new ApplicationNetwork(console);
    }

    public void configure() {
        this.configureExchange();
        this.configureLogin();
    }

    private void configureLogin() {
        loginAcceptor = new NioSocketAcceptor();
        loginAcceptor.getFilterChain().addLast("codec", new ProtocolCodecFilter(new TextLineCodecFactory(Charset.forName("UTF8"), LineDelimiter.NUL, new LineDelimiter("\n\0"))));
        loginAcceptor.setHandler(new LoginNetworkHandler(this, console));
        if (loginAcceptor.isActive())
            return;

        try {
            loginAcceptor.bind(new InetSocketAddress(config.getExchangeIp(), config.getLoginPort()));
        } catch (IOException e) {
            console.println("Fail to bind Login acceptor : \n" + e.toString(), true);
        }
    }

    private void configureExchange() {
        exchangeAcceptor = new NioSocketAcceptor();
        exchangeAcceptor.setReuseAddress(true);
        exchangeAcceptor.setHandler(new ExchangeNetworkHandler(this, console));
        if (exchangeAcceptor.isActive())
            return;

        try {
            exchangeAcceptor.bind(new InetSocketAddress(config.getExchangeIp(), config.getExchangePort()));
        } catch (IOException e) {
            console.println("Fail to bind Exchange acceptor : \n" + e.toString(), true);
            return;
        }
    }

    public void sendToAll(String packet) {
        this.locker.lock();
        this.loginClients.values().forEach((clients) -> clients.send(packet));
        this.locker.unlock();
    }

    public void stop() {
        loginAcceptor.getManagedSessions().values().stream().filter(session -> !session.isClosing()).forEach(session -> session.close(false));
        exchangeAcceptor.getManagedSessions().values().stream().filter(session -> !session.isClosing()).forEach(session -> session.close(false));
        loginAcceptor.dispose(false);
        exchangeAcceptor.dispose(false);
    }
}
