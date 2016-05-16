package graviton.game.job.actions.type;

import graviton.game.action.player.ActionManager;
import graviton.game.client.player.Player;
import graviton.game.job.Job;
import graviton.game.job.actions.JobAction;
import graviton.game.maps.object.InteractiveObject;
import graviton.game.object.ObjectTemplate;

import java.util.Random;

/**
 * Created by Botan on 12/06/2016.
 */
public class Harvest extends JobAction {
    private short time;
    protected byte[] differentialGains;

    public Harvest(short id, byte level, byte minimum, byte gain, Job job) {
        super(id, gain, job);
        this.time = (short) (12000 - (level * 100));
        this.differentialGains = new byte[]{(byte) (minimum + level / 5), (byte) ((minimum + 1) + (level / 5))};
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder(",");
        builder.append(super.getId()).append("~").append(this.differentialGains[0]).append("~");
        builder.append(this.differentialGains[1]).append("~0~").append(this.time);
        return builder.toString();
    }

    @Override
    public void start(Player player, int gameAction, InteractiveObject interactiveObject) {
        player.setActionState(ActionManager.Status.WORKING);
        player.getActionManager().setCurrentJobAction(this);
        interactiveObject.setInteractive(false);
        interactiveObject.setState(InteractiveObject.State.EMPTYING);
        player.getMap().send("GA" + gameAction + ";501;" + player.getId() + ";" + interactiveObject.getCell().getId() + "," + time);
        player.getMap().sendGdf(interactiveObject);
    }

    @Override
    public void stop(Player player, InteractiveObject interactiveObject) {
        player.getActionManager().setCurrentJobAction(null);
        player.setActionState(ActionManager.Status.WAITING);
        interactiveObject.setState(InteractiveObject.State.EMPTY);
        player.getMap().sendGdf(interactiveObject);
        job.addExperience(gain);
        byte quantity = (byte) (new Random().nextInt(differentialGains[1] - differentialGains[0] + 1) + differentialGains[0]);
        player.addObject(player.getGameManager().getObjectTemplate(player.getGameManager().getJobFactory().getObjectByAction(this.id)).createObject(quantity,false),true);
        player.send("IQ" + player.getId() + "|" + quantity);

        //TODO protector (FIGHT)
    }

}
