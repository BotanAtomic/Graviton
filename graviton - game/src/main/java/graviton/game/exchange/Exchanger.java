package graviton.game.exchange;

import graviton.game.client.player.Player;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Botan on 25/12/2015.
 */

public class Exchanger {
    private final Player creature;
    private long kamas = 0;
    private boolean ok;

    private final Map<Integer,Integer> objects = new HashMap<>();

    public Exchanger(Player creature) {
        this.creature = creature;
    }

    public Player getCreature() {
        return creature;
    }

    public Map<Integer,Integer> getObjects() {
        return objects;
    }

    public long getKamas() {
        return this.kamas;
    }

    public void setKamas(long kamas) {
        this.kamas = kamas;
    }

    public boolean isOk() {
        return ok;
    }

    public void setOk(boolean ok) {
        this.ok = ok;
    }


    public void send(String packet) {
        if(creature != null)
            creature.send(packet);
    }

    public void quit() {
        if(creature != null)
            this.creature.setExchange(null);
    }
}
