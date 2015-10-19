package graviton.game.creature.mount;

import com.google.inject.Inject;
import com.google.inject.Injector;
import graviton.core.Main;
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
        StringBuilder builder = new StringBuilder();
        builder.append(this.id).append(":");
        builder.append(this.color).append(":");
        builder.append(this.ancestor).append(":");
        builder.append(",").append(":");
        builder.append(this.name).append(":");
        builder.append(this.sex).append(":");
        builder.append(this.getXpString()).append(":");
        builder.append(this.level).append(":");
        builder.append("1").append(":");
        builder.append("1000").append(":");//Total pod
        builder.append("0").append(":");
        builder.append(this.endurance).append(",10000:");
        builder.append(this.maturity).append(",").append(1000).append(":");
        builder.append(this.energy).append(",").append(10000).append(":");
        builder.append(this.serenity).append(",-10000,10000:");
        builder.append(this.love).append(",10000:");
        builder.append("-1").append(":");
        builder.append("0").append(":");
        builder.append(getStatsString()).append(":");
        builder.append(this.fatigue).append(",240:");
        builder.append(this.reproduction).append(",20:");
        return builder.toString();
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
