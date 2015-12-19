package graviton.game.enums;

/**
 * Created by Botan on 01/10/2015.
 */
public enum Rank {
    PLAYER(0),
    ANIMATOR(1),
    MODERATOR(2),
    MANAGER(3),
    BOSS(4); // I'ts me ahaha !

    public final int id;

    Rank(int id) {
        this.id = id;
    }
}
