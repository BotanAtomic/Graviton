package graviton.game.client;

import com.google.inject.Inject;
import com.google.inject.Injector;
import graviton.common.Pair;
import graviton.core.Main;
import graviton.database.DatabaseManager;
import graviton.game.GameManager;
import graviton.game.admin.Admin;
import graviton.game.client.player.Player;
import graviton.game.enums.Rank;
import graviton.network.game.GameClient;
import lombok.Data;
import org.joda.time.Interval;
import org.joda.time.Period;

import java.util.*;

/**
 * Created by Botan on 19/06/2015.
 */
@Data
public class Account {
    @Inject
    GameManager manager;
    @Inject
    DatabaseManager databaseManager;

    private final Injector injector;

    private final int id;
    private final String answer;
    private final Rank rank;
    private final String pseudo;

    private String ipAdress, lastConnection;

    private GameClient client;

    private List<Integer> friends;
    private List<Integer> enemmy;

    private List<Player> players;
    private Player currentPlayer;

    private Pair<Integer, Date> mute;

    public Account(int id, String answer, String pseudo, int rank,Injector injector) { //TODO : get IP for last IP
        injector.injectMembers(this);
        this.injector = injector;
        this.id = id;
        this.answer = answer;
        this.pseudo = pseudo;
        this.manager.getAccounts().put(id, this);
        this.players = databaseManager.loadPlayers(this);
        this.friends = new ArrayList<>();
        this.enemmy = new ArrayList<>();
        this.rank = Rank.values()[rank - 1];
        if (rank != 0)
            new Admin(this.rank, this,injector);
    }

    public Player getPlayer(int id) {
        final Player[] player = {null};
        this.players.stream().filter(player1 -> player1.getId() == id).forEach(playerSelected -> player[0] = playerSelected);
        return player[0];
    }

    public void createPlayer(String name, byte classeId, byte sexe, int[] colors) {
        if (players.add(new Player(name, sexe, classeId, colors, this,injector))) {
                client.send("AAK");
                client.send(getPlayersPacket());
            return;
        }
        client.send("AAEF");
    }

    public String getPlayersPacket() {
        if (players.isEmpty())
            return "ALK31536000000|0";
        String packet = "ALK31536000000|" + (this.players.size() == 1 ? 2 : this.players.size());
        for (Player player : this.players)
            packet += (player.getPacket("ALK"));
        return packet;
    }

    public final void setLastConnection() {
        Calendar calendar = GregorianCalendar.getInstance();
        this.lastConnection = (calendar.get(Calendar.YEAR) + "~" + calendar.get(Calendar.MONTH) + "~" + calendar.get(Calendar.DAY_OF_MONTH) + "~" + calendar.get(Calendar.HOUR_OF_DAY) + "~" + calendar.get(Calendar.MINUTE));
    }

    public void setCurrentPlayer(Player currentPlayer) {
        this.currentPlayer = currentPlayer;
        this.client.setCurrentPlayer(currentPlayer);
    }

    public void close() {
        if (currentPlayer != null) {
            currentPlayer.save();
            currentPlayer.getPosition().getMap().removeCreature(currentPlayer);
            manager.getPlayers().remove(currentPlayer.getId());
        }
        manager.getAccounts().remove(this.id);
    }

    public void setOnline() {
        //TODO : Set online for prevent friends
    }

    public void mute(int time, Player player, String reason) {
        this.mute = new Pair<>(time, new Date());
        manager.mute(currentPlayer, player, time, reason);
    }

    public boolean canSpeak() {
        if (this.mute != null) {
            Period period = new Interval(this.mute.getSecond().getTime(), new Date().getTime()).toPeriod();
            int remainingTime = (this.mute.getFirst() - period.getMinutes());
            if (period.getMinutes() < this.mute.getFirst()) {
                currentPlayer.sendText("A force de trop parler, vous en avez perdu la voix... Vous devriez vous taire pendant les " + remainingTime + " prochaine " + (remainingTime > 1 ? "minutes" : "minute"), "FF0000");
                return false;
            }
            this.mute = null;
        }
        return true;
    }

    public void addFriend(Player friend) {
        if (friend != null)
            friends.add(friend.getAccount().getId());
        send("FAef");
    }

    public void removeFriend(Player friend) {
        if (friend != null)
            friends.remove(friend.getAccount().getId());
    }

    public void send(String packet) {
        this.client.getSession().write(packet);
    }
}
