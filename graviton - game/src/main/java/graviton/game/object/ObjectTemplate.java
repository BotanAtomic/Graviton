package graviton.game.object;

import graviton.enums.ObjectType;
import lombok.Data;
import lombok.Getter;

/**
 * Created by Botan on 21/06/2015.
 */
@Data
public class ObjectTemplate {
    private int id;
    private ObjectType type;
}
