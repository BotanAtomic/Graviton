package graviton.game.spells;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.jooq.Record;

import java.util.HashMap;
import java.util.Map;

import static graviton.database.utils.game.Tables.SPELLS;

/**
 * Created by Botan on 23/06/2015.
 */
@Data
@Slf4j
public class SpellTemplate {
    private int id;
    private int sprite;
    private String spriteInfos;

    private Map<Integer, Spell> stats;

    public SpellTemplate(int id, int sprite, String spriteInfos, Record record) {
        this.id = id;
        this.sprite = sprite;
        this.spriteInfos = spriteInfos;
        this.stats = new HashMap<>();
        configureStats(record);
    }

    private void configureStats(Record record) {
        addSpellStats(1, record.getValue(SPELLS.LEVEL1));
        addSpellStats(2, record.getValue(SPELLS.LEVEL2));
        addSpellStats(3, record.getValue(SPELLS.LEVEL3));
        addSpellStats(4, record.getValue(SPELLS.LEVEL4));
        addSpellStats(5, record.getValue(SPELLS.LEVEL5));
        addSpellStats(6, record.getValue(SPELLS.LEVEL6));
    }

    private void addSpellStats(int level, String arguments) {
        if (arguments.equals("-1"))
            return;
        String[] stats = arguments.split(",");
        this.stats.put(level, new Spell(this.id, level, (Integer.parseInt(stats[stats.length - 2].trim()))));
    }

    public Spell getStats(int id) {
        return stats.get(id);
    }
}
