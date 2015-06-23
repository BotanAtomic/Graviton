package graviton.network.application;

import graviton.console.Console;
import org.apache.mina.transport.socket.SocketAcceptor;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;

import java.io.IOException;
import java.net.InetSocketAddress;

/**
 * Created by Botan on 18/06/2015.
 */
public class ApplicationNetwork {
    private int port;
    private SocketAcceptor acceptor;
    private Console console;

    public ApplicationNetwork(Console console) {
        this.console = console;
        this.port = 500;
        this.acceptor = new NioSocketAcceptor();
        configure();
    }

    public void configure() {
        acceptor = new NioSocketAcceptor();
        acceptor.setReuseAddress(true);
        acceptor.setHandler(new ApplicationNetworkHandler());
        if (acceptor.isActive())
            return;
        try {
            acceptor.bind(new InetSocketAddress("127.0.0.1", port));
        } catch (IOException e) {
            console.println("Fail to bind Login acceptor : \n" + e.toString(), true);
        }
    }


}
