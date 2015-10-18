package graviton.game.spells;

import graviton.game.fight.Fighter;
import graviton.game.maps.Cell;
import lombok.Data;

/**
 * Created by Botan on 28/06/2015.
 */
@Data
public class SpellEffect {

    private int effectID;
    private int turns = 0;
    private String jet = "0d0+0";
    private int chance = 100;
    private String arguments;
    private int value = 0;
    private Fighter fighter = null;
    private int spell = 0;
    private int spellLevel = 1;
    private boolean isDebuffable = true;
    private int duration = 0;
    private Cell cell = null;

    public SpellEffect(int id,String arguments,int spell,int level) {
        this.effectID = id;
        this.arguments = arguments;
        this.spell = spell;
        this.spellLevel = level;
        try {
            this.value = Integer.parseInt(arguments.split(";")[0]);
            this.turns = Integer.parseInt(arguments.split(";")[3]);
            this.chance = Integer.parseInt(arguments.split(";")[4]);
            this.jet = arguments.split(";")[5];
        } catch (Exception e) {
        }
    }

    public SpellEffect(int id, int value, int duration, int turns, boolean debuffable, Fighter fighter, String arguments, int spell) {
        this.effectID = id;
        this.value = value;
        this.turns = turns;
        this.isDebuffable = debuffable;
        this.fighter = fighter;
        this.duration = duration;
        this.arguments = arguments;
        this.spell = spell;
        try {
            jet = arguments.split(";")[5];
        } catch (Exception e) {
        }
    }
}
