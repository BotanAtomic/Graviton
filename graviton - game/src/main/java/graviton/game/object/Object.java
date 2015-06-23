package graviton.game.object;

import graviton.enums.ObjectPosition;
import lombok.Getter;

/**
 * Created by Botan on 21/06/2015.
 */
public class Object {
    @Getter
    private ObjectTemplate template;
    @Getter
    private ObjectPosition position;

    public Object() {
        this.position = ObjectPosition.NO_EQUIPED;
    }
}
