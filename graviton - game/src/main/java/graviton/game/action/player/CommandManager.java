package graviton.game.action.player;

import com.google.inject.Inject;
import graviton.api.Manager;
import graviton.enums.DataType;
import graviton.game.GameManager;
import graviton.game.client.player.Player;
import graviton.game.common.Command;
import graviton.game.object.ObjectTemplate;
import graviton.game.object.panoply.PanoplyTemplate;

import java.util.*;


/**
 * Created by Botan on 18/10/2015 [Game]
 */
public class CommandManager implements Manager { //TODO : create differents class for command
    @Inject
    GameManager gameManager;

    private Map<String, Command> playerCommands;
    private Map<String, Command> adminCommand;

    public CommandManager() {
        load();
    }

    public void launchCommand(Player player, String[] arguments) {
        try {
            this.playerCommands.get(arguments[0].toLowerCase()).perform(player, arguments);
        } catch (Exception e) {
            if (this.playerCommands.get(arguments[0].toLowerCase()) != null)
                this.playerCommands.get(arguments[0].toLowerCase()).perform(player, null);
            else
                player.sendText("La commande <b>" + arguments[0] + "</b> n'existe pas", "FF0000");
        }
    }

    public void launchAdminCommand(Player player, String[] arguments) {
        try {
            this.adminCommand.get(arguments[0].toLowerCase()).perform(player, arguments);
        } catch (Exception e) {
            if (this.adminCommand.get(arguments[0].toLowerCase()) != null)
                this.adminCommand.get(arguments[0].toLowerCase()).perform(player, null);
            else
                player.send("BAT1La commande <b>" + arguments[0] + "</b> n'existe pas");
        }
    }

    @Override
    public void load() {
        Map<String, Command> playerCommands = new HashMap<>();
        Map<String, Command> adminCommand = new HashMap<>();

        playerCommands.put("send", (player, arguments) -> player.send(arguments[1]));

        this.playerCommands = Collections.unmodifiableMap(playerCommands);

        adminCommand.put("teleport", (player, arguments) -> {
            int cell = 0;
            int maps = Integer.parseInt(arguments[1]);
            try {
                cell = Integer.parseInt(arguments[2]);
            } catch (Exception e) {
                cell = (gameManager.getMap(maps)).getRandomCell().getId();
            }
            player.changePosition(maps, cell);
        });

        adminCommand.put("item", (player, arguments) -> {
            try {
                int quantity = 1;
                ObjectTemplate template = gameManager.getObjectTemplate(Integer.parseInt(arguments[1]));
                try {
                    quantity = Integer.parseInt(arguments[2]);
                } catch (Exception e) {
                }
                player.addObject(template.createObject(quantity, true), true);
                player.send("BAT2L'objet " + template.getName() + " à bien ete ajouté à l'inventaire " + quantity + " fois");
            } catch (Exception e) {
                player.send("BAT1L'objet n'existe pas");
            }
        });

        adminCommand.put("panoply", (player, arguments) -> {
            try {
                PanoplyTemplate panoply = gameManager.getObjectFactory().getPanoply(Integer.parseInt(arguments[1]));

                for (Integer object : panoply.getItems()) {
                    ObjectTemplate template = gameManager.getObjectFactory().get(object);
                    player.addObject(template.createObject(1, true), true);
                }

                player.send("BAT2La panoplie " + panoply.getName() + " à bien été ajoutée à l'inventaire");
            } catch (Exception e) {
                player.send("BAT1La panoplie n'existe pas");
            }
        });

        adminCommand.put("stats", (player, arguments) -> {
            long totalMemory = Runtime.getRuntime().totalMemory() / 1024 / 1024;
            long freeMemory = Runtime.getRuntime().freeMemory() / 1024 / 1024;


            StringBuilder builder = new StringBuilder("\n\n<u>Statistiques du serveur</u>\n\n");

            builder.append((gameManager.getElements(DataType.ACCOUNT).size()) + " compte(s) chargé(s)\n");
            builder.append((gameManager.getElements(DataType.PLAYER).size()) + " personnage(s) chargé(s)\n");
            builder.append((gameManager.getElements(DataType.MAPS).size()) + " carte(s) chargé(s)\n\n");

            builder.append("Mémoire maximale : " + totalMemory + " Mb\n");
            builder.append("Mémoire libre : " + freeMemory + " Mb\n");
            builder.append("Mémoire utilisée : " + (totalMemory - freeMemory) + " Mb\n\n");

            builder.append("<u>" + Thread.activeCount() + " threads actifs </u>\n");
            Thread.getAllStackTraces().keySet().forEach(thread -> builder.append(thread + " (".concat(thread.getState().name()).concat(")\n")));

            player.getAccount().getClient().send("BAT0".concat(builder.toString()));

        });

        this.adminCommand = Collections.unmodifiableMap(adminCommand);
    }

    @Override
    public void unload() {
        this.playerCommands.clear();
        this.adminCommand.clear();
    }
}
