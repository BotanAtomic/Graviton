package graviton.game.packet.manager.game;

import graviton.api.PacketParser;
import graviton.network.game.GameClient;

/**
 * Created by Botan on 06/07/2015.
 */
public class Informations implements PacketParser {
    @Override
    public void parse(GameClient client, String packet) {
        client.send("GDK");
    }
}
