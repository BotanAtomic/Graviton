package graviton.login;

import com.google.inject.Inject;
import com.google.inject.Injector;
import graviton.api.Client;
import graviton.common.Scanner;
import graviton.database.data.AccountData;
import graviton.database.data.PlayerData;
import graviton.database.data.ServerData;
import graviton.game.Account;
import graviton.game.Player;
import graviton.game.Server;
import graviton.network.NetworkManager;
import graviton.network.exchange.ExchangeClient;
import graviton.network.login.LoginClient;
import lombok.Data;
import org.apache.mina.core.session.IoSession;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Botan on 06/07/2015.
 */
@Data
public class Manager {
    @Inject
    Injector injector;
    @Inject
    Configuration configuration;
    @Inject
    NetworkManager manager;
    @Inject
    Scanner scanner;
    @Inject
    AccountData accountData;
    @Inject
    PlayerData playerData;
    @Inject
    ServerData serverData;

    private Map<Long, Client> clients;
    private Map<Integer, Integer> connected;

    private Map<Integer, Account> accounts;
    private Map<Integer, Player> players;
    private Map<Integer, Server> servers;

    private Date dateOfStart;

    public Manager() {
        this.accounts = new ConcurrentHashMap<>();
        this.players = new ConcurrentHashMap<>();
        this.servers = new ConcurrentHashMap<>();
        this.clients = new ConcurrentHashMap<>();
        this.connected = new ConcurrentHashMap<>();
        dateOfStart = new java.util.Date();
    }

    public Manager start() {
        initialize();
        manager.start();
        scanner.start(this);
        return this;
    }

    public void stop() {
        scanner.interrupt();
        manager.stop();
        configuration.getDatabase().stop();
    }

    private void initialize() {
        accountData.initialize();
        playerData.initialize();
        serverData.initialize();
    }

    public void addClient(Client client) {
        clients.put(client.getId(), client);
    }

    public void removeClient(Client client) {
        if (clients.containsValue(client))
            clients.remove(client.getId());
    }

    public Client getClient(IoSession session) {
        for (Client client : clients.values())
            if (client.getSession().getId() == session.getId())
                return client;
        return null;
    }

    public String getHostList() {
        return serverData.getHostList();
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
        String[] name = {"L"};
        if(getExchangeClients().size() == 0) {
            return "L";
        }
        getExchangeClients().forEach(exchangeClient -> name[0] += exchangeClient.getServer().getKey() + ";");
        return name[0].substring(0, name[0].length() - 1);
    }

    public Server getServerByKey(String key) {
        for (Server server : servers.values())
            if (server.getKey().equals(key))
                return server;
        return null;
    }

    public List<LoginClient> getLoginClients() {
        List<LoginClient> loginClients = new ArrayList<>();
        clients.values().stream().filter(client -> client instanceof LoginClient).forEach(client -> loginClients.add((LoginClient) client));
        return loginClients;
    }

    public List<ExchangeClient> getExchangeClients() {
        List<ExchangeClient> exchangeClients = new ArrayList<>();
        clients.values().stream().filter(client -> client instanceof ExchangeClient).forEach(client -> exchangeClients.add((ExchangeClient) client));
        return exchangeClients;
    }

    public void checkAccount(int id) {
        if (accounts.get(id) != null) {
            accounts.get(id).getClient().send("AlEa");
            accounts.get(id).getClient().getSession().close(true);
        }
        if (connected.get(id) != null) {
            servers.get(connected.get(id)).send("-" + id);
            connected.remove(id);
        }
    }
}
