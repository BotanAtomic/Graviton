package graviton.game.statistics;


import graviton.common.Stats;
import graviton.enums.Classe;
import graviton.game.client.player.Player;
import lombok.Data;

import java.util.HashMap;
import java.util.List;
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

    public int getEffect(int value) {
        return (effects.containsKey(value) ? effects.get(value) : 0);
    }

    public Map<Integer, Integer> cumulStatistics(List<Statistics> stats) {
        Map<Integer, Integer> builder = new HashMap<>();
        stats.stream().filter(statistics -> statistics.getEffects() != null).forEach(statistics -> {
            for (Integer i : statistics.getEffects().keySet())
                builder.put(i, (builder.get(i) == null ? 0 : builder.get(i)) + statistics.getEffects().get(i));
        });
        return builder;
    }
}
