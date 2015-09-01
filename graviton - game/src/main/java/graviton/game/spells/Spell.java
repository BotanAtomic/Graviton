package graviton.game.spells;

import graviton.common.Pair;
import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Botan on 23/06/2015.
 */
@Data
public class Spell {
    private int id;
    private int sprite;
    private String spriteInfos;
    private Pair<ArrayList<Integer>,ArrayList<Integer>> effectTargets; //normal & coup critique

    private Map<Integer,SpellStats> stats;

    public Spell(int id,int sprite,String spriteInfos,String effectTarget) {
        this.id = id;
        this.sprite = sprite;
        this.spriteInfos = spriteInfos;
        this.stats = new HashMap<>();
        //TODO : Effect target
    }

    public void addSpellStats(int level, String arguments) {
        if(arguments.equals("-1"))
            return;
        SpellStats spellStats = null;
        String[] stats = arguments.split(",");
        try {
            spellStats = new SpellStats() {{
                setLevel(level);
                getEffects().setFirst(parseEffect(stats[0].trim()));
                getEffects().setSecond(parseEffect(stats[1].trim()));
                try {
                    setCost(Integer.parseInt(stats[2].trim()));
                } catch (Exception e) {
                    setCost(6); //Par default 6Pa, a corriger dans les sorts si exeption !
                }
                getScopes().setFirst(Integer.parseInt(stats[3].trim()));
                getScopes().setSecond(Integer.parseInt(stats[4].trim()));
                getRates().setFirst(Integer.parseInt(stats[5].trim()));
                getRates().setSecond(Integer.parseInt(stats[6].trim()));
                setLineLaunch(stats[7].trim().equalsIgnoreCase("true"));
                setHasLDV(stats[8].trim().equalsIgnoreCase("true"));
                setEmptyCell(stats[9].trim().equalsIgnoreCase("true"));
                setChangeableScope(stats[10].trim().equalsIgnoreCase("true"));
                getMaxLaunch().setFirst(Integer.parseInt(stats[12].trim()));
                getMaxLaunch().setSecond(Integer.parseInt(stats[13].trim()));
                setCoolDown(Integer.parseInt(stats[14].trim()));
                setScopeType(stats[15].trim());
                setRequiredLevel(Integer.parseInt(stats[stats.length - 2].trim()));
                setEndTurnEC(stats[19].trim().equalsIgnoreCase("true"));
            }};
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if(stats == null)
                this.stats = new HashMap<>();
            this.stats.put(level,spellStats);
        }
    }

    public SpellStats getStats(int id) {
        return stats.get(id);
    }
}
