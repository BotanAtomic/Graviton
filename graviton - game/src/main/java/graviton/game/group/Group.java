package graviton.game.group;

import graviton.game.client.player.Player;

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
        this.send("PM+" + player.getPacket("PM"));
        this.players.add(player);
        player.setGroup(this);
        sendPackets(player);
    }

    public void removeMember(Player player) {
        if(chief.getId() == player.getId()) {
            players.forEach(member -> member.setGroup(null));
            this.send("PV");
            this.send("IH");
            players.clear();
            return;
        }
        this.players.remove(player);

        if(players.size() == 1) {
            players.forEach(member -> member.setGroup(null));
            this.send("PV");
            this.send("IH");
            players.clear();
        } else {
            player.setGroup(null);
            player.send("PV");
            player.send("IH");
            this.send("PM-" + player.getId());
        }
    }

    public void kick(Player kicker,Player kicked) {
        if(chief.getId() != kicker.getId())
            return;
        kicked.send("PV" + kicker.getId());
        kicked.send("IH");
        removeMember(kicked);
        if(players.size() == 1)
            removeMember(chief);

    }

    public void send(String packet) {
        locker.lock();
        this.players.forEach(player -> player.send(packet));
        locker.unlock();
    }

    private List<Player> asList(Player... players) {
        List<Player> list = new CopyOnWriteArrayList<>();
        Collections.addAll(list, players);
        return list;
    }

    private void sendPackets() {
        this.send("PCK"+ chief.getName());
        this.send("PL"+chief.getId());
        final String[] packet = {""};
        this.players.stream().forEach(player -> packet[0] += ("|" + player.getPacket("PM")));
        this.send("PM+" + packet[0].substring(1));
    }

    private void sendPackets(Player newPlayer) {
        newPlayer.send("PCK" + chief.getName());
        newPlayer.send("PL" + chief.getId());
        final String[] packet = {""};
        this.players.stream().forEach(player -> packet[0] += ("|" + player.getPacket("PM")));
        newPlayer.send("PM+" + packet[0].substring(1));
    }

}
