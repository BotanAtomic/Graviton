package graviton.network.login;

import com.google.inject.Inject;
import com.google.inject.Injector;
import graviton.api.Client;
import graviton.database.data.AccountData;
import graviton.database.data.PlayerData;
import graviton.database.data.ServerData;
import graviton.game.Account;
import graviton.game.Player;
import graviton.game.Server;
import graviton.login.Manager;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.mina.core.session.IoSession;

import java.util.List;

/**
 * Created by Botan on 07/07/2015.
 */
@Data
@Slf4j
public class LoginClient implements Client {
    @Inject
    Manager manager;

    @Inject
    AccountData accountData;
    @Inject
    PlayerData playerData;
    @Inject
    ServerData serverData;

    private final long id;
    private final String key;
    private final IoSession session;

    private Statut statut;
    private Account account;

    public LoginClient(IoSession session, String key, Injector injector) {
        injector.injectMembers(this);
        this.id = session.getId();
        this.session = session;
        this.key = key;
        this.statut = Statut.CONNECTION;
        this.manager.addClient(this);
    }

    @Override
    public void parsePacket(String packet) {
        switch (statut) {
            case CONNECTION:
                if (!packet.contains("@")) {
                    session.close(true);
                    return;
                }
                String[] args = packet.split("@");
                log.info("[Session {}] checking username [{}] & password [{}]", id, args[0], args[1]);
                if (!accountData.isGood(args[0], args[1], this)) {
                    send("AlEf");
                    session.close(true);
                    return;
                }
                this.account = accountData.load(args[0]);
                if (account != null)
                    this.account.setClient(this);
                else
                    session.close(true);
                statut = Statut.SERVER;
                break;
            case NICKNAME:
                String[] forbiden = {"admin", "modo", "moderateur", " ", "&", "�", "\"", "'",
                        "(", "-", "�", "_", "�", "�", ")", "=", "~", "#",
                        "{", "[", "|", "`", "^", "@", "]", "}", "�", "+",
                        "^", "$", "�", "*", ",", ";", ":", "!", "<", ">",
                        "�", "�", "%", "�", "?", ".", "/", "�", "\n", account.getName()};
                for (String forbidenWord : forbiden)
                    if (packet.contains(forbidenWord)) {
                        send("AlEs");
                        return;
                    }
                if (!accountData.isAvaiableNickname(packet)) {
                    send("AlEs");
                    return;
                }
                account.setPseudo(packet);
                accountData.updateNickname(account);
                statut = Statut.SERVER;
                sendInformations();
                break;
            case SERVER:
                switch (packet.substring(0, 2)) {
                    case "AF":
                        sendFriendListPacket(packet.substring(2));
                        break;
                    case "Af":
                        sendInformations();
                        break;
                    case "AX":
                        selectServer(Integer.parseInt(packet.substring(2)));
                        break;
                    case "Ax":
                        send("AxK" + serverList());
                        break;
                    default:
                        log.info("[Login] Packet server not found -> {}", packet);
                }
                break;
        }
    }

    private void sendFriendListPacket(String name) {
        List<Player> players = playerData.getPlayers(name);
        send((players.isEmpty() ? "AF" : getList(players)));
    }

    private String getList(List<Player> players) {
        String list = null;
        for (Server server : manager.getServers().values())
            list += (server.getId()) + (",") + (getNumber(players, server.getId())) + (";");
        return "AF" + list;
    }

    private int getNumber(List<Player> players, int id) {
        int i = 0;
        for (Player character : players)
            if (character.getServer() == id)
                i++;
        return i;
    }

    private void sendInformations() {
        if (account.getPseudo() == null || account.getPseudo().isEmpty()) {
            send("AlEr");
            statut = Statut.NICKNAME;
            return;
        }
        playerData.loadAll(account);
        send("Af0|0|0|1|-1");
        send("Ad" + account.getPseudo());
        send("Ac0");
        send(serverData.getHostList());
        send("AlK" + (account.getRank() != 0 ? 1 : 0));
        send("AQ" + account.getQuestion());
    }

    private String serverList() {
        StringBuilder sb = new StringBuilder("31556864852");
        for (Server server : manager.getServers().values()) {
            int i = 0;
            for (Player player : account.getPlayers()) {
                if (player.getServer() == server.getId())
                    i++;
            }
            sb.append("|").append(server.getId()).append(",").append(i != 0 ? i : 1);
        }
        return sb.toString();
    }

    private void selectServer(int serverID) {
        Server server = manager.getServers().get(serverID);
        if (server == null) {
            send("AXEr");
            session.close(true);
            return;
        }
        if (server.getState() != Server.State.ONLINE) {
            send("AXEd");
            session.close(true);
            return;
        }
        server.send("+" + account.getId());
        StringBuilder sb = new StringBuilder();
        sb.append("AYK").append(server.getIp());
        sb.append(":").append(server.getPort()).append(";");
        sb.append(account.getId());
        send(sb.toString());
        manager.getConnected().put(account.getId(), serverID);
    }

    @Override
    public void kick() {
        if (account != null)
            account.delete();
        manager.removeClient(this);
        session.close(true);
    }

    @Override
    public void send(String packet) {
        session.write(packet);
    }

    public enum Statut {
        CONNECTION,
        NICKNAME,
        SERVER
    }
}
