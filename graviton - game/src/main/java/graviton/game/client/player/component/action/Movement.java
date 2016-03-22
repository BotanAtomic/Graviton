package graviton.game.client.player.component.action;

import graviton.api.Action;
import graviton.common.Utils;
import graviton.game.client.player.Player;
import graviton.game.client.player.component.ActionManager;
import graviton.game.common.Pathfinding;
import graviton.game.maps.Cell;
import lombok.Getter;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by Botan on 04/10/2015.
 */
public class Movement extends Pathfinding implements Action {
    @Getter
    private final int id, action = 1;
    private final Player player;
    private String arguments;
    private String finalPathfinding;
    private String initialPathfinding;

    public Movement(int id, Player player, String arguments) {
        this.id = id;
        this.player = player;
        this.arguments = arguments;
    }

    @Override
    public boolean start() {
        this.finalPathfinding = this.initialPathfinding = arguments;
        if (player.getActionManager().getStatus() != ActionManager.Status.WAITING) {
            player.send("GA;0");
            return false;
        }
        AtomicReference<String> pathRef = new AtomicReference<>(arguments);
        int result = isValidPathfinding(player.getMap(), player.getCell().getId(), pathRef);
        if (result == 0) {
            player.send("GA;0");
            return false;
        }
        arguments = result == -1000 ? Utils.getHashedValueByInteger(player.getOrientation()) + cellToCode(player.getCell().getId()) : pathRef.get();
        player.getMap().send("GA" + id + ";" + action + ";" + player.getId() + ";" + "a" + cellToCode(player.getCell().getId()) + arguments);
        this.finalPathfinding = arguments;
        player.getActionManager().setStatus(ActionManager.Status.MOVING);
        return true;
    }

    @Override
    public void cancel() {

    }

    @Override
    public void onFail(String args) {
        int newCellID = Integer.parseInt(args);
        player.getMap().getCell(newCellID).addCreature(player);
        player.changeOrientation(getFinalOrientation(initialPathfinding), false);
        player.send("BN");
        player.getActionManager().setStatus(ActionManager.Status.WAITING);
    }

    @Override
    public void onSuccess(String args) {
        Cell newCell = player.getMap().getCell(getFinalCell(finalPathfinding));
        newCell.addCreature(player);
        player.changeOrientation(getFinalOrientation(finalPathfinding), false);
        player.getActionManager().setStatus(ActionManager.Status.WAITING);
        player.getCell().applyAction(player);
    }

}
