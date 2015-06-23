package graviton.enums;

import lombok.Data;

/**
 * Created by Botan on 22/06/2015.
 */
@Data
public class Alignement {
    private Type type;
    private int honor, deshonnor;
    private boolean wings;

    public Alignement() {
        this.type = Type.NEUTRE;
        this.honor = 0;
        this.deshonnor = 0;
        this.wings = false;
    }

    public Alignement(int alignement, int honor, int deshonnor) {
        this.type = alignement <= 0 ? Type.NEUTRE : Type.values()[alignement - 1];
        this.honor = honor;
        this.deshonnor = deshonnor;
        this.wings = false;
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
