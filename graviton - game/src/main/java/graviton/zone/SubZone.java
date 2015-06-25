package graviton.zone;

import graviton.game.maps.Maps;
import lombok.Data;

import java.util.List;

/**
 * Created by Botan on 25/06/2015.
 */
@Data
public class SubZone {
    private final int id;
    private final String name;
    private final Zone zone;

    private List<Maps> maps;

    public SubZone(int id, String name,Zone zone) {
        this.id = id;
        this.name = name;
        this.zone = zone;
    }

}
