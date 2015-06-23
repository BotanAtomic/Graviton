package graviton.game.maps;

import graviton.common.Action;
import graviton.game.client.player.Player;
import lombok.Data;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by Botan on 22/06/2015.
 */
@Data
public class Cell {
    private int id;
    private Maps map;
    private boolean walkable;
    private Action action;

    private List<Player> players;

    public Cell(int id, Maps map, boolean walkable) {
        this.id = id;
        this.map = map;
        this.walkable = walkable;
        this.players = new CopyOnWriteArrayList<>();
    }
}
