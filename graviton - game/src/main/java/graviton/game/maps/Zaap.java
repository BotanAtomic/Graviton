package graviton.game.maps;



/**
 * Created by Botan on 22/06/2015.
 */
public class Zaap {
    private final Maps map;
    private final int cell;

    public Zaap(Maps map, int cell) {
        this.map = map;
        this.cell = cell;
    }

    public Maps getMap() {
        return this.map;
    }

    public int getCell() {
        return this.cell;
    }

    public int getCost(Maps maps) {
        return maps == this.map ? 0 : (10 * (Math.abs(this.map.getX() - maps.getX()) + Math.abs(this.map.getY() - maps.getY()) - 1));
    }
}
