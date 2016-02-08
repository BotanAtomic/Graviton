package graviton.game.creature.mount;

import com.google.inject.Inject;
import com.google.inject.Injector;
import graviton.game.GameManager;
import graviton.game.object.Object;
import graviton.game.statistics.Statistics;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * Created by Botan on 25/06/2015.
 */
@Data
public class Mount {
    @Inject
    GameManager manager;

    private final int id;
    private final int color;
    private final int sex;

    private String name;
    private String ancestor;

    private long experience;
    private int level;

    private Statistics statistics;

    private List<Object> objects;

    private int fatigue;
    private int energy;
    private int reproduction;
    private int love;
    private int endurance;
    private int maturity;
    private int serenity;


    public Mount(int id, int color, int sex, List<Object> objects,Injector injector) {
        injector.injectMembers(this);
        this.id = id;
        this.color = color;
        this.sex = sex;
        this.objects = objects;
        this.statistics = new Statistics();
        this.ancestor = ",,,,,,,,,,,,,";
    }

    public String getPacket() {
        return String.valueOf(this.id) + ":" + this.color + ":" + this.ancestor + ":" + "," + ":" + this.name + ":" + this.sex + ":" + this.getXpString() + ":" + this.level + ":" + "1" + ":" + "1000" + ":" + "0" + ":" + this.endurance + ",10000:" + this.maturity + "," + 1000 + ":" + this.energy + "," + 10000 + ":" + this.serenity + ",-10000,10000:" + this.love + ",10000:" + "-1" + ":" + "0" + ":" + getStatsString() + ":" + this.fatigue + ",240:" + this.reproduction + ",20:";
    }

    private String getXpString() {
        return this.getExperience() + "," + manager.getMountExperience(this.level) + "," + manager.getMountExperience(this.level + 1);
    }

    private String getStatsString() {
        String stats = "";
        for (Map.Entry<Integer, Integer> entry : this.getStatistics().getEffects().entrySet()) {
            if (entry.getValue() <= 0)
                continue;
            if (stats.length() > 0)
                stats += ",";

            stats += Integer.toHexString(entry.getKey()) + "#" + Integer.toHexString(entry.getValue()) + "#0#0";
        }
        return stats;
    }

}
