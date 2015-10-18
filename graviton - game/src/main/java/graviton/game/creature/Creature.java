package graviton.game.creature;

import graviton.game.client.player.Position;

/**
 * Created by Botan on 25/06/2015.
 */
public interface Creature {
    int getId();

    String getGm();

    void send(String packet);

    Position getPosition();
}
