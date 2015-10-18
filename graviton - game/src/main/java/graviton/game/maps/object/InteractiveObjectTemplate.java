package graviton.game.maps.object;

import lombok.Getter;

/**
 * Created by Botan on 10/10/2015.
 */
public class InteractiveObjectTemplate {
    @Getter
    private final int id, respawnTime, duration, unk;
    @Getter
    private final boolean walkable;

    public InteractiveObjectTemplate(int id, int respawnTime, int duration, int unk, boolean walkable) {
        this.id = id;
        this.respawnTime = respawnTime;
        this.duration = duration;
        this.unk = unk;
        this.walkable = walkable;
    }

}
