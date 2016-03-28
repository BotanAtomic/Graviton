package graviton.game.client.player.packet;

import graviton.game.client.player.Player;

/**
 * Created by Botan on 20/03/2016.
 */
public interface Packet {
    String perform(Player player);
}
