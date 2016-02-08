package graviton.game.client.player.component;

import graviton.api.Manager;
import graviton.game.client.player.Player;
import graviton.game.common.Command;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;


/**
 * Created by Botan on 18/10/2015 [Game]
 */
public class CommandManager implements Manager {

    private final Map<String, Command> commands;

    private final ReentrantLock locker;

    public CommandManager() {
        this.commands = new ConcurrentHashMap<>();
        this.locker = new ReentrantLock();
    }

    public void launchCommand(Player player, String[] arguments) {
        try {
            locker.lock();
            this.commands.get(arguments[0].toLowerCase()).perform(player, arguments[1]);
        } catch (Exception e) {
            if (this.commands.get(arguments[0].toLowerCase()) != null)
                this.commands.get(arguments[0].toLowerCase()).perform(player, "");
            else
                player.sendText("La commande <b>" + arguments[0] + "</b> n'existe pas", "FF0000");
        } finally {
            locker.unlock();
        }
    }

    @Override
    public void load() {
        this.commands.put("guilde", (player, arguments) -> player.send("gn"));
    }

    @Override
    public void unload() {
        this.commands.clear();
    }
}
