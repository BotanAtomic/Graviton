package graviton.game.enums;

/**
 * Created by Botan on 10/02/2016.
 */
public enum ActionType {
    BANK(-1),
    TELEPORT(0),
    DIALOG(1),
    RESET_STATS(13),
    DONJON(15),
    DEFAULT(999);

    public final int id;

    ActionType(int id) {
        this.id = id;
    }

    public static ActionType getType(int id) {
        for(ActionType i : values())
            if(i.id == id)
                return i;
        return DEFAULT;
    }
}
