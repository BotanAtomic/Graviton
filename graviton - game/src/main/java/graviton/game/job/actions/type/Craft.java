package graviton.game.job.actions.type;

import graviton.game.client.player.Player;
import graviton.game.exchange.job.BreakerExchange;
import graviton.game.job.Job;
import graviton.game.job.actions.JobAction;
import graviton.game.job.utils.CraftData;
import graviton.game.maps.object.InteractiveObject;
import lombok.Data;

import java.util.Map;

/**
 * Created by Botan on 12/06/2016.
 */
@Data
public class Craft extends JobAction {
    private byte chance;
    private byte maxCase;

    private InteractiveObject interactiveObject;

    public Craft(short id, byte gain, byte chance, byte maxCase, Job job) {
        super(id, gain, job);
        this.maxCase = maxCase;
        this.chance = chance;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder(",");
        builder.append(super.getId()).append("~").append(this.maxCase).append("~");
        builder.append("0~0~").append(this.chance);
        return builder.toString();
    }

    @Override
    public void start(Player player, int gameAction, InteractiveObject interactiveObject) {
        this.interactiveObject = interactiveObject;
        interactiveObject.setState(InteractiveObject.State.EMPTYING);
        player.getActionManager().setCurrentJobAction(this);
        player.send("ECK3|" + this.maxCase + ";" + this.id);
        player.getMap().sendGdf(interactiveObject);
        player.setExchange(new BreakerExchange(player, this));
    }

    @Override
    public void stop(Player player, InteractiveObject interactiveObject) {
        player.send("EV");
        player.getActionManager().resetActions();
        this.interactiveObject.setState(InteractiveObject.State.FULL);
        player.getMap().sendGdf(this.interactiveObject);
    }

    public CraftData get(Map<Short, Short> ingredients) {
        final CraftData[] craftData = {null};
        this.getJob().getTemplate().getCraft(id, maxCase).stream().filter(data -> data.check(ingredients)).forEach(craft -> craftData[0] = craft);
        return craftData[0];
    }


}
