package graviton.game.spells;

import lombok.Getter;
import lombok.Setter;

/**
 * Created by Botan on 20/03/2016.
 */
public class Animation {

    @Getter
    @Setter
    private int id, gfx, area, action, size;

    public Animation() {
        //For serialize
    }

    public Animation(int id, int gfx, int area, int action, int size) {
        this.id = id;
        this.gfx = gfx;
        this.area = area;
        this.action = action;
        this.size = size;
    }

    public String getGA() {
        return new StringBuilder().append(gfx).append(",").append(area).append(",").append(action).append(",").append(size).toString();
    }
}
