package graviton.network.login;

import graviton.console.Console;
import graviton.enums.LoginStatus;
import graviton.network.NetworkManager;
import org.apache.mina.core.service.IoHandler;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;

import java.util.Random;

/**
 * Created by Botan on 06/06/2015.
 */
public class LoginNetworkHandler implements IoHandler {
    private final NetworkManager manager;
    private Console console;

    public LoginNetworkHandler(NetworkManager networkManager, Console console) {
        this.manager = networkManager;
        this.console = console;
    }

    @Override
    public void sessionCreated(IoSession session) throws Exception {
        console.println("[Session " + session.getId() + "] as created", false);
        LoginClient client = new LoginClient(session, generateKey(), this.manager);
        session.write("HC" + client.getKey());
        client.setStatus(LoginStatus.WAIT_CONNECTION);
    }

    @Override
    public void sessionOpened(IoSession session) throws Exception {
        console.println("[Session " + session.getId() + "] opened...", false);
    }

    @Override
    public void sessionClosed(IoSession session) throws Exception {
        console.println("[Session " + session.getId() + "] closed...", false);
    }

    @Override
    public void sessionIdle(IoSession session, IdleStatus status) throws Exception {
        console.println("[Session " + session.getId() + "] idle...", false);
    }

    @Override
    public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
        console.println(cause.getMessage(), true);
    }

    @Override
    public void messageReceived(IoSession session, Object message) throws Exception {
        String packet = (String) message;
        String[] s = packet.split("\n");
        final LoginClient client = this.manager.getLoginClients().get(session.getId());
        int i = 0;
        do {
            console.println("[Session " + session.getId() + "] recv < " + s[i], false);
            this.checkPacket(client, s[i]);
            i++;
        } while (i == s.length - 1);
    }

    @Override
    public void messageSent(IoSession session, Object message) throws Exception {
        console.println("[Session " + session.getId() + "] send > " + message.toString(), false);
    }

    @Override
    public void inputClosed(IoSession session) throws Exception {

    }

    private String generateKey() {
        String alphabet = "abcdefghijklmnopqrstuvwxyz";
        StringBuilder hashKey = new StringBuilder();
        Random rand = new Random();

        for (int i = 0; i < 32; i++)
            hashKey.append(alphabet.charAt(rand.nextInt(alphabet.length())));
        return hashKey.toString();
    }

    private void checkPacket(LoginClient client, String packet) {
        if (packet.equalsIgnoreCase("1.29.1")) {
            console.println("[Session " + client.getId() + "] : check version", false);
            return;
        }
        client.parsePacket(packet);
    }
}
