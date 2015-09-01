package graviton.game.spells;

import graviton.common.Pair;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Botan on 23/06/2015.
 */
@Data
public class SpellStats {
    private int spell;
    private int level;
    private int cost; // (PA)
    private Pair<Integer,Integer> scopes; // portée mini & portée maxi
    private Pair<Integer,Integer> rates; // CC & EC
    private boolean isLineLaunch;
    private boolean hasLDV;
    private boolean isEmptyCell;
    private boolean changeableScope;
    private Pair<Integer,Integer> maxLaunch; // turn & target
    private int coolDown;
    private int requiredLevel;
    private boolean endTurnEC;
    private Pair<List<SpellEffect>,List<SpellEffect>> effects; // normal & coup critique
    private String scopeType;

    public SpellStats() {
        this.scopes = new Pair<>(0,0);
        this.rates = new Pair<>(0,0);
        this.maxLaunch = new Pair<>(0,0);
        this.effects = new Pair<>(new ArrayList<>(),new ArrayList<>());
    }

    public List<SpellEffect> parseEffect(String arguments) {
        if(arguments.equals("-1"))
            return null;
        List<SpellEffect> effets = new ArrayList<>();
        String[] split = arguments.split("\\|");
        for(String a : split) {
            try {
                int id = Integer.parseInt(a.split(";",2)[0]);
                String args = a.split(";",2)[1];
                effets.add(new SpellEffect(id, args,spell,level));
            }catch(Exception e){
                e.printStackTrace();
            }
        }
        return effets;
    }
}
