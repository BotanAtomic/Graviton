package graviton.game.packet.manager.game;

import graviton.api.Packet;
import graviton.api.PacketParser;
import graviton.network.game.GameClient;

/**
 * Created by Botan on 22/06/2015.
 */
@Packet("GC")
public class CreateGame implements PacketParser {

    @Override
    public void parse(GameClient client, String packet) {
        client.getPlayer().createGame();
    }
}
