package graviton.network.login;

import com.google.inject.Inject;
import com.google.inject.Injector;
import graviton.api.Client;
import graviton.core.GlobalManager;
import graviton.database.Database;
import graviton.game.Account;
import graviton.game.Player;
import graviton.game.Server;
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
    private final long id;
    private final String key;
    private final IoSession session;
    @Inject
    GlobalManager globalManager;
    @Inject
    Database database;
    private byte status;
    private Account account;

    public LoginClient(IoSession session, String key, Injector injector) {
        injector.injectMembers(this);
        this.id = session.getId();
        this.session = session;
        this.key = key;
        this.status = 1;
        session.setAttribute("client", this);
    }

    @Override
    public void parsePacket(String packet) {
        switch (status) {
            case 1:
                String[] args = packet.split("\n");
                log.info("[Session {}] checking username [{}] & password [{}]", id, args[0], args[1]);
                if (!database.isGoodAccount(args[0], args[1], this)) {
                    send("AlEf");
                    session.close(true);
                    return;
                }
                this.account = database.loadAccount(args[0]);

                if (account != null)
                    this.account.setClient(this);
                else
                    session.close(true);
                status = 3;
                break;
            case 2:
                for (String forbiddenWord : new String[]{"admin", "modo", "moderateur", " ", "&", "é", "\"", "'",
                        "(", "-", "è", "_", "ç", "à", ")", "=", "~", "#",
                        "{", "[", "|", "`", "^", "@", "]", "}", "°", "+",
                        "^", "$", "ù", "*", ",", ";", ":", "!", "<", ">",
                        "¨", "£", "%", "µ", "?", ".", "/", "§", "\n", account.getName()})
                    if (packet.contains(forbiddenWord)) {
                        send("AlEs");
                    } else if (!database.isAvaiableNickname(packet)) {
                        send("AlEs");
                    } else {
                        account.setPseudo(packet);
                        database.updateNickname(account);
                        status = 3;
                        sendInformations();
                    }
                break;
            case 3:
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
        List<Player> players = database.getPlayers(name);
        if (players == null)
            send("AF");
        else
            send((players.isEmpty() ? "AF" : getList(players)));
    }

    private String getList(List<Player> players) {
        StringBuilder builder = new StringBuilder("AF");
        globalManager.getServers().values().stream().filter(server -> getNumberOfPlayer(players, server.getId()) != 0).forEach(playerSever -> builder.append(playerSever.getId()).append(",").append(getNumberOfPlayer(players, playerSever.getId())).append(";"));
        return builder.toString();
    }

    private int getNumberOfPlayer(List<Player> players, int server) {
        final int[] number = {0};
        players.stream().filter(player -> player.getServer() == server).forEach(validPlayer -> number[0]++);
        return number[0];
    }

    private void sendInformations() {
        if (account.getPseudo() == null || account.getPseudo().isEmpty()) {
            send("AlEr");
            status = 3;
            return;
        }
        database.loadPlayers(account);
        send("Af0|0|0|1|-1");
        send("Ad" + account.getPseudo());
        send("Ac0");
        send(globalManager.getHostList());
        send("AlK" + (account.getRank() != 0 ? 1 : 0));
        send("AQ" + account.getQuestion());
    }

    private String serverList() {
        StringBuilder builder = new StringBuilder("31556864852");
        globalManager.getServers().values().forEach(server -> builder.append("|").append(server.getId()).append(",").append(getNumberOfPlayer(account.getPlayers(), server.getId())));
        return builder.toString();
    }

    private void selectServer(int serverID) {
        Server server = globalManager.getServers().get(serverID);
        if (server == null) {
            send("AXEr");
            session.close(true);
            return;
        }
        if (server.getState() != 1) {
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
        globalManager.getConnectedClient().put(account.getId(), serverID);
    }

    @Override
    public void kick() {
        if (account != null)
            account.delete();
    }

    @Override
    public void send(String packet) {
        session.write(packet);
    }
}
