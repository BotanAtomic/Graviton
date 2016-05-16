package graviton.game.action.player;

import graviton.api.Action;
import graviton.game.client.player.Player;
import graviton.game.action.player.action.MapAction;
import graviton.game.action.player.action.Movement;
import graviton.game.job.actions.JobAction;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;

/**
 * Created by Botan on 09/10/2015.
 */
@Data
@Slf4j
public class ActionManager {
    private final ArrayList<Action> currentActions;
    private final Player player;
    private Status status;
    private boolean isAway;

    private JobAction currentJobAction;

    public ActionManager(Player player) {
        this.player = player;
        this.currentActions = new ArrayList<>();
        this.status = Status.WAITING;
        this.isAway = false;
    }

    public Status getStatus() {
        if (!isAway)
            return status;
        return status != Status.WAITING ? status : Status.AWAY;
    }

    public void createAction(int actionId, String args) {
        Action gameAction;
        int id = nextActionId();
        switch (actionId) {
            case 1:
                gameAction = new Movement(id, player, args);
                break;
            case 500:
                gameAction = new MapAction(id, player, args);
                break;
            case 900:
                player.askDefy(args);
                return;
            default:
                log.error("Can't find action {} for the player [{}]", actionId, player.getName());
                return;
        }
        addAction(gameAction);
    }

    private void startAction(Action gameActions) {
        if (!gameActions.start())
            currentActions.remove(gameActions);
    }

    public void endAction(int actionId, boolean success, String args) {
        Action gameAction = currentActions.get(actionId);
        if (gameAction != null) {
            if (success) {
                gameAction.onSuccess(args);
                if (currentActions.size() > currentActions.indexOf(gameAction) + 1)
                    startAction(currentActions.get(currentActions.indexOf(gameAction) + 1));
                if (currentActions.contains(gameAction)) currentActions.remove(gameAction);
            } else {
                gameAction.onFail(args);
                resetActions();
            }
        }
    }

    public void resetActions() {
        currentActions.forEach(Action::cancel);
        currentActions.clear();
        setStatus(Status.WAITING);
        this.currentJobAction = null;
    }

    private int nextActionId() {
        for (Action action : currentActions)
            if (action.getId() > 0)
                return action.getId() + 1;
        return 0;
    }

    private void addAction(Action gameAction) {
        currentActions.add(gameAction);
        if (currentActions.size() == 1)
            startAction(gameAction);
    }

    public enum Status {
        WAITING,
        MOVING,
        ATTACKING,
        DEFYING,
        ANIMATION,
        EXCHANGING,
        AWAY,
        GHOST,
        WORKING,
        DIALOG
    }
}
