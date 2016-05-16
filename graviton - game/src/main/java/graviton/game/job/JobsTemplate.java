package graviton.game.job;

import graviton.game.job.utils.CraftData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by Botan on 13/04/2016.
 */
public enum JobsTemplate {
    BUCHERON((byte) 2, true, new short[]{454, 8539, 1378, 2608, 478, 2593, 2592, 2600, 2604, 456, 502, 675, 674, 923, 927, 515, 782, 673, 676, 771},"101;459,2539,2540,2543,6868,7653,7654,7655,7656,7657,7658,7659,7660,7661,7662,7663,7664,7665,7666,7667,7668,7669,7670,7671,7672,8078"),
    FORGEUR_EPEES((byte) 11, true, new short[]{494},""),
    SCULPTEUR_ARCS((byte) 13, true, new short[]{500},""),
    FORGEUR_MARTEAUX((byte) 14, true, new short[]{493},""),
    CORDONNIER((byte) 15, true, new short[]{579},""),
    BIJOUTIER((byte) 16, true, new short[]{491},""),
    FORGEUR_DAGUES((byte) 17, true, new short[]{495},""),
    SCULPTEUR_BATONS((byte) 18, true, new short[]{498},""),
    SCULPTEUR_BAGUETTES((byte) 19, true, new short[]{499},""),
    FORGEUR_PELLES((byte) 20, true, new short[]{496},""),
    MINEUR((byte) 24, true, new short[]{497},""),
    BOULANGER((byte) 25, true, new short[]{492},""),
    ALCHIMISTE((byte) 26, true, new short[]{1473, 8542},""),
    TAILLEUR((byte) 27, true, new short[]{951},""),
    PAYSAN((byte) 28, true, new short[]{577, 765, 8127, 8540, 8992},"47;285,389,390,396,397,399,529,530,531,534,535,582,583,586,587,690,2019,2022,2027,2030,2033,2037,6672,7068|122;422,427"),
    FORGEUR_HACHES((byte) 31, true, new short[]{922},""),
    PECHEUR((byte) 36, true, new short[]{596, 1860, 1863, 1865, 1866, 1868, 2188, 2366, 6661, 8541},""),
    CHASSEUR((byte) 41, true,""),
    FOREMAGE_DAGUES((byte) 43, false, new short[]{1520},""),
    FOREMAGE_EPEES((byte) 44, false, new short[]{1539},""),
    FOREMAGE_MARTEAUX((byte) 45, false, new short[]{1561},""),
    FOREMAGE_PELLES((byte) 46, false, new short[]{1560},""),
    FOREMAGE_HACHES((byte) 47, false, new short[]{1562},""),
    SCULPTEMAGE_ARCS((byte) 48, false, new short[]{1563},""),
    SCULPTEMAGE_BAGUETTES((byte) 49, false, new short[]{1564},""),
    SCULPTEMAGE_BATONS((byte) 50, false, new short[]{1565},""),
    BOUCHER((byte) 56, true, new short[]{1945},""),
    POISSONNIER((byte) 58, true, new short[]{1946},""),
    FORGEUR_BOUCLIERS((byte) 60, true, new short[]{7098},""),
    CORDOMAGE((byte) 62, false, new short[]{7495},""),
    JOAILLOMAGE((byte) 63, false, new short[]{7493},""),
    COSTUMAGE((byte) 64, false, new short[]{7494},""),
    BRICOLEUR((byte) 65, true, new short[]{7650},""),
    JOAILLER((byte) 66, false,"");

    private final byte id;
    private final boolean basic;
    private final short[] tools;

    private final String craftData;
    private Map<Short, List<CraftData>> crafts;

    JobsTemplate(final byte id, final boolean basic, final short[] tools, String craftData) {
        this.id = id;
        this.basic = basic;
        this.tools = tools;
        this.craftData = craftData;
    }

    JobsTemplate(final byte id, final boolean basic, String craftData) {
        this.id = id;
        this.basic = basic;
        this.tools = new short[0];
        this.craftData = craftData;
    }


    public static JobsTemplate get(final byte id) {
        for (final JobsTemplate job : values())
            if (job.getId() == id)
                return job;
        return null;
    }

    public byte getId() {
        return this.id;
    }

    public boolean isBasic() {
        return this.basic;
    }

    public boolean isValidTool(int object) {
        for (int tool : this.tools)
            if (tool == object)
                return true;
        return false;
    }

    public void configureCraft(Map<Short, CraftData> craftData) {
        this.crafts = new HashMap();
        if(this.craftData.isEmpty()) return;
        for (String value : this.craftData.split("\\|")) {
            this.crafts.put(Short.parseShort(value.split(";")[0]), new ArrayList() {{
                for (String element : value.split(";")[1].split(","))
                    add(craftData.get(Short.parseShort(element)));
            }});
        }
    }

    public List<CraftData> getCraft(short id, byte maxCase) {
        return crafts.get(id).stream().filter(craftData -> craftData.getIngredients().size() <= maxCase).collect(Collectors.toList());
    }
}
