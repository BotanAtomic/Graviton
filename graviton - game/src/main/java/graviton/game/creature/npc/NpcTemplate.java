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

    public NpcTemplate(int id,int gfx,int sex, int[] colors,String accessories,int extraClip,int customArt,int initQuestion) {
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

    public String generateGm(Npc npc) {
        StringBuilder builder = new StringBuilder();
        builder.append(npc.getPosition().getCell().getId()).append(";").append(npc.getPosition().getOrientation()).append(";0;");
        builder.append(npc.getId()).append(";").append(this.id).append(";-4;").append(this.gfx).append("^100;");
        builder.append(this.sex).append(";");
        builder.append((this.getColor(1) != -1 ? Integer.toHexString(this.getColor(1)) : "-1")).append(";");
        builder.append((this.getColor(2) != -1 ? Integer.toHexString(this.getColor(2)) : "-1")).append(";");
        builder.append((this.getColor(3) != -1 ? Integer.toHexString(this.getColor(3)) : "-1")).append(";");
        builder.append(this.accessories).append(";").append(this.extraClip == -1 ? "" : this.extraClip).append(";").append(this.customArt);
        return builder.toString();
    }

}
