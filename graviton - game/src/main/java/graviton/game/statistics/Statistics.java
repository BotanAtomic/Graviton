package graviton.game.statistics;

import lombok.Data;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Botan on 25/06/2015.
 */
@Data
public abstract class Statistics {
    protected Map<Integer,Integer> effects = new ConcurrentHashMap<>();

    protected abstract void addEffect(int value,int number);

}
