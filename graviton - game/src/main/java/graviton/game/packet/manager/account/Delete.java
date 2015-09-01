package graviton.game.packet.manager.account;

import graviton.api.Packet;
import graviton.api.PacketParser;
import graviton.game.client.Account;
import graviton.network.game.GameClient;

/**
 * Created by Botan on 06/07/2015.
 */
@Packet("AD")
public class Delete implements PacketParser {
    @Override
    public void parse(GameClient client, String packet) {
        Account account = client.getAccount();
        if (account.getAnswer().equals(packet.substring(2))) {
            account.getPlayer(Integer.parseInt(packet.substring(0, 1))).delete();
            return;
        }
        client.send("ADE");
    }
}
