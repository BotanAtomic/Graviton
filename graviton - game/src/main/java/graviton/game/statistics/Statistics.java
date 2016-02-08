package graviton.game.statistics;


import graviton.game.client.player.Player;
import graviton.game.common.Stats;
import graviton.game.enums.Classe;
import lombok.Data;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Botan on 25/06/2015.
 */
@Data
public class Statistics {
    private Map<Integer, Integer> effects;

    public Statistics() {
        this.effects = new HashMap<>();
    }

    public Statistics(Player player, Map<Integer, Integer> effects) {
        this.effects = (effects == null ? new HashMap<>() : effects);
        this.effects.put(Stats.ADD_PA, player.getLevel() < 100 ? 6 : 7);
        this.effects.put(Stats.ADD_PM, 3);
        this.effects.put(Stats.ADD_PROS, player.getClasse() == Classe.ENUTROF ? 120 : 100);
        this.effects.put(Stats.ADD_PODS, 1000);
        this.effects.put(Stats.CREATURE, 1);
        this.effects.put(Stats.ADD_INIT, 1);
    }

    public void addEffect(int value, int quantity) {
        if (effects.get(value) == null || effects.get(value) == 0)
            effects.put(value, quantity);
        else
            effects.put(value, effects.get(value) + quantity);
    }

    public boolean isSameStatistics(Statistics statistics) {
        for (Map.Entry<Integer, Integer> entry : this.effects.entrySet()) {
            if (statistics.getEffects().get(entry.getKey()) == null)
                return false;
            if (statistics.getEffects().get(entry.getKey()).compareTo(entry.getValue()) != 0)
                return false;
        }
        for (Map.Entry<Integer, Integer> entry : statistics.getEffects().entrySet()) {
            if (this.effects.get(entry.getKey()) == null)
                return false;
            if (this.effects.get(entry.getKey()).compareTo(entry.getValue()) != 0)
                return false;
        }
        return true;
    }

    public int getEffect(int value) {
        return (effects.containsKey(value) ? effects.get(value) : 0);
    }

    public Statistics cumulStatistics(Collection<Statistics> stats) {
        Map<Integer, Integer> builder = new HashMap<>();
        stats.stream().filter(statistics -> statistics.getEffects() != null).forEach(statistics -> {
            for (Integer i : statistics.getEffects().keySet())
                builder.put(i, (builder.get(i) == null ? 0 : builder.get(i)) + statistics.getEffects().get(i));
        });
        this.effects = builder;
        return this;
    }

    public Statistics cumulStatistics(Statistics statistics) {
        if(statistics == null) return this;
            for (Integer i : statistics.getEffects().keySet())
                effects.put(i, (effects.get(i) == null ? 0 : effects.get(i)) + statistics.getEffects().get(i));
        return this;
    }
}
