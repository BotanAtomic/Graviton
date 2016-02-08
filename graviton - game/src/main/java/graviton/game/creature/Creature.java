package graviton.game.creature;

/**
 * Created by Botan on 25/06/2015.
 */
public interface Creature {
    int getId();

    String getGm();

    void send(String packet);

    void speak(String message);

    Position getPosition();
}
