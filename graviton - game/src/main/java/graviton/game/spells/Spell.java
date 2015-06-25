package graviton.game.spells;

import lombok.Data;

import java.util.Map;

/**
 * Created by Botan on 23/06/2015.
 */
@Data
public class Spell {
    private int id;
    private int sprite;

    private Map<Integer,SpellStats> stats;
}
