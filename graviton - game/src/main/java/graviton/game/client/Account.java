package graviton.game.client;

import com.google.inject.Inject;
import com.google.inject.Injector;
import graviton.common.Pair;
import graviton.factory.AccountFactory;
import graviton.factory.PlayerFactory;
import graviton.game.GameManager;
import graviton.game.admin.Admin;
import graviton.game.client.player.Player;
import graviton.game.client.player.component.CommandManager;
import graviton.game.enums.Rank;
import graviton.game.trunks.Trunk;
import graviton.network.game.GameClient;
import lombok.Data;
import org.joda.time.Interval;
import org.joda.time.Period;
import org.jooq.Record;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static graviton.database.utils.login.Tables.ACCOUNTS;

/**
 * Created by Botan on 19/06/2015.
 */
@Data
public class Account {
    private final Injector injector;
    private final int id;
    private final String answer;
    private final Rank rank;
    private final String pseudo;
    @Inject
    GameManager manager;
    @Inject
    PlayerFactory playerFactory;
    @Inject
    AccountFactory accountFactory;
    @Inject
    CommandManager commandManager;
    private String ipAdress;

    private GameClient client;

    private boolean seefriends, online;
    private List<Integer> friends;
    private List<Integer> enemies;

    private List<Player> players;
    private Player currentPlayer;

    private Pair<Integer, Date> mute;

    private Trunk bank;

    private Admin admin;

    public Account(Record record, Injector injector) {
        injector.injectMembers(this);
        this.injector = injector;
        this.id = record.getValue(ACCOUNTS.ID);
        this.accountFactory.getElements().put(id, this);
        this.answer = record.getValue(ACCOUNTS.ANSWER);
        this.pseudo = record.getValue(ACCOUNTS.PSEUDO);
        this.players = playerFactory.load(this);
        this.friends = convertToList(record.getValue(ACCOUNTS.FRIENDS));
        this.enemies = convertToList(record.getValue(ACCOUNTS.ENEMIES));
        this.rank = Rank.values()[record.getValue(ACCOUNTS.RANK)];
        this.bank = new Trunk(record.getValue(ACCOUNTS.BANK),injector);
        if (rank != Rank.PLAYER)
            this.admin = new Admin(this.rank, this, injector);

    }

    private List<Integer> convertToList(String data) {
        List<Integer> list = new ArrayList<>();
        if (data == null || data.isEmpty())
            return list;
        for (String value : data.split(";"))
            list.add(Integer.parseInt(value));
        return list;
    }

    public Player getPlayer(int id) {
        final Player[] player = {null};
        this.players.stream().filter(player1 -> player1.getId() == id).forEach(playerSelected -> player[0] = playerSelected);
        return player[0];
    }

    public void createPlayer(String name, byte classeId, byte sexe, int[] colors) {
        if (players.add(new Player(name, sexe, classeId, colors, this, injector))) {
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

    public void setCurrentPlayer(Player currentPlayer) {
        this.currentPlayer = currentPlayer;
        this.client.setCurrentPlayer(currentPlayer);
    }

    public void close() {
        if (currentPlayer != null) {
            currentPlayer.save();
            currentPlayer.getPosition().getMap().removeCreature(currentPlayer);
            playerFactory.delete(currentPlayer.getId());
        }
        accountFactory.getElements().remove(this.id);

        if (admin != null)
            admin.remove();
    }

    public void setOnline() {
        this.online = true;

        for (Integer i : friends) {
            Account account = accountFactory.getElements().get(i);
            if (account == null) return;

            if (account.isOnline() && account.getFriends().contains(id))
                account.send("Im0143;" + this.pseudo + " (" + currentPlayer.getPacketName() + ")");
        }
    }

    public void mute(int time, Player player, String reason) {
        this.mute = new Pair<>(time, new Date());
        manager.mute(currentPlayer, player, time, reason);
    }

    public boolean canSpeak() {
        if (this.mute != null) {
            Period period = new Interval(this.mute.getValue().getTime(), new Date().getTime()).toPeriod();
            int remainingTime = (this.mute.getKey() - period.getMinutes());
            if (period.getMinutes() < this.mute.getKey()) {
                currentPlayer.sendText("A force de trop parler, vous en avez perdu la voix... Vous devriez vous taire pendant les " + remainingTime + (remainingTime > 1 ? " prochaines " : " prochaine ") + (remainingTime > 1 ? "minutes" : "minute"), "FF0000");
                return false;
            }
            this.mute = null;
        }
        return true;
    }

    public void addFriend(Player friend) {
        if (friends.contains(friend.getAccount().getId())) return;

        if (friend != null)
            friends.add(friend.getAccount().getId());
        send("FAe" + friend.getName());
        accountFactory.update(this);
    }

    public void update() {
        accountFactory.update(this);
    }

    public void removeFriend(String friend) {
        Account account = accountFactory.getByName(friend.substring(1));
        if (account != null)
            friends.remove((java.lang.Object) account.getId());
        send("FD" + account.getId());
        accountFactory.update(this);
    }

    public void send(String packet) {
        this.client.getSession().write(packet);
    }

    public String parseFriends() {
        if (friends.isEmpty()) return "";
        final String[] data = {""};
        this.friends.forEach(integer -> data[0] += integer + ";");
        return data[0];
    }

    public String parseEnemies() {
        if (enemies.isEmpty()) return "";
        final String[] data = {""};
        this.enemies.forEach(integer -> data[0] += integer + ";");
        return data[0].substring(0, data[0].length() - 1);
    }

    public String parseMute() {
        return mute == null ? "" : mute.getKey() + ";" + mute.getValue().getTime();
    }

    public String getFriendsPacket() {
        if (friends.isEmpty()) return "FL";
        StringBuilder builder = new StringBuilder("FL");
        Account account;
        for (Integer i : friends) {
            account = accountFactory.get(i);
            if (account == null) continue;
            builder.append("|").append(account.getPseudo());
            if (account.isOnline())
                builder.append(account.getPlayerListPacket(this.id));
        }
        return builder.toString();
    }

    public String getPlayerListPacket(int id) {
        StringBuilder builder = new StringBuilder();
        builder.append(";");
        builder.append("0;"); //TODO : is in fight = 1
        builder.append(currentPlayer.getName()).append(";");
        if (this.friends.contains(id)) {
            builder.append(currentPlayer.getLevel()).append(";");
            builder.append(currentPlayer.getAlignement().getType().getId()).append(";");
        } else {
            builder.append("?;");
            builder.append("-1;");
        }
        builder.append(currentPlayer.getClasse().getId()).append(";");
        builder.append(currentPlayer.getSex()).append(";");
        builder.append(currentPlayer.getGfx());
        return builder.toString();
    }

    public void launchCommand(String arguments) {
        commandManager.launchAdminCommand(currentPlayer, arguments.split(" "));
    }

    public int getBankPrice() {
        return bank.getObjects().size();
    }
}
