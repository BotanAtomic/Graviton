package graviton.network.exchange;

import graviton.console.Console;
import graviton.game.Server;
import graviton.network.NetworkManager;
import lombok.Getter;
import lombok.Setter;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;

/**
 * Created by Botan on 06/06/2015.
 */
public class ExchangeClient {
    private NetworkManager networkManager;
    private Console console;

    @Getter private long id;
    @Getter private IoSession session;
    @Getter
    @Setter
    private Server server;

    public ExchangeClient(IoSession session, NetworkManager manager) {
        this.networkManager = manager;
        this.id = session.getId();
        this.session = session;
        this.networkManager.getExchangeClients().put(this.id, this);
        this.console = networkManager.getConsole();
    }

    public void kick() {
        if (networkManager.getExchangeClients().containsKey(this.id))
            networkManager.getExchangeClients().remove(this);
        this.session.close(true);
    }


    /**
     * @param packet [S] = Server
     *               (S)H = Host
     *               (S)K = Key
     *               (S)S = Statut
     *               [M] = Migration
     *               (M)P = Player
     *               (M)T = Take
     *               (M)T = Token
     *               (M)O = Okey
     */
    public void parsePacket(String packet) {
        try {
            switch (packet.charAt(0)) {
                case 'S':
                    switch (packet.charAt(1)) {
                        case 'H':
                            Server server = this.getServer();
                            String[] s = packet.substring(2).split(";");
                            server.setIp(s[0]);
                            server.setPort(Integer.parseInt(s[1]));
                            server.setState(1);
                            break;

                        case 'K':
                            s = packet.substring(2).split(";");
                            int id = Integer.parseInt(s[0]);
                            String key = s[1];
                            server = networkManager.getConfig().getDatabase().getServerData().getServers().get(id);

                            if (!server.getKey().equals(key)) {
                                this.send("SR");
                                this.kick();
                            }

                            server.setClient(this);
                            this.server = server;
                            send("SK");
                            break;

                        case 'S':
                            if (this.getServer() == null)
                                return;
                            this.getServer().setState(Integer.parseInt(packet.substring(2)));
                            break;
                    }
                    break;

                default:
                    console.println("Packet undefined\" " + packet + "\"", true);
                    this.kick();
                    break;
            }
        } catch (Exception e) {
            console.println(e.getMessage(), true);
            this.kick();
        }
    }

    public void send(String packet) {
        if (packet.isEmpty())
            return;
        IoBuffer ioBuffer = IoBuffer.allocate(2048);
        ioBuffer.put(packet.getBytes());
        ioBuffer.flip();
        this.session.write(ioBuffer);
    }

}
