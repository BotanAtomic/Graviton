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
        this.players = asList(chief, second);
        this.players.forEach(player -> player.setGroup(this));
        this.locker = new ReentrantLock();
        sendPackets();
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

    private final List<Player> asList(Player... players) {
        List<Player> list = new CopyOnWriteArrayList<>();
        Collections.addAll(list,players);
        return list;
    }

    private void sendPackets() {
        this.send("PCK"+chief.getName());
        this.send("PL"+chief.getId());
        final String[] packet = {"PM+" + chief.getPacket("PM")};
        this.players.stream().filter(player1 -> player1 != chief).forEach(player -> packet[0] += ("|" + player.getPacket("PM")));
        this.send(packet[0]);
    }
}
