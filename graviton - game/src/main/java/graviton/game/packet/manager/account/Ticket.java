package graviton.game.packet.manager.account;

import graviton.api.Packet;
import graviton.api.PacketParser;
import graviton.core.Main;
import graviton.game.GameManager;
import graviton.network.game.GameClient;
import graviton.network.game.GameNetworkService;

/**
 * Created by Botan on 20/06/2015.
 */
@Packet("AT")
public class Ticket implements PacketParser {

    @Override
    public void parse(GameClient client, String packet) {
        final GameManager service = Main.getInstance(GameManager.class);
        client.setAccount(service.getAccounts().get(Integer.parseInt(packet)));

        if (client.getAccount() != null) {
            client.getAccount().setClient(client);
            client.getAccount().setIpAdress(client.getSession().getLocalAddress().toString().replace("/", "").split(":")[0]);
            client.send("ATK0");
        } else {
            client.send("ATE");
        }
    }
}
