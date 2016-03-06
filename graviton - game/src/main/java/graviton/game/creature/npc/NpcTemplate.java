package graviton.game.creature.npc;

import graviton.game.object.Object;
import lombok.Data;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by Botan on 19/12/2015.
 */
@Data
public class NpcTemplate {
    private final int id;

    private int gfx;
    private byte sex;
    private int[] colors;

    private int customArt;
    private String accessories;
    private int extraClip;

    private int initQuestion;
    private List<Object> items;

    public NpcTemplate(int id,int gfx,int sex, int[] colors,String accessories,int customArt,int extraClip,int initQuestion) {
        this.id = id;
        this.gfx = gfx;
        this.sex = (byte)sex;
        this.colors = colors;
        this.accessories = accessories;
        this.customArt = customArt;
        this.extraClip = extraClip;
        this.initQuestion = initQuestion;
        this.items = new CopyOnWriteArrayList<>();
    }

    public int getColor(int color) {
        return colors[color-1];
    }
}
