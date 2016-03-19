package graviton.login;

import com.google.inject.Inject;
import com.google.inject.Injector;
import graviton.api.Client;
import graviton.common.Scanner;
import graviton.database.Database;
import graviton.game.Account;
import graviton.game.Player;
import graviton.game.Server;
import graviton.network.NetworkManager;
import graviton.network.exchange.ExchangeClient;
import graviton.network.login.LoginClient;
import lombok.Data;

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
    NetworkManager networkManager;
    @Inject
    Scanner scanner;
    @Inject
    Database database;

    private Map<Long, Client> clients;
    private Map<Integer, Integer> connected;

    private Map<Integer, Account> accounts;
    private Map<Integer, Player> players;
    private Map<Integer, Server> servers;

    private Date dateOfStart;

    public Manager() {
        this.accounts = new ConcurrentHashMap<>();
        this.players = new ConcurrentHashMap<>();
        this.clients = new ConcurrentHashMap<>();
        this.connected = new ConcurrentHashMap<>();
        this.dateOfStart = new java.util.Date();
    }

    public Manager start() {
        networkManager.start();
        scanner.start(this);
        database.loadServers();
        return this;
    }

    public void stop() {
        scanner.interrupt();
        networkManager.stop();
        database.stop();
    }

    public void addClient(Client client) {
        clients.put(client.getId(), client);
    }

    public void removeClient(Client client) {
        if (clients.containsValue(client))
            clients.remove(client.getId());
    }

    public Client getClient(int id) {
        for (Client client : clients.values())
            if (client.getSession().getId() == id)
                return client;
        return null;
    }

    public String getHostList() {
        return database.getHostList();
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
