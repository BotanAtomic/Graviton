package graviton.game.spells;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

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

    public SpellTemplate(int id, int sprite, String spriteInfos, ResultSet resultSet) {
        this.id = id;
        this.sprite = sprite;
        this.spriteInfos = spriteInfos;
        this.stats = new HashMap<>();
        configureStats(resultSet);
    }

    private void configureStats(ResultSet result) {
        try {
            String[] results = {result.getString("level1"), result.getString("level2"), result.getString("level3"),
                    result.getString("level4"), result.getString("level5"), result.getString("level6")};
            addSpellStats(1, results[0]);
            addSpellStats(2, results[1]);
            addSpellStats(3, results[2]);
            addSpellStats(4, results[3]);
            addSpellStats(5, results[4]);
            addSpellStats(6, results[5]);
        } catch (SQLException e) {
            log.error("Configure stats ", e);
        }

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
