package graviton.network.login.packet;


import graviton.console.Console;
import graviton.database.Database;
import graviton.enums.LoginStatus;
import graviton.game.Account;
import graviton.game.Player;
import graviton.game.Server;
import graviton.login.Configuration;
import graviton.network.login.LoginClient;


/**
 * Created by Botan on 07/06/2015.
 */
public class PacketParser {
    public char[] hash = {'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l',
            'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', 'A',
            'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P',
            'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '0', '1', '2', '3', '4',
            '5', '6', '7', '8', '9', '-', '_'};
    private Console console;
    private Database database;

    public PacketParser(Configuration config, Console console) {
        this.console = console;
        this.database = config.getDatabase();
    }

    public void parse(String packet, LoginClient client) {
        console.println("[Session " + client.getId() + "] parse packet > " + packet + " < with status : " + client.getStatus().name(), false);
        switch (client.getStatus()) {
            case WAIT_CONNECTION:
                this.parseConnectionPacket(client, packet);
                break;
            case WAIT_NICKNAME:
                this.checkPseudo(client, packet);
                break;
            case SERVER:
                this.parseServerPacket(client, packet);
                break;
        }
    }

    public void parseConnectionPacket(LoginClient client, String packet) {
        console.println("[Session " + client.getId() + "] parse connection packet > " + packet, false);
        switch (packet.charAt(0)) {
            case '#':
                checkPassword(client, packet);
                break;
            default:
                checkAccount(client, packet);
        }
    }

    private void checkAccount(LoginClient client, String name) {
        console.println("[Session " + client.getId() + "] check account > " + name, false);
        try {
            final Account account = this.database.getAccountData().load(name.toLowerCase());
            client.setAccount(account);
        } catch (Exception e) {
            client.send("AlEf");
            client.kick();
            return;
        }
        if (client.getAccount() == null) {
            client.send("AlEf");
            client.kick();
            return;
        }

    }

    private void checkPassword(LoginClient client, String packet) {
        if (client.getAccount() == null) {
            console.println("[Session " + client.getId() + "] can't check password (Account = null)", false);
            return;
        }
        console.println("[Session " + client.getId() + "] checking password...", false);
        if (!cryptedPassword(client).equals(packet)) {
            client.send("AlEf");
            client.kick();
            return;
        }
        this.database.getAccountData().addAccount(client.getAccount());
        client.setStatus(LoginStatus.SERVER);
    }

    private String cryptedPassword(LoginClient client) {
        String pass = client.getAccount().getPassword();
        String key = client.getKey();
        int i = hash.length;
        StringBuilder crypted = new StringBuilder("#1");

        for (int y = 0; y < pass.length(); y++) {
            char c1 = pass.charAt(y);
            char c2 = key.charAt(y);
            double d = Math.floor(c1 / 16);
            int j = c1 % 16;
            crypted.append(hash[(int) ((d + c2 % i) % i)]).append(hash[(j + c2 % i) % i]);
        }
        return crypted.toString();
    }

    public void parseServerPacket(LoginClient client, String packet) {
        console.println("[Session " + client.getId() + "] check server packets", false);
        switch (packet.substring(0, 2)) {
            case "AF":
                this.sendFriendListPacket(client, packet);
                break;
            case "Af":
                this.sendInformations(client);
                break;
            case "AX":
                this.implementSelectedServerAction(client, packet.substring(2));
                break;
            case "Ax":
                client.send("AxK" + serverList(client.getAccount()));
                break;
        }
    }

    public void sendInformations(final LoginClient client) {
        Account account = client.getAccount();

        if (account.getPseudo() == null || account.getPseudo().isEmpty()) {
            client.send("AlEr");
            client.setStatus(LoginStatus.WAIT_NICKNAME);
            return;
        }

        this.database.getPlayerData().load(account);

        client.send("Af0|0|0|1|-1");
        client.send("Ad" + account.getPseudo());
        client.send("Ac0");
        client.send(this.database.getServerData().getHostList());
        client.send("AlK" + (account.getRank() != 0 ? 1 : 0));
        client.send("AQ" + account.getQuestion());
    }

    private String serverList(Account account) {
        StringBuilder sb = new StringBuilder("31556864852");
        for (Server server : this.database.getServerData().getServers().values()) {
            int i = 0;
            for (Player player : account.getPlayers()) {
                if (player.getServer() == server.getId())
                    i++;
            }
            sb.append("|").append(server.getId()).append(",").append(i != 0 ? i : 1);
        }
        return sb.toString();
    }

    private void sendFriendListPacket(LoginClient client, String packet) {
        try {
            String name = packet.substring(2);
            Account account = database.getAccountData().loadByPseudo(name);

            if (account == null) {
                client.send("AF");
                return;
            }
            database.getPlayerData().load(account);
            client.send("AF" + getList(account));
        } catch (Exception e) {
        }
    }

    public String getList(Account account) {
        StringBuilder sb = new StringBuilder();
        for (Server server : database.getServerData().getServers().values()) {
            int i = getNumber(account, server.getId());
            if (i != 0)
                sb.append(server.getId()).append(",").append(i).append(";");
        }
        return sb.toString();
    }

    public int getNumber(Account account, int id) {
        int i = 0;
        for (Player character : account.getPlayers())
            if (character.getServer() != id) continue;
            else i++;
        return i;
    }

    private void checkPseudo(LoginClient client, String pseudo) {
        Account account = client.getAccount();
        if (account.getPseudo().length() != 0) {
            client.kick();
            return;
        }
        if (pseudo.toLowerCase().equals(account.getName().toLowerCase())) {
            client.send("AlEr");
            return;
        }
        String forbidenWord[] = {"admin", "modo", " ", "&", "é", "\"", "'",
                "(", "-", "è", "_", "ç", "à", ")", "=", "~", "#",
                "{", "[", "|", "`", "^", "@", "]", "}", "°", "+",
                "^", "$", "ù", "*", ",", ";", ":", "!", "<", ">",
                "¨", "£", "%", "µ", "?", "", "/", "§", "\n"};

        for (String word : forbidenWord) {
            if (pseudo.contains(word)) {
                client.send("AlEs");
                break;
            }
        }
        if (this.database.getAccountData().alreadyExist(pseudo)) {
            client.send("AlEs");
            return;
        }

        if (pseudo.length() > 10 || pseudo.length() < 4) {
            client.send("AlEs");
            return;
        }
        client.getAccount().setPseudo(pseudo);
        this.database.getAccountData().update(client.getAccount());
        client.setStatus(LoginStatus.SERVER);
        sendInformations(client);
    }

    private void implementSelectedServerAction(LoginClient client, String packet) {
        Server server;
        try {
            int i = Integer.parseInt(packet);
            server = database.getServerData().getServers().get(i);
        } catch (Exception e) {
            client.send("AXEr");
            client.kick();
            return;
        }

        if (server == null) {
            client.send("AXEr");
            client.kick();
            return;
        }

        if (server.getState() != 1) {
            client.send("AXEd");
            client.kick();
            return;
        }
        Account account = client.getAccount();
        server.send("WA" + account.getId());
        StringBuilder sb = new StringBuilder();
        String ip = client.getSession().getLocalAddress().toString().replace("/", "").split(":")[0];
        sb.append("AYK")
                .append((ip.equals("127.0.0.1") ? "127.0.0.1" : server.getIp()))
                .append(":").append(server.getPort()).append(";")
                .append(account.getId());
        client.send(sb.toString());
    }
}


