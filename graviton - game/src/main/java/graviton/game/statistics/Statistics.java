package graviton.game.statistics;


import graviton.game.client.player.Player;
import graviton.game.common.Stats;
import graviton.game.enums.Classe;
import lombok.Data;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by Botan on 25/06/2015.
 */
@Data
public class Statistics {
    private Map<Integer, Integer> effects;
    private Map<Integer, Object> optionalEffect;

    public Statistics() {
        this.effects = new TreeMap<>();
        this.optionalEffect = new HashMap<>();
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
        effects.put(value, getEffect(value) + quantity);
    }

    public void addOptionalEffect(int value, Object argument) {
        optionalEffect.put(value, argument);
    }

    public boolean isSameStatistics(Statistics statistics) {
        return statistics.getEffects().equals(effects) && optionalEffect.equals(statistics.getOptionalEffect());
    }

    public int getEffect(int value) {
        return effects.getOrDefault(value, 0);
    }

    public Object getOptionalEffect(int value) {
        return optionalEffect.getOrDefault(value, null);
    }

    public Statistics accumulateStatistics(Collection<Statistics> stats) {
        stats.stream().filter(statistics -> statistics.getEffects() != null).forEach(statistics -> statistics.getEffects().keySet().forEach(i -> effects.put(i, (effects.get(i) == null ? 0 : effects.get(i)) + statistics.getEffects().get(i))));
        return this;
    }

    public Statistics accumulateStatistics(Statistics statistics) {
        statistics.getEffects().keySet().forEach(i -> effects.put(i, (effects.get(i) == null ? 0 : effects.get(i)) + statistics.getEffects().get(i)));
        return this;
    }

    public Statistics removeStatistics(Statistics statistics) {
        statistics.getEffects().keySet().forEach(i -> effects.put(i, ((effects.get(i) == null ||  statistics.getEffects().get(i) > effects.get(i) )? 0 : effects.get(i)) - statistics.getEffects().get(i)));
        return this;
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        for (Map.Entry<Integer, Integer> entry : this.effects.entrySet()) {
            if (builder.length() > 0)
                builder.append(",");
            builder.append(Integer.toHexString(entry.getKey())).append("#").append(Integer.toHexString(entry.getValue())).append("#0#0");
        }
        return builder.toString();
    }
}
