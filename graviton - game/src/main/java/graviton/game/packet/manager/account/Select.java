package graviton.game.packet.manager.account;

import graviton.api.Packet;
import graviton.api.PacketParser;
import graviton.network.game.GameClient;

/**
 * Created by Botan on 22/06/2015.
 */
@Packet("AS")
public class Select implements PacketParser {

    @Override
    public void parse(GameClient client, String packet) {
        final int id = Integer.parseInt(packet);
        if (client.getAccount().getPlayer(id) != null) {
            client.getAccount().getPlayer(id).joinGame();
            client.getAccount().setCurrentPlayer(client.getAccount().getPlayer(id));
            return;
        }
        client.send("ASE");
    }
}
