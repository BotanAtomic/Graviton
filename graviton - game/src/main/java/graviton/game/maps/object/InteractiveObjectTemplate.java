package graviton.game.maps.object;


import lombok.Data;


/**
 * Created by Botan on 10/10/2015.
 */
@Data
public class InteractiveObjectTemplate {
    private int id, respawnTime, duration, unk;
    private boolean walkable;
    private String name;


    public InteractiveObjectTemplate() {
        //For serialize
    }

    public InteractiveObjectTemplate(int id, String name, int respawnTime, int duration, int unk, boolean walkable) {
        this.id = id;
        this.name = name;
        this.respawnTime = respawnTime;
        this.duration = duration;
        this.unk = unk;
        this.walkable = walkable;
    }

}
