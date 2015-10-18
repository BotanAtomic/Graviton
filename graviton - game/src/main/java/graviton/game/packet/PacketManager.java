package graviton.game.packet;

import graviton.api.Manager;
import graviton.api.PacketParser;
import graviton.common.Utils;
import graviton.core.Main;
import graviton.database.DatabaseManager;
import graviton.game.GameManager;
import graviton.game.client.player.Player;
import graviton.game.maps.Maps;
import graviton.network.game.GameClient;
import lombok.extern.slf4j.Slf4j;

import java.util.*;


/**
 * Created by Botan on 20/06/2015.
 */
@Slf4j
public class PacketManager implements Manager {
    private final String[] dictionary = {"ae", "au", "ao", "ap", "ka", "ha", "ah",
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
            "osa", "panda", "xel", "rox", "stuff", "spoon", "days", "mouarf",
            "beau", "sexe", "koli", "master", "pro", "puissant"};

    private final String[] forbiden = {"admin", "modo", "mj", "-"};
    private final Calendar calendar;

    private Map<String, PacketParser> packets;

    public PacketManager() {
        this.packets = new HashMap<>();
        this.calendar = GregorianCalendar.getInstance();
    }

    @Override
    public void start() {
        packets.put("FA", (client, packet) -> client.getAccount().addFriend(client.getCurrentPlayer().getGameManager().getPlayer(packet)));

        packets.put("Ba", (client, packet) -> {
            if (client.getAccount().getRank().id < 1)
                return;
            String[] arguments = packet.substring(1).trim().split(",");
            Maps map = client.getCurrentPlayer().getData().loadMapByPosition(Integer.parseInt(arguments[0]), Integer.parseInt(arguments[1]));
            if (map != null)
                client.getCurrentPlayer().changePosition(map.getRandomCell());
        });

        packets.put("GP", (client, packet) -> client.getCurrentPlayer().togglePvp(packet.charAt(0)));

        packets.put("WU", (client, packet) -> client.getCurrentPlayer().useZaap(Integer.parseInt(packet)));

        packets.put("AB", (client, packet) -> client.getCurrentPlayer().boostStatistics(Integer.parseInt(packet)));

        packets.put("GC", (client, packet) -> client.getCurrentPlayer().createGame());

        packets.put("SB", (client, packet) -> {
            if (!client.getCurrentPlayer().boostSpell(Integer.parseInt(packet)))
                client.send("SUE");
        });

        packets.put("SM", (client, packet) -> {
            String[] arguments = packet.split("\\|");
            Player player = client.getCurrentPlayer();
            int id = Integer.parseInt(arguments[0]);
            if (player.getSpells().containsKey(id))
                player.moveSpell(id, Utils.HASH[Integer.parseInt(arguments[1])]);
            client.send("BN");
        });

        packets.put("PI", (client, packet) -> {
            Player target = client.getCurrentPlayer().getGameManager().getPlayer(packet);

            if (target == null) {
                client.send("PIEn" + packet);
                return;
            }

            if (target.getGroup() != null) {
                client.send("PIEa" + packet);
                return;
            }
            String finalPacket = "PIK" + client.getCurrentPlayer().getName() + "|" + target.getName();
            client.getCurrentPlayer().setInviting(target.getId());
            target.setInviting(client.getCurrentPlayer().getId());
            client.send(finalPacket);
            target.send(finalPacket);
        });

        packets.put("GA", (client, packet) -> client.getCurrentPlayer().createAction(Integer.parseInt(packet.substring(0, 3)), packet.substring(3)));

        packets.put("GK", (client, packet) -> {
            if (client.getCurrentPlayer().getActionManager() == null || client.getCurrentPlayer().getActionManager().getCurrentActions().isEmpty())
                return;
            int gameActionId;
            String[] infos = packet.substring(1).split("\\|");
            try {
                gameActionId = Integer.parseInt(infos[0]);
                client.getCurrentPlayer().getActionManager().endAction(gameActionId, packet.charAt(0) == 'K', infos.length > 1 ? infos[1] : "");
            } catch (Exception e) {
                log.error("Probleme at packet {} : {}", packet, e);
            }
        });

        packets.put("eD", (client, packet) -> client.getCurrentPlayer().changeOrientation(Integer.parseInt(packet), true));

        packets.put("eU", (client, packet) -> client.getCurrentPlayer().getMap().send("eUK" + client.getCurrentPlayer().getId() + "|" + packet));

        packets.put("BS", (client, packet) -> client.getCurrentPlayer().getMap().send("cS" + client.getCurrentPlayer().getId() + "|" + packet));

        packets.put("BM", (client, packet) -> client.getCurrentPlayer().speak(packet.substring(1, packet.length() - 1), packet.substring(0, 1)));

        packets.put("AT", (client, packet) -> {
            client.setAccount(Main.getInstance(GameManager.class).getAccounts().get(Integer.parseInt(packet)));
            if (client.getAccount() != null) {
                client.getAccount().setClient(client);
                client.getAccount().setIpAdress(client.getSession().getLocalAddress().toString().replace("/", "").split(":")[0]);
                client.send("ATK0");
            } else {
                client.send("ATE");
            }
        });

        packets.put("AS", (client, packet) -> {
            try {
                client.getAccount().getPlayer(Integer.parseInt(packet)).joinGame();
            } catch (NullPointerException e) {
                client.send("ASE");
            }
        });

        packets.put("AD", (client, packet) -> {
            if (client.getAccount().getAnswer().equals(packet.substring(2))) {
                client.getAccount().getPlayer(Integer.parseInt(packet.substring(0, 1))).delete();
                return;
            }
            client.send("ADE");
        });

        packets.put("AA", (client, packet) -> {
            DatabaseManager data = Main.getInstance(DatabaseManager.class);
            String[] arguments = packet.split("\\|");
            if (data.ifPlayerExist(arguments[0]) || arguments[0].length() < 4 || arguments[0].length() > 12) {
                client.send("AAEa");
                return;
            }
            for (String forbidenWord : forbiden)
                if (arguments[0].toLowerCase().contains(forbidenWord)) {
                    client.send("AAEa");
                    return;
                }
            int[] colors = {Integer.parseInt(arguments[3]), Integer.parseInt(arguments[4]), Integer.parseInt(arguments[5])};
            client.getAccount().createPlayer(arguments[0], (byte) Integer.parseInt(arguments[1]), (byte) Integer.parseInt(arguments[2]), colors);
        });
    }

    public final void parse(GameClient client, String packet) {
        if (packet.length() == 2) {
            switch (packet) {
                case "AL":
                    client.send(client.getAccount().getPlayersPacket());
                    break;
                case "AV":
                    client.send("AV0");
                    break;
                case "AP":
                    String pseudo = dictionary[(int) (Math.random() * dictionary.length - 1)] + dictionary[(int) (Math.random() * dictionary.length - 1)];
                    while (pseudo.length() < 4)
                        pseudo = dictionary[(int) (Math.random() * dictionary.length - 1)] + dictionary[(int) (Math.random() * dictionary.length - 1)];
                    client.send("AP" + pseudo);
                    break;
                case "BD":
                    client.send("BD" + calendar.get(Calendar.YEAR) + "|" + calendar.get(Calendar.MONTH) + "|" + calendar.get(Calendar.DAY_OF_MONTH));
                    client.send("BT" + (new Date().getTime() + 3600000));
                    break;
                case "Af":
                    client.send("Af1|1|1|1|1");
                    break;
                case "GI":
                    client.send(client.getCurrentPlayer().getMap().getGMs());
                    client.getCurrentPlayer().getMap().sendGdf(client.getCurrentPlayer());
                    client.send("GDK");
                    break;
                case "WV":
                    client.send("WV");
                    break;
                case "PR":
                    if (client.getCurrentPlayer() == null)
                        break;
                    if (client.getCurrentPlayer().getInviting() == 0)
                        break;
                    client.send("BN");
                    Player player = client.getCurrentPlayer().getGameManager().getPlayers().get(client.getCurrentPlayer().getInviting());
                    assert player != null;
                    player.send("PR");
                    player.setInviting(0);
                    client.getCurrentPlayer().setInviting(0);
                    break;
                default:
                    log.error("Unknown packet {}", packet);
            }
            return;
        }
        try {
            packets.get(packet.substring(0, 2)).parse(client, packet.substring(2));
        } catch (NullPointerException e) {
            log.error("Unknown packet {}", packet);
        }
    }

    @Override
    public void stop() {
        packets.clear();
    }
}
