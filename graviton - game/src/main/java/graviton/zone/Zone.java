package graviton.zone;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Botan on 25/06/2015.
 */
@Data
public class Zone {
    private final int id;
    private final String name;

    private List<SubZone> subZones;

    public Zone(int id, String name) {
        this.id = id;
        this.name = name;
        this.subZones = new ArrayList<>();
    }
}
