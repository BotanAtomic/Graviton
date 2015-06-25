package graviton.game.packet.manager.account;

import graviton.api.Packet;
import graviton.api.PacketParser;
import graviton.core.Main;
import graviton.network.game.GameClient;
import graviton.network.game.GameNetworkService;

/**
 * Created by Botan on 20/06/2015.
 */
@Packet("AT")
public class SendTicket implements PacketParser {

    @Override
    public void parse(GameClient client, String packet) {
        /**final GameNetworkService service = Main.getInstance(GameNetworkService.class);
        client.setAccount(service.getAccount(Integer.parseInt(packet)));

        if (client.getAccount() != null) {
            String ip = client.getSession().getLocalAddress().toString().replace("/", "").split(":")[0];
            client.getAccount().setClient(client);
            client.getAccount().setIpAdress(ip);
            service.removeAccount(client.getAccount());
            client.send("ATK0");
        } else {
            client.send("ATE");
        } **/
    }
}
