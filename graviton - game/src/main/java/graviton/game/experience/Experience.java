package graviton.game.experience;

import graviton.enums.DataType;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Botan on 01/07/2015.
 */

public class Experience {
    /**
     * Type -> level -> experience
     **/
    @Getter
    private Map<DataType, Map<Integer, Long>> data;

    public Experience(Object players, Object job, Object mount, Object pvp) {
        this.data = new ConcurrentHashMap<DataType, Map<Integer, Long>>() {{
            put(DataType.PLAYER, (Map<Integer, Long>) players);
            put(DataType.JOB, (Map<Integer, Long>) job);
            put(DataType.MOUNT, (Map<Integer, Long>) mount);
            put(DataType.PVP, (Map<Integer, Long>) pvp);
            put(DataType.GUILD, new HashMap() {{
                ((Map<Integer, Long>) players).keySet().forEach(key -> put(key, ((Map<Integer, Long>) players).get(key) * 10));
            }});
        }};
    }
}
