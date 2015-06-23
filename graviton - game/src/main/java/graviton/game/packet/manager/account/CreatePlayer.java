package graviton.game.packet.manager.account;

import graviton.api.Packet;
import graviton.api.PacketParser;
import graviton.console.Console;
import graviton.core.Main;
import graviton.database.DatabaseManager;
import graviton.database.data.PlayerData;
import graviton.enums.DataType;
import graviton.network.game.GameClient;

/**
 * Created by Botan on 21/06/2015.
 */
@Packet("AA")
public class CreatePlayer implements PacketParser {

    @Override
    public void parse(GameClient client, String packet) {
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

    }

}
