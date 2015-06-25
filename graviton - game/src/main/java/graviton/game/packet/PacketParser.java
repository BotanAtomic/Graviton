package graviton.game.packet;

import graviton.console.Console;
import graviton.core.Main;
import graviton.database.DatabaseManager;
import graviton.database.data.PlayerData;
import graviton.enums.DataType;
import graviton.network.game.GameClient;
import graviton.network.game.GameNetworkService;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by Botan on 23/06/2015.
 */
public class PacketParser { // Temporary class
    public static void parse(GameClient client, String startPacket) {
        String packet = startPacket.substring(2);
        switch (startPacket.substring(0,2)) {

            case "AA" :
                final Console console = Main.getInstance(Console.class);
                final PlayerData data = (PlayerData) Main.getInstance(DatabaseManager.class).getData().get(DataType.PLAYER);

                String[] forbiden = {"admin", "modo", "mj", "-"};
                String[] arguments = packet.split("\\|");

                if (data.exist(arguments[0])) {
                    client.send("AAEa");
                    return;
                }

                if (arguments[0].length() < 4 || arguments[0].length() > 12) {
                    client.send("AAEa");
                    return;
                }

                for (String forbidenWord : forbiden)
                    if ((arguments[0].toLowerCase()).contains(forbidenWord)) {
                        client.send("AAEa");
                        return;
                    }

                int[] colors = {Integer.parseInt(arguments[3]), Integer.parseInt(arguments[4]), Integer.parseInt(arguments[5])};
                if (!client.getAccount().createPlayer(arguments[0], (byte) Integer.parseInt(arguments[1]), (byte) Integer.parseInt(arguments[2]), colors))
                    client.send("AAEF");
                break;
            case "AP" :
                String[] dictionary = {"ae", "au", "ao", "ap", "ka", "ha", "ah",
                        "na", "hi", "he", "eh", "an", "ma", "wa", "we", "wh", "sk", "sa",
                        "se", "ne", "ra", "re", "ru", "ri", "ro", "za", "zu", "ta", "te",
                        "ty", "tu", "ti", "to", "pa", "pe", "py", "pu", "pi", "po", "da",
                        "de", "du", "di", "do", "fa", "fe", "fu", "fi", "fo", "ga", "gu",
                        "ja", "je", "ju", "ji", "jo", "la", "le", "lu", "ma", "me", "mu",
                        "mo", "radio", "kill", "explode", "craft", "fight", "shadow",
                        "bouftou", "bouf", "piou", "piaf", "champ", "abra", "grobe",
                        "krala", "sasa", "nianne", "miaou", "was", "killed", "born",
                        "storm", "lier", "arm", "hand", "mind", "create", "random", "nick",
                        "error", "end", "life", "die", "cut", "make", "spawn", "respawn",
                        "zaap", "zaapis", "mobs", "google", "firefox", "rapta", "ewplorer",
                        "men", "women", "dark", "eau", "get", "set", "geek", "nolife",
                        "spell", "boost", "gift", "leave", "smiley", "blood", "jean",
                        "yes", "eays", "skha", "rock", "stone", "fefe", "sadi", "sacri",
                        "osa", "panda", "xel", "rox", "stuff", "spoon", "days", "mouarf", "beau", "sexe"};

                client.send("AP" + dictionary[(int) (Math.random() * dictionary.length - 1)] + dictionary[(int) (Math.random() * dictionary.length - 1)]);
                break;
            case "AL" :
                client.send(client.getAccount().getPlayersList());
                break;
            case "AV":
                client.send("BN");
                client.send("AV0");
                break;
            case "BD" :
                Calendar calendar = Main.getCalendar();
                Date actDate = new Date();
                client.send("BD" + calendar.get(Calendar.YEAR) + "|"+calendar.get(Calendar.MONTH)+"|"+calendar.get(Calendar.DAY_OF_MONTH));
                client.send("BT" + (actDate.getTime() + 3600000));
                break;
            case "GI" :
                client.send("GDK");
                break;
            case "AT" :
                final GameNetworkService service = Main.getInstance(GameNetworkService.class);
                client.setAccount(service.getAccount(Integer.parseInt(packet)));

                if (client.getAccount() != null) {
                    String ip = client.getSession().getLocalAddress().toString().replace("/", "").split(":")[0];
                    client.getAccount().setClient(client);
                    client.getAccount().loadPlayers();
                    client.getAccount().setIpAdress(ip);
                    service.removeAccount(client.getAccount());
                    client.send("ATK0");
                } else {
                    client.send("ATE");
                }
                break;
            case "AS" :
                int id = Integer.parseInt(packet);
                if (client.getAccount().getPlayer(id) != null) {
                    client.getAccount().setCurrentPlayer(client.getAccount().getPlayer(id));
                    if (client.getPlayer() != null) {
                        client.getPlayer().joinGame();
                        return;
                    }
                }
                client.send("ASE");
                break;
            case "Af":
                client.send("BN");
                client.send("Af" + (1) + ("|") + (1) + ("|") + (1) + ("|") + (1) + ("|") + (1));
                break;
            case "GC" :
                client.getPlayer().createGame();
                break;
        }
    }
}
