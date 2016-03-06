package graviton.game.common;

import graviton.game.client.player.Player;
import graviton.game.client.player.component.ActionManager;
import graviton.game.client.player.exchange.TrunkExchange;
import graviton.game.creature.npc.NpcQuestion;
import graviton.game.enums.ActionType;
import graviton.game.maps.Maps;
import graviton.game.object.*;
import lombok.Data;

/**
 * Created by Botan on 22/06/2015.
 */
@Data
public class Action {
    private ActionType action;
    private String arguments;

    public Action(int id, String arguments) {
        this.action = ActionType.getType(id);
        this.arguments = arguments;
    }

    public void apply(Player player) {
        String[] split = arguments.split(",");
        switch (this.action) {

            case BANK: {
                int cost = player.getAccount().getBankPrice();
                if(player.getKamas() < cost) {
                    player.send("Im1128;" + cost);
                    return;
                }

                player.setKamas(player.getKamas() - cost);
                player.send("Im020;" + cost);
                player.send(player.getPacket("As"));

                player.send("DV");
                player.setExchange(new TrunkExchange(player,player.getAccount().getBank()));
                break;
            }

            case TELEPORT: {
                Maps map = player.getGameManager().getMap(Integer.parseInt(split[0]));
                if (map != null)
                    player.changePosition(map.getCell(Integer.parseInt(split[1])));
                if (player.getInviting() != 0)
                    player.quitDialog();
                break;
            }

            case DIALOG: {
                if (arguments.equalsIgnoreCase("DV")) {
                    player.send("DV");
                    player.setInviting(0);
                    player.getActionManager().setStatus(ActionManager.Status.WAITING);
                } else
                    player.createDialog(Integer.parseInt(arguments));
                return;
            }

            case DONJON: {
                int key = Integer.parseInt(arguments.split(",")[2]);

                graviton.game.object.Object object = player.getObjectByTemplate(key);

                if (object != null) {
                    player.removeObject(object.getId(), 1);
                    player.changePosition(Short.parseShort(arguments.split(",")[0]), Integer.parseInt(arguments.split(",")[1]));
                    player.send("Im022;1~" + key);
                } else {
                    player.send("Im00;Vous ne possedez pas la clef necessaire.");
                }

                player.quitDialog();
            }
        }
    }
}
