package graviton.game.packet.manager.account;

import graviton.api.Packet;
import graviton.api.PacketParser;
import graviton.network.game.GameClient;

/**
 * Created by Botan on 21/06/2015.
 */
@Packet("Ag")
public class Gift implements PacketParser {
    @Override
    public void parse(GameClient client, String packet) {
        //TODO : Gift
    }
}
