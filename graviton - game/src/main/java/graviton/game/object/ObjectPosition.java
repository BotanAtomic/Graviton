package graviton.game.object;


/**
 * Created by Botan on 21/06/2015.
 */
public enum ObjectPosition {
    NO_EQUIPED(-1, null),
    AMULETTE(0, new int[]{1}),
    ARME(1, new int[]{2, 3, 4, 5, 6, 7, 8, 19, 20, 21, 22}),
    ANNEAU1(2, new int[]{9}),
    CEINTURE(3, new int[]{10}),
    ANNEAU2(4, new int[]{9}),
    BOTTES(5, new int[]{11}),
    COIFFE(6, new int[]{16}),
    CAPE(7, new int[]{17}),
    FAMILIER(8, new int[]{18}),
    DOFUS1(9, new int[]{23}),
    DOFUS2(10, new int[]{23}),
    DOFUS3(11, new int[]{23}),
    DOFUS4(12, new int[]{23}),
    DOFUS5(13, new int[]{23}),
    DOFUS6(14, new int[]{23}),
    BOUCLIER(15, new int[]{82}),
    MONTURE(16, new int[0]);

    public final int id;
    private final int[] types;

    ObjectPosition(int id, int[] types) {
        this.id = id;
        this.types = types;
    }

    public static ObjectPosition get(int id) {
        if (id >= 35 && id <= 57)
            return NO_EQUIPED;
        for (ObjectPosition position : ObjectPosition.values())
            if (position.id == id)
                return position;
        return null;
    }

    public boolean contains(int id) {
        if (types == null)
            return true;

        for (Integer value : types)
            if (value == id)
                return true;

        return false;
    }

    public int get(boolean real) {
        if (!real && this.id >= 35 && this.id <= 57)
            return -1;
        return this.id;
    }
}
