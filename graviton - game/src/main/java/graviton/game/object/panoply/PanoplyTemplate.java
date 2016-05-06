package graviton.game.object.panoply;

import graviton.game.client.player.Player;
import graviton.game.enums.StatsType;
import graviton.game.statistics.Statistics;
import lombok.Data;

/**
 * Created by Botan on 05/05/2016.
 */
@Data
public class PanoplyTemplate {

    private final int id;
    private final int[] items;
    private final String name;

    private final Statistics[] statistics;

    public PanoplyTemplate(int id, String name, String[] items, String[] statistics) {
        this.id = id;
        this.name = name;
        this.items = convert(items);
        this.statistics = configureStatistics(statistics);
    }

    private int[] convert(String[] items) {
        int[] arrayOfItems = new int[items.length];
        for (int i = 0; i < items.length; i++)
            arrayOfItems[i] = Integer.parseInt(items[i].trim());
        return arrayOfItems;
    }

    private Statistics[] configureStatistics(String[] statistics) {
        Statistics[] arrayOfStatistics = new Statistics[statistics.length + 2];
        Statistics result;
        int array = 2;
        for (String split : statistics) {
            result = new Statistics();
            for (String value : split.split(",")) {
                if (value.isEmpty()) continue;
                result.addEffect(Integer.parseInt(value.split(":")[0]), Integer.parseInt(value.split(":")[1]));
            }
            arrayOfStatistics[array] = result;
            array++;
        }
        return arrayOfStatistics;
    }

    public Statistics getStatistics(int objectEquipped, Player player) {
        Statistics panoplyStatistics = statistics[objectEquipped] == null ? new Statistics() : statistics[objectEquipped];
        player.getStatistics().get(StatsType.PANOPLY).cumulStatistics(panoplyStatistics);
        return panoplyStatistics;
    }
}
