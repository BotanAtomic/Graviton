package graviton.game.client;

import com.google.inject.Inject;
import com.google.inject.Injector;
import graviton.common.Pair;
import graviton.factory.AccountFactory;
import graviton.factory.PlayerFactory;
import graviton.game.GameManager;
import graviton.game.action.player.CommandManager;
import graviton.game.admin.Admin;
import graviton.game.client.player.Player;
import graviton.game.client.player.packet.Packets;
import graviton.game.enums.Rank;
import graviton.game.trunk.Trunk;
import graviton.network.game.GameClient;
import lombok.Data;
import org.joda.time.Interval;
import org.joda.time.Period;
import org.jooq.Record;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static graviton.database.utils.login.Tables.ACCOUNTS;

/**
 * Created by Botan on 19/06/2015.
 */
@Data
public class Account {
    private final PlayerFactory playerFactory;
    private final AccountFactory accountFactory;
    private final Injector injector;
    private final int id;
    private final String answer;
    private final String pseudo;
    @Inject
    CommandManager commandManager;
    @Inject
    GameManager manager;
    private Rank rank;
    private String networkAddress;
    private String informations;
    private GameClient client;

    private boolean seeFriends, online;
    private List<Integer> friends;
    private List<Integer> enemies;

    private List<Player> players;
    private Player currentPlayer;

    private Pair<Integer, Date> mute;

    private Trunk bank;

    private Admin admin;

    public Account(Record record, PlayerFactory playerFactory, AccountFactory accountFactory, Injector injector) {
        injector.injectMembers(this);
        this.id = record.getValue(ACCOUNTS.ID);
        accountFactory.getElements().put(id, this);
        this.playerFactory = playerFactory;
        this.accountFactory = accountFactory;
        this.injector = injector;
        this.answer = record.getValue(ACCOUNTS.ANSWER);
        this.pseudo = record.getValue(ACCOUNTS.PSEUDO);
        this.friends = convertToList(record.getValue(ACCOUNTS.FRIENDS));
        this.enemies = convertToList(record.getValue(ACCOUNTS.ENEMIES));
        this.rank = Rank.values()[record.getValue(ACCOUNTS.RANK)];
        this.bank = new Trunk(record.getValue(ACCOUNTS.BANK), injector);
        this.informations = record.getValue(ACCOUNTS.INFORMATIONS);
        if (rank != Rank.PLAYER)
            this.admin = new Admin(this, injector);
        this.players = playerFactory.load(this);
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
        if (players == null || players.isEmpty())
            return "ALK31536000000|0";
        String packet = "ALK31536000000|" + (this.players.size() == 1 ? 2 : this.players.size());
        for (Player player : this.players)
            packet = packet.concat(player.getPacket(Packets.ALK));
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

    public void update() {
        accountFactory.update(this);
    }

    public void send(String packet) {
        this.client.getSession().write(packet);
    }

    public String parseMute() {
        return mute == null ? "" : mute.getKey() + ";" + mute.getValue().getTime();
    }

    public String getListPacket(boolean friends) {
        List<Integer> list = friends ? this.friends : this.enemies;
        if (list.isEmpty()) return friends ? "FL" : "iL";

        StringBuilder builder = new StringBuilder(friends ? "FL" : "iL");

        list.forEach(i -> {
            final Account account = accountFactory.get(i);
            if (account != null) {
                builder.append("|").append(account.getPseudo());
                if (account.isOnline())
                    builder.append(account.getPlayerListPacket(this.id));
            }
        });
        return builder.toString();
    }

    public String parseList(boolean friends) {
        List<Integer> list = friends ? this.friends : this.enemies;
        if (list.isEmpty()) return "";

        final String[] data = {""};
        list.forEach(integer -> data[0] = data[0].concat(integer + ";"));
        return data[0];
    }

    public void addInList(Player target, boolean friends) {
        List<Integer> list = friends ? this.friends : this.enemies;
        if (list.contains(target.getAccount().getId())) return;

        if (list != null)
            list.add(target.getAccount().getId());
        send((friends ? "FAe" : "iAe") + target.getName());
        accountFactory.update(this);
    }

    public void removeInList(String target, boolean friends) {
        List<Integer> list = friends ? this.friends : this.enemies;
        Account account = accountFactory.getByName(target.substring(1));

        if (account != null)
            list.remove((java.lang.Object) account.getId());
        send((friends ? "FD" : "iD") + account.getId());
        accountFactory.update(this);
    }

    public String getPlayerListPacket(int id) {
        StringBuilder builder = new StringBuilder();
        builder.append(";");
        builder.append(currentPlayer.getFight() != null ? "1;" : "0;");
        builder.append(currentPlayer.getName()).append(";");
        builder.append(friends.contains(id) ? currentPlayer.getLevel() : "?;").append(";");
        builder.append(friends.contains(id) ? currentPlayer.getAlignement().getType().getId() : "-1;").append(";");
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

    public String getNewInformations() {
        return new SimpleDateFormat("yyyy~MM~dd~HH~mm~").format(new Date()) + networkAddress;
    }
}
