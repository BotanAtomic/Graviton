package graviton.login;

import com.google.inject.Inject;
import graviton.api.Client;
import graviton.database.data.AccountData;
import graviton.database.data.PlayerData;
import graviton.database.data.ServerData;
import graviton.game.Account;
import graviton.game.Player;
import graviton.game.Server;
import graviton.network.NetworkManager;
import graviton.network.login.LoginClient;
import lombok.Data;
import org.apache.mina.core.session.IoSession;
import org.fusesource.jansi.AnsiConsole;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Botan on 06/07/2015.
 */
@Data
public class Login {
    @Inject
    private Configuration configuration;
    @Inject
    private NetworkManager manager;

    private Map<String, Map<Long, Client>> clients;
    private Map<String, graviton.api.Data> datas;
    private Map<Integer, Integer> connected;
    /**
     * ID -> Object
     **/
    private Map<Integer, Account> accounts;
    private Map<Integer, Player> players;
    private Map<Integer, Server> servers;

    public Login() {
        this.accounts = new ConcurrentHashMap<>();
        this.players = new ConcurrentHashMap<>();
        this.servers = new ConcurrentHashMap<>();
        this.clients = new ConcurrentHashMap<>();
        this.datas = new ConcurrentHashMap<>();
        this.connected = new ConcurrentHashMap<>();
    }

    public Login start() {
        datas.put("account", new AccountData());
        datas.put("player", new PlayerData());
        datas.put("server", new ServerData());

        clients.put("login", new ConcurrentHashMap<>());
        clients.put("exchange", new ConcurrentHashMap<>());

        manager.start();
        AnsiConsole.out.println("                 _____                     _  _                \n                / ____|                   (_)| |               \n               | |  __  _ __  __ _ __   __ _ | |_  ___   _ __  \n               | | |_ || '__|/ _` |\\ \\ / /| || __|/ _ \\ | '_ \\ \n               | |__| || |  | (_| | \\ V / | || |_| (_) || | | |\n                \\_____||_|   \\__,_|  \\_/  |_| \\__|\\___/ |_| |_|\n");
        AnsiConsole.out.append("\033]0;").append("Graviton - Login").append("\007");
        return this;
    }

    public void stop() {
        manager.stop();
        configuration.getDatabase().stop();
    }

    public void addClient(Client client) {
        clients.get(client instanceof LoginClient ? "login" : "exchange").put(client.getId(), client);
    }

    public void removeClient(Client client) {
        if (client instanceof LoginClient) {
            if (clients.get("login").get(client.getId()) != null)
                clients.get("login").remove(client.getId());
            return;
        }
        if (clients.get("exchange").get(client.getId()) != null)
            clients.get("exchange").remove(client.getId());
    }

    public Client getClient(IoSession session) {
        for (Client client : getAllClients())
            if (client.getSession() == session)
                return client;
        return null;
    }

    private List<Client> getAllClients() {
        List<Client> allClients = new ArrayList<>();
        allClients.addAll(clients.get("login").values());
        allClients.addAll(clients.get("exchange").values());
        return allClients;
    }

    public String getHostList() {
        ServerData data = (ServerData) getData("server");
        return data.getHostList();
    }

    public graviton.api.Data getData(String name) {
        return datas.get(name);
    }
}
