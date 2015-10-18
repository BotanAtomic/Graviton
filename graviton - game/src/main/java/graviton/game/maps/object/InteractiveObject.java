package graviton.game.maps.object;

import graviton.core.Main;
import graviton.game.GameManager;
import graviton.game.maps.Cell;
import graviton.game.maps.Maps;
import lombok.Data;
import lombok.Getter;

import java.util.concurrent.TimeUnit;

/**
 * Created by Botan on 10/10/2015.
 */
@Data
public class InteractiveObject {
    private final GameManager gameManager = Main.getInstance(GameManager.class);
    private final int id;
    private final Maps map;
    private final Cell cell;

    private State state;
    private boolean interactive = true;
    private InteractiveObjectTemplate template;
    @Getter
    private boolean walkable;

    public InteractiveObject(int id, final Maps map, final Cell cell) {
        this.id = id;
        this.map = map;
        this.cell = cell;
        this.state = State.FULL;
        this.template = gameManager.getInteractiveObjectTemplates().get(id);
        this.walkable = this.template != null && (this.template.isWalkable() || this.state == State.EMPTY);
        if (template == null) return;
        if (template.getRespawnTime() == -1) return;
        map.getSheduler().scheduleAtFixedRate(() -> {
            if (state == State.FULL) return;
            state = State.FULLING;
            interactive = true;
            map.send(getGDF());
            state = State.FULL;
        }, template.getRespawnTime(), template.getRespawnTime(), TimeUnit.MILLISECONDS);
    }

    public int getUseDuration() {
        int duration = 1500;
        if (this.getTemplate() != null)
            duration = this.getTemplate().getDuration();
        return duration;
    }

    public int getUnknowValue() {
        int unk = 4;
        if (this.getTemplate() != null)
            unk = this.getTemplate().getUnk();
        return unk;
    }

    public String getGDF() {
        return ("GDF|" + cell.getId() + ";" + state.id + ";" + (isInteractive() ? 1 : 0));
    }

    public enum State {
        FULL(1),
        EMPTYING(2),
        EMPTY(3),
        EMPTY2(4),
        FULLING(5);

        public final int id;

        State(int id) {
            this.id = id;
        }
    }
}
