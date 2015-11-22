package graviton.game.spells;

import lombok.Data;


/**
 * Created by Botan on 23/06/2015.
 */
@Data
public class Spell {
    private int template;
    private int level;
    private int requiredLevel;

    public Spell(int template, int level,int requiredLevel) {
        this.template = template;
        this.level = level;
        this.requiredLevel = requiredLevel;
    }

}
