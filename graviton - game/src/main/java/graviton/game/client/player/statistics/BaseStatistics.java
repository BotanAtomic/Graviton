package graviton.game.client.player.statistics;

import graviton.common.StatsID;
import graviton.enums.Classe;
import graviton.game.client.player.Player;
import graviton.game.statistics.Statistics;
import lombok.Data;

import java.util.Map;

/**
 * Created by Botan on 22/06/2015.
 */
@Data
public class BaseStatistics extends Statistics {

    private Map<Integer, Integer> effects;

    public BaseStatistics(Map<Integer, Integer> effects,Player player) {
        this.effects = effects == null ? super.effects : effects;
        this.effects.put(StatsID.ADD_PA, player.getLevel() < 100 ? 6 : 7);
        this.effects.put(StatsID.ADD_PM, 3);
        this.effects.put(StatsID.ADD_PROS, player.getClasse() == Classe.ENUTROF ? 120 : 100);
        this.effects.put(StatsID.ADD_PODS, 1000);
        this.effects.put(StatsID.CREATURE, 1);
        this.effects.put(StatsID.ADD_INIT, 1);
    }

    public Statistics cumulStatistics(Statistics...stats) { //TODO : cumul stats

        return this;
    }

    @Override
    protected void addEffect(int value, int quantity) {
        if(this.effects.get(value) == null || this.effects.get(value) == 0)
            this.effects.put(value, quantity);
        else
            this.effects.put(value,effects.get(value) + quantity);
    }
}
