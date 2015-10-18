package graviton.network.application;

import graviton.api.Client;
import graviton.common.CryptManager;
import graviton.login.Main;
import graviton.login.Manager;
import lombok.Data;
import org.apache.mina.core.session.IoSession;

/**
 * Created by Botan on 19/09/2015.
 */
@Data
public class ApplicationClient implements Client {
    private final long id;
    private final IoSession session;

    private final String username = "p68Em14cTsErCubmOWjjjA==";
    private final String password = "LWuLLsTioC/OHq/CGbiYyw==";

    private String remoteIP;

    public ApplicationClient(IoSession session) {
        this.session = session;
        this.id = session.getId();
        manager.addClient(this);
    }

    @Override
    public void parsePacket(String packet) {
        String finalPacket = packet.substring(1);
        switch (packet.charAt(0)) {
            case 'I':
                this.remoteIP = finalPacket;
                break;
            case 'C':
                String[] args = finalPacket.split(";");
                if (args[0].equals(CryptManager.decrypt(username)) && args[1].equals(CryptManager.decrypt(password))) {
                    send("L" + Main.getInstance(Manager.class).getServerForApplication());
                    return;
                }
                send("E");
                break;
            case 'S':
                manager.getServers().values().stream().filter(server -> server.getClient() != null).forEach(server -> server.send("S"));
                break;
            case 'R':
                Main.getInstance(Manager.class).getServerByKey(finalPacket).send("R");
                break;
            case 'E':
                Main.getInstance(Manager.class).getServerByKey(finalPacket).send("E");
                break;

        }
    }

    @Override
    public void kick() {
        manager.removeClient(this);
        session.close(true);
    }

    @Override
    public void send(String packet) {
        session.write(cryptPacket(packet));
    }
}
