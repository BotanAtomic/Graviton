package graviton.game.zone;

import graviton.game.alignement.Alignement;
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
    private final Alignement alignement;
    private final Zone zone;

    private List<Maps> maps;

    public SubZone(int id, String name,Zone zone,int alignId) {
        this.id = id;
        this.name = name;
        this.alignement = new Alignement(alignId,0,0);
        this.zone = zone;
    }

}
