package graviton.game.job.actions;

import graviton.game.client.player.Player;
import graviton.game.job.Job;
import graviton.game.maps.object.InteractiveObject;
import lombok.Data;

import java.util.List;

/**
 * Created by Botan on 07/06/2016.
 */
@Data
public abstract class JobAction {
    protected short id;
    protected byte gain;
    protected Job job;

    public JobAction(short id, byte gain, Job job) {
        this.id = id;
        this.gain = gain;
        this.job = job;
    }

    public abstract String toString();

    public abstract void start(Player player, int gameAction, InteractiveObject interactiveObject);

    public abstract void stop(Player player, InteractiveObject interactiveObject);

    public interface Getter {
        List<JobAction> get(Job job);
    }

}

