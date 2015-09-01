package graviton.game.creature.mount;

import graviton.game.object.Object;
import graviton.game.statistics.Statistics;
import lombok.Data;

import java.util.List;

/**
 * Created by Botan on 25/06/2015.
 */
@Data
public class Mount {

    private final int id;
    private final int color;
    private final int sex;

    private String name;
    private long experience;
    private int level;

    private MountData data;
    private Statistics statistics;

    private List<Object> objects;

    public Mount(int id,int color,int sex,MountData data,List<Object> objects) {
        this.id = id;
        this.color = color;
        this.sex = sex;
        this.data = data;
        this.objects = objects;
        this.statistics = new Statistics();
    }



}
