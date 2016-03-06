package graviton.game.creature.npc;

import graviton.game.client.player.Player;
import graviton.game.common.Action;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Botan on 09/02/2016.
 */
public class NpcAnswer {
    @Getter
    private int id;

    private List<Action> actions;

    public NpcAnswer(int id) {
        this.id = id;
        this.actions = new ArrayList<>();
    }

    public void addAction(Action action) {
        this.actions.add(action);
    }

    public void apply(Player player) {
        actions.forEach(action1 -> action1.apply(player));
    }

}
