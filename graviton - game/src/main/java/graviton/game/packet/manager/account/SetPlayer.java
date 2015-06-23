package graviton.game.packet.manager.account;

import graviton.api.Packet;
import graviton.api.PacketParser;
import graviton.network.game.GameClient;

/**
 * Created by Botan on 22/06/2015.
 */
@Packet("AS")
public class SetPlayer implements PacketParser {

    @Override
    public void parse(GameClient client, String packet) {
        int id = Integer.parseInt(packet);
        if (client.getAccount().getPlayer(id) != null) {
            client.getAccount().setCurrentPlayer(client.getAccount().getPlayer(id));
            if (client.getPlayer() != null) {
                System.err.println("STAPE 1");
                client.getPlayer().joinGame();
                System.err.println("STAPE 2");
                return;
            }
        }
        client.send("ASE");
    }
}
