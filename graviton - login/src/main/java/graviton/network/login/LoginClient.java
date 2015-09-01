package graviton.network.login;

import graviton.api.Client;
import graviton.database.data.AccountData;
import graviton.database.data.PlayerData;
import graviton.database.data.ServerData;
import graviton.game.Account;
import graviton.game.Player;
import graviton.game.Server;
import graviton.login.Login;
import graviton.login.Main;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.mina.core.session.IoSession;

import java.sql.ResultSet;

/**
 * Created by Botan on 07/07/2015.
 */
@Data
@Slf4j
public class LoginClient implements Client {
    private final Login login = Main.getInstance(Login.class);
    private final AccountData accountData = (AccountData) login.getData("account");
    private final PlayerData playerData = (PlayerData) login.getData("player");
    private final ServerData serverData = (ServerData) login.getData("server");

    private final long id;
    private final String key;
    private final IoSession session;
    private Statut statut;

    private Account account;

    public LoginClient(IoSession session, String key) {
        this.id = session.getId();
        this.session = session;
        this.key = key;
        this.statut = Statut.CONNECTION;
    }

    @Override
    public void parsePacket(String packet) throws Exception {
        if (packet.equals("1.29.1")) {
            log.info("[Session {}] checking version > {}", id, "1.29.1");
            return;
        }
        switch (statut) {
            case CONNECTION:
                String[] args = packet.split("@");
                log.info("[Session {}] checking username [{}] & password [{}]", id, args[0], args[1]);
                if (!accountData.isGood(args[0], args[1], this)) {
                    send("AlEf");
                    session.close(true);
                    return;
                }
                this.account = accountData.load(args[0]);
                this.account.setClient(this);
                statut = Statut.SERVER;
                break;
            case NICKNAME:
                String forbiden[] = {"admin", "modo", "moderateur", " ", "&", "é", "\"", "'",
                        "(", "-", "è", "_", "ç", "à", ")", "=", "~", "#",
                        "{", "[", "|", "`", "^", "@", "]", "}", "°", "+",
                        "^", "$", "ù", "*", ",", ";", ":", "!", "<", ">",
                        "¨", "£", "%", "µ", "?", ".", "/", "§", "\n", account.getName()};
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
                        break;
                    case "Ax":
                        send("AxK" + serverList());
                        break;
                    default:
                        log.info("Packet server not found -> {}", packet);
                }
                break;
        }
    }

    private void sendFriendListPacket(String packet) {
        //TODO : send Friend List Packet
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
        for (Server server : login.getServers().values()) {
            int i = 0;
            for (Player player : account.getPlayers()) {
                if (player.getServer() == server.getId())
                    i++;
            }
            sb.append("|").append(server.getId()).append(",").append(i != 0 ? i : 1);
        }
        return sb.toString();
    }

    public void kick() {
        if (login.getAccounts().containsKey(account.getId()))
            login.getAccounts().remove(account.getId());
        if (login.getClients().get("login").containsKey(id))
            login.getClients().remove(id);
        session.close(true);
    }

    public void send(String packet) {
        session.write(packet);
    }

    public enum Statut {
        CONNECTION,
        NICKNAME,
        SERVER
    }
}
