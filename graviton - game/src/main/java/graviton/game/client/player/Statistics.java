package graviton.game.client.player;

import graviton.common.StatsID;
import lombok.Data;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Botan on 22/06/2015.
 */
@Data
public class Statistics {

    private Map<Integer, Integer> effects;

    public Statistics(Map<Integer, Integer> effects, boolean addBases, Player player) {
        this.effects = effects == null ? new ConcurrentHashMap<>() : effects;
        if (addBases)
            addBaseEffect(player);
    }

    private void addBaseEffect(Player player) {
        this.effects.put(StatsID.STATS_ADD_PA, player.getLevel() < 100 ? 6 : 7);
        this.effects.put(StatsID.STATS_ADD_PM, 3);
        this.effects.put(StatsID.STATS_ADD_PROS, player.getClasse() == Classe.ENUTROF ? 120 : 100);
        this.effects.put(StatsID.STATS_ADD_PODS, 1000);
        this.effects.put(StatsID.STATS_CREATURE, 1);
        this.effects.put(StatsID.STATS_ADD_INIT, 1);
    }
}
