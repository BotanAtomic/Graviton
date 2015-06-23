package graviton.common;

import lombok.Data;

/**
 * Created by Botan on 22/06/2015.
 */
@Data
public class Action {
    private int id;
    private String arguments;

    public Action(int id, String arguments) {
        this.id = id;
        this.arguments = arguments;
    }

    public void apply() {

    }
}
