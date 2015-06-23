package graviton.game.client;

import graviton.core.Main;
import graviton.database.DatabaseManager;
import graviton.enums.DataType;
import graviton.game.client.player.Player;
import graviton.network.game.GameClient;
import lombok.Getter;
import lombok.Setter;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by Botan on 19/06/2015.
 */
public class Account {
    @Getter
    private final int id;

    @Getter
    @Setter
    private String ipAdress, lastConnection;
    @Getter
    @Setter
    private GameClient client;

    @Getter
    private List<Player> players;
    @Getter
    @Setter
    private Player currentPlayer;

    public Account(int id) {
        this.id = id;
        System.err.print(this.getLastConnection());
        try {
            this.players = (CopyOnWriteArrayList<Player>) Main.getInstance(DatabaseManager.class).getData().get(DataType.PLAYER).loadAll(this);
        } catch (Exception e) {
            e.printStackTrace();
            this.players = new CopyOnWriteArrayList<>();
        }

    }

    public Player getPlayer(int id) {
        for (Player player : players)
            if (player.getId() == id)
                return player;
        return null;
    }

    public boolean createPlayer(String name, byte classeId, byte sexe, int[] colors) {
        try {
            if (players.add(new Player(name, sexe, classeId, colors, this))) {
                client.send("AAK");
                client.send(getPlayersList());
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public String getPlayersList() {
        if (players.size() == 0)
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
        this.client.setPlayer(currentPlayer);
    }
}
