package graviton.game.experience;

import graviton.enums.DataType;
import lombok.Data;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Botan on 01/07/2015.
 */
@Data
public class Experience {
    /** Type -> level -> experience **/
    private Map<DataType,Map<Integer,Long>> data;

    public Experience(Map<Integer,Long> players,Map<Integer,Long> job,Map<Integer,Long> mount,Map<Integer,Long> pvp) {
        this.data = new ConcurrentHashMap<DataType,Map<Integer,Long>>() {{
                put(DataType.PLAYER, players);
                put(DataType.JOB, job);
                put(DataType.MOUNT, mount);
                put(DataType.PVP, pvp);
            }};
    }
}
