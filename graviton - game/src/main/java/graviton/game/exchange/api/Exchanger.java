package graviton.game.exchange.api;

import graviton.game.client.player.Player;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Botan on 25/12/2015.
 */

public class Exchanger {
    private final Player creature;
    private long kamas = 0;
    private boolean ready = false;

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

    public boolean isReady() {
        return ready;
    }

    public void setReady(boolean ready) {
        this.ready = ready;
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
