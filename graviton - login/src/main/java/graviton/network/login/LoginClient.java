package graviton.network.login;

import graviton.enums.LoginStatus;
import graviton.game.Account;
import graviton.network.NetworkManager;
import graviton.network.login.packet.PacketParser;
import lombok.Getter;
import org.apache.mina.core.session.IoSession;

/**
 * Created by Botan on 06/06/2015.
 */
public class LoginClient {
    @Getter
    LoginStatus status;
    @Getter
    Account account;
    private PacketParser parser;
    private NetworkManager networkManager;
    @Getter
    private long id;
    @Getter
    private String key;
    @Getter
    private IoSession session;

    public LoginClient(IoSession session, String key, NetworkManager manager) {
        this.session = session;
        this.id = session.getId();
        this.key = key;
        this.networkManager = manager;
        this.networkManager.getLoginClients().put(this.id, this);
        this.parser = manager.getParser();
    }

    public void setStatus(LoginStatus status) {
        this.status = status;
    }

    public void kick() {
        if (account != null)
            if (networkManager.getConfig().getDatabase().getAccountData().getAccounts().containsValue(account))
                networkManager.getConfig().getDatabase().getAccountData().getAccounts().remove(account);

        if (networkManager.getLoginClients().containsKey(this.id))
            networkManager.getLoginClients().remove(this);
        this.session.close(true);
    }

    public void parsePacket(String packet) {
        parser.parse(packet, this);
    }

    public void send(String packet) {
        this.session.write(packet);
    }

    public void setAccount(Account account) {
        this.account = account;
        this.account.setClient(this);
    }
}
