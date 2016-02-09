package graviton.game.creature.monster;

import lombok.Data;
import lombok.Getter;

/**
 * Created by Botan on 08/02/2016.
 */
@Data
public class MonsterGrade {
    final private MonsterTemplate template;
    private int level;

    public MonsterGrade(MonsterTemplate template,int level) {
        this.template = template;
        this.level = level;
    }

}
