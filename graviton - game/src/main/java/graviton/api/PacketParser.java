package graviton.api;

import graviton.network.game.GameClient;

/**
 * Created by Botan on 20/06/2015.
 */
public interface PacketParser  {
    void parse(GameClient client, String packet);
}
