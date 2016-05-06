package graviton.core;

import com.google.inject.Inject;
import com.google.inject.Injector;
import graviton.api.Manageable;
import graviton.game.Account;
import graviton.game.Server;
import graviton.network.exchange.ExchangeClient;
import graviton.network.exchange.ExchangeNetwork;
import graviton.network.login.LoginNetwork;
import lombok.Data;
import org.apache.mina.core.session.IoSession;

import java.util.*;

/**
 * Created by Botan on 06/07/2015.
 */
@Data
public class GlobalManager {
    @Inject
    Injector injector;

    @Inject
    LoginNetwork loginNetwork;

    @Inject
    ExchangeNetwork exchangeNetwork;

    private Map<Integer, Integer> connectedClient;

    private Map<Integer, Account> accounts;

    private Map<Integer, Server> servers;

    private List<Manageable> manageableList;

    public GlobalManager(Manageable firstManageable) {
        this.manageableList = new ArrayList() {{
            add(firstManageable);
        }};
        this.accounts = new HashMap();
        this.connectedClient = new HashMap();
    }

    public void addManageable(Manageable manageable) {
        this.manageableList.add(manageable);
    }

    public GlobalManager start() {
        this.manageableList.forEach(Manageable::configure);
        return this;
    }

    public void stop() {
        this.manageableList.forEach(Manageable::stop);
    }

    public Collection<IoSession> getLoginClients() {
        return loginNetwork.getSessions();
    }

    public Collection<ExchangeClient> getExchangeClients() {
        return exchangeNetwork.getSessions();
    }

    public String getHostList() {
        StringBuilder sb = new StringBuilder("AH");
        List<Server> list = new ArrayList<>();
        list.addAll(servers.values());
        list.forEach((server) -> sb.append(server.getId()).append(";").append(server.getState()).append(";110;1|"));
        return sb.toString();
    }

    public final String getServerName(boolean connected) {
        String[] name = {" ["};
        if (connected) {
            if (getExchangeClients().size() == 0)
                return "";
            getExchangeClients().forEach(exchangeClient -> name[0] += exchangeClient.getServer().getKey() + "/");
        } else
            servers.values().forEach(server -> name[0] += server.getKey() + "/");
        return name[0].substring(0, name[0].length() - 1) + "]";
    }

    public final String getServerForApplication() {
        if (getExchangeClients().isEmpty())
            return "L";

        StringBuilder builder = new StringBuilder("L");
        getExchangeClients().forEach(exchangeClient -> builder.append(exchangeClient.getServer().getKey()).append(";"));
        return builder.substring(0, builder.length() - 1);
    }

    public Server getServerByKey(String key) {
        for (Server server : servers.values())
            if (server.getKey().equals(key))
                return server;
        return null;
    }

    public void checkAccount(int id) {
        if (accounts.get(id) != null) {
            accounts.get(id).getClient().send("AlEa");
            accounts.get(id).getClient().kick();
        }
        if (connectedClient.get(id) != null) {
            servers.get(connectedClient.get(id)).send("-" + id);
            connectedClient.remove(id);
        }
    }
}
