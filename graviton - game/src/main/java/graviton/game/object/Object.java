package graviton.game.object;

import graviton.enums.ObjectPosition;
import lombok.Data;
import lombok.Getter;

/**
 * Created by Botan on 21/06/2015.
 */
@Data
public class Object {
    private ObjectTemplate template;
    private ObjectPosition position;

    public Object() {
        this.position = ObjectPosition.NO_EQUIPED;
    }
}
