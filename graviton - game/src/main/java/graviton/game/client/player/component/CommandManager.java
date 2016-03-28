package graviton.game.client.player.component;

import com.google.inject.Inject;
import graviton.api.Manager;
import graviton.game.GameManager;
import graviton.game.alignement.Alignement;
import graviton.game.client.player.Player;
import graviton.game.common.Command;
import graviton.game.object.ObjectTemplate;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;


/**
 * Created by Botan on 18/10/2015 [Game]
 */
public class CommandManager implements Manager {
    private final ReentrantLock locker;
    @Inject
    GameManager gameManager;
    private Map<String, Command> playerCommands;
    private Map<String, Command> adminCommand;

    public CommandManager() {
        this.locker = new ReentrantLock();
    }

    public void launchCommand(Player player, String[] arguments) {
        try {
            locker.lock();
            this.playerCommands.get(arguments[0].toLowerCase()).perform(player, arguments);
        } catch (Exception e) {
            if (this.playerCommands.get(arguments[0].toLowerCase()) != null)
                this.playerCommands.get(arguments[0].toLowerCase()).perform(player, null);
            else
                player.sendText("La commande <b>" + arguments[0] + "</b> n'existe pas", "FF0000");
        } finally {
            locker.unlock();
        }
    }

    public void launchAdminCommand(Player player, String[] arguments) {
        try {
            locker.lock();
            this.adminCommand.get(arguments[0].toLowerCase()).perform(player, arguments);
        } catch (Exception e) {
            if (this.adminCommand.get(arguments[0].toLowerCase()) != null)
                this.adminCommand.get(arguments[0].toLowerCase()).perform(player, null);
            else
                player.send("BAT1La commande <b>" + arguments[0] + "</b> n'existe pas");
        } finally {
            locker.unlock();
    }
    }

    @Override
    public void load() {
        Map<String, Command> playerCommands = new HashMap<>();
        Map<String, Command> adminCommand = new HashMap<>();

        playerCommands.put("guilde", (player, arguments) -> player.send("gn"));
        playerCommands.put("ange", (player, arguments) -> player.getAlignement().setType(Alignement.Type.BONTARIEN));
        playerCommands.put("grade10", (player, arguments) -> player.getAlignement().setHonor(17500));
        playerCommands.put("send", (player, arguments) -> player.send(arguments[1]));

        this.playerCommands = Collections.unmodifiableMap(playerCommands);

        adminCommand.put("teleport", (player, arguments) -> {
            try {
                player.changePosition(Integer.parseInt(arguments[1]), Integer.parseInt(arguments[2]));
            } catch (Exception e) {
                player.send("BAT1La map est inconnue");
            }
        });

        adminCommand.put("item", (player, arguments) -> {
            try {
                int quantity = 1;
                ObjectTemplate template = gameManager.getObjectTemplate(Integer.parseInt(arguments[1]));
                try {
                    quantity = Integer.parseInt(arguments[2]);
                }catch (Exception e) {}
                player.addObject(template.createObject(quantity, true),true);
                player.send("BAT2L'objet " + template.getName() + " à bien ete cree " + quantity + " fois");
            } catch (Exception e) {
                player.send("BAT1L'objet n'existe pas");
            }
        });
        this.adminCommand = Collections.unmodifiableMap(adminCommand);
    }

    @Override
    public void unload() {
        this.playerCommands.clear();
        this.adminCommand.clear();
    }
}
