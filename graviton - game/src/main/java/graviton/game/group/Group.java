package graviton.game.group;

import graviton.game.client.player.Player;
import graviton.game.client.player.packet.Packets;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by Botan on 03/10/2015.
 */
public class Group {
    private final Player chief;
    private final Map<Integer, Player> players;

    private final ReentrantLock locker;

    public Group(Player chief, Player second) {
        this.chief = chief;
        this.players = new ConcurrentHashMap<>();
        this.locker = new ReentrantLock();
        this.players.put(chief.getId(), chief);
        this.players.put(second.getId(), second);
        this.players.values().forEach(player -> player.setGroup(this));
        this.send(getPacket());
    }

    public void addMember(Player player) {
        this.send("PM+" + player.getPacket(Packets.PM));
        this.players.put(player.getId(), player);
        player.setGroup(this);
        player.send(getPacket());
    }

    public void removeMember(Player player) {
        if (chief.getId() == player.getId() || (players.size() - 1) == 1) {
            players.values().forEach(member -> member.setGroup(null));
            this.send("PV");
            this.send("IH");
            players.clear();
        } else {
            this.players.remove(player);
            player.setGroup(null);
            player.send("PV");
            player.send("IH");
            this.send("PM-" + player.getId());
        }
    }

    public void kick(Player kicker, Player kicked) {
        if (chief.getId() != kicker.getId())
            return;
        kicked.send("PV" + kicker.getId());
        kicked.send("IH");
        removeMember(kicked);
        if (players.size() == 1)
            removeMember(chief);
    }

    public void send(String packet) {
        locker.lock();
        this.players.values().forEach(player -> player.send(packet));
        locker.unlock();
    }

    private String getPacket() {
        StringBuilder builder = new StringBuilder("PCK").append(chief.getName()).append("\n");
        builder.append("PL").append(chief.getId()).append("\n");
        builder.append("PM+").append(chief.getId());
        this.players.values().stream().forEach(player -> builder.append(player.getPacket(Packets.PM)).append("|"));
        return builder.toString().substring(0, builder.length() - 1);
    }
}
