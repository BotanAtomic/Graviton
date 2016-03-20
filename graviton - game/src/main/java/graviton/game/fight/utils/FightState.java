package graviton.game.fight.utils;

/**
 * Created by Botan on 19/03/2016.
 */
public enum FightState {
    INITIATION(1), PLACEMENT(2), ACTIVE(3), FINISHED(4);
    public final int id;

    FightState(int id) {
        this.id = id;
    }
}
