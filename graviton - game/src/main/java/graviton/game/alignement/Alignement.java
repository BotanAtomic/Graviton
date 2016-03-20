package graviton.game.alignement;

import graviton.enums.DataType;
import graviton.game.GameManager;
import graviton.game.client.player.Player;
import lombok.Data;

/**
 * Created by Botan on 22/06/2015.
 */
@Data
public class Alignement {
    private final Player player;
    private final GameManager gameManager;

    private Type type;
    private int honor, deshonnor;
    private int grade;
    private boolean showWings;

    public Alignement(int id) {
        this.player = null;
        this.gameManager = null;
        this.type = id == -1 ? Type.NEUTRE : Type.values()[id];
    }

    public Alignement(Player player) {
        this.player = player;
        this.gameManager = player.getGameManager();
        this.type = Type.NEUTRE;
        this.honor = 0;
        this.deshonnor = 0;
        this.showWings = false;
        this.grade = getGradeByHonor();
    }

    public Alignement(Player player, int alignement, int honor, int deshonnor, boolean showWings) {
        this.player = player;
        this.gameManager = player == null ? null : player.getGameManager();
        this.type = alignement <= 0 ? Type.NEUTRE : Type.values()[alignement];
        this.honor = honor;
        this.deshonnor = deshonnor;
        this.showWings = showWings;
        this.grade = getGradeByHonor();
    }

    private int getGradeByHonor() {
        if (type == Type.NEUTRE)
            return 0;
        if (this.getHonor() >= 17500)
            return 10;
        for (int i = 1; i <= 10; i++)
            if (this.getHonor() < gameManager.getExperience().getData().get(DataType.PVP).get(i))
                return i - 1;
        return 0;
    }

    public void addHonor(int honor) {
        this.honor += honor;
        if (grade != getGradeByHonor()) {
            grade = getGradeByHonor();
            player.send("Im080;" + grade);
        }
    }

    public void removeHonor(int honor) {
        this.honor -= honor;
        if (grade != getGradeByHonor()) {
            grade = getGradeByHonor();
            player.send("Im00;" + "Tu viens de descendre grade " + grade);
        }
    }

    public enum Type {
        NEUTRE(0),
        BONTARIEN(1),
        BRAKMARIEN(2),
        MERCENAIRE(3);

        private final int id;

        Type(int id) {
            this.id = id;
        }

        public int getId() {
            return id;
        }
    }
}
