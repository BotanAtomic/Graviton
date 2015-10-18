package graviton.game.client.player;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by Botan on 03/10/2015.
 */
public class Group {
    private final Player chief;
    private final List<Player> players;

    private final ReentrantLock locker;

    public Group(Player chief, Player second) {
        this.chief = chief;
        this.players = new CopyOnWriteArrayList<>();
        asList(chief, second);
        this.locker = new ReentrantLock();
    }

    public void addMember(Player player) {
        this.players.add(player);
    }

    public void removeMember(Player player) {
        this.players.remove(player);
    }

    public void send(String packet) {
        locker.lock();
        this.players.forEach(player -> player.send(packet));
        locker.unlock();
    }

    private final void asList(Player... players) {
        Collections.addAll(this.players, players);
    }
}
