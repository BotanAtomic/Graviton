package graviton.game.enums;

/**
 * Created by Botan on 20/12/2015.
 */
public enum IdType {
    PLAYER(-1),
    MONSTER(-2),
    MONSTER_GROUP(-3),
    NPC(-4),
    SELLER(-5),
    COLLECTOR(-6),
    NON_PLAYER_MUTANT(-7),
    PLAYER_MUTANT(-8),
    FENCE(-9),
    PRISM(-10),
    TEMPORARY_OBJECT(-11);

    public final int id;

    public final int MAXIMAL_ID;

    public final int MINIMAL_ID;

    IdType(int id) {
        this.id = id;
        MAXIMAL_ID = id*100000000;
        MINIMAL_ID =((id-1)*100000000)+1;
    }
}
