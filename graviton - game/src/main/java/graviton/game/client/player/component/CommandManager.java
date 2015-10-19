package graviton.game.client.player.component;

import graviton.api.Manager;
import graviton.game.client.player.Player;
import graviton.game.common.Command;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Botan on 18/10/2015 [Game]
 */
public class CommandManager implements Manager {
    private final Map<String, Command> commands;

    public CommandManager() {
        this.commands = new HashMap<>();
    }

    public void launchCommand(Player player, String[] arguments) {
        try {
            this.commands.get(arguments[0].toLowerCase()).perform(player, arguments[1]);
        } catch (Exception e) {
            if (this.commands.get(arguments[0].toLowerCase()) != null) {
                this.commands.get(arguments[0].toLowerCase()).perform(player, "");
                return;
            }
            player.sendText("La commande <b>" + arguments[0] + "</b> n'existe pas", "FF0000");
        }
    }

    @Override
    public void start() {
        this.commands.put("exemple", (player, arguments) -> player.changeOrientation(player.getOrientation() + 1, true));
    }

    @Override
    public void stop() {

    }
}
