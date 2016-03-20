package graviton.game.fight.utils;

/**
 * Created by Botan on 06/03/2016.
 */
public enum FightType {
    DEFY(0), PVP(1), PVM(4), COLLECTOR(5);
    public final int id;

    FightType(int id) {
        this.id = id;
    }
}