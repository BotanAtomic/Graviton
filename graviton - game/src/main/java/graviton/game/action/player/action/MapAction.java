package graviton.game.action.player.action;

import graviton.api.Action;
import graviton.game.client.player.Player;
import graviton.game.action.player.ActionManager;
import graviton.game.maps.Cell;

/**
 * Created by Botan on 10/10/2015.
 */
public class MapAction implements Action {
    private final int id;
    private final Player player;
    private final String argument;
    private int action;
    private Cell cell;

    public MapAction(int id, Player player, String argument) {
        this.id = id;
        this.player = player;
        this.argument = argument;
    }

    @Override
    public boolean start() {
        cell = player.getMap().getCell(Integer.parseInt(argument.split(";")[0]));
        action = Integer.parseInt(argument.split(";")[1]);
        player.getMap().getCell(Integer.parseInt(argument.split(";")[0])).startAction(player, id, action);
        return true;
    }

    @Override
    public void cancel() {
        player.setActionState(ActionManager.Status.WAITING);
    }

    @Override
    public void onFail(String args) {
        player.setActionState(ActionManager.Status.WAITING);
    }

    @Override
    public void onSuccess(String args) {
        cell.finishAction(player, action);
    }

    @Override
    public int getId() {
        return 0;
    }

    @Override
    public int getAction() {
        return 500;
    }
}
