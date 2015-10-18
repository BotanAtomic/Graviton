package graviton.game.client.player;

import graviton.game.maps.Cell;
import graviton.game.maps.Maps;
import lombok.Data;

/**
 * Created by Botan on 23/06/2015.
 */
@Data
public class Position {
    private Maps map;
    private Cell cell;
    private int orientation;

    public Position(Maps map,Cell cell, int orientation) {
        this.map = map;
        this.cell = cell;
        this.orientation = (orientation == -1 ? 1 : orientation);
    }

}
