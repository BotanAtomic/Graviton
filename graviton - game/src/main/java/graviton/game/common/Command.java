package graviton.game.common;

import graviton.game.client.player.Player;

/**
 * Created by Botan on 18/10/2015 [Game]
 */
public interface Command {
    void perform(Player player, String arguments);
}
