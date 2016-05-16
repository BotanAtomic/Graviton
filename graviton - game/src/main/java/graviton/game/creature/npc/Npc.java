package graviton.game.creature.npc;

import com.google.inject.Inject;
import com.google.inject.Injector;
import graviton.game.GameManager;
import graviton.game.creature.Creature;
import graviton.game.creature.Position;
import graviton.game.enums.IdType;
import graviton.game.maps.Maps;
import lombok.Data;

/**
 * Created by Botan on 19/12/2015.
 */
@Data
public class Npc implements Creature {
    @Inject
    GameManager manager;

    private final int id;

    private NpcTemplate template;

    private Position position;


    public Npc(int template,Maps maps,int cell,int orientation,Injector injector) {
        injector.injectMembers(this);
        this.id = IdType.NPC.MAXIMAL_ID - maps.getId()* 1000 - cell;
        this.template = manager.getNpcTemplate(template);
        this.position = new Position(maps,maps.getCell(cell),orientation);
    }

    @Override
    public void speak(String message) {
        this.position.getMap().send("cMK|" + id + "|PNJ |" + message);
    }

    @Override
    public String getGm() {
        return template.generateGm(this);
    }

    @Override
    public void send(String packet) {

    }

}
