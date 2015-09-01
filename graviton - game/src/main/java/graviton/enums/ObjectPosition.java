package graviton.enums;

import lombok.Getter;

/**
 * Created by Botan on 21/06/2015.
 */
public enum ObjectPosition {
    NO_EQUIPED(-1),
    AMULETTE(0),
    ARME(1),
    ANNEAU1(2),
    CEINTURE(3),
    ANNEAU2(4),
    BOTTES(5),
    COIFFE(6),
    CAPE(7),
    FAMILIER(8),
    DOFUS1(9),
    DOFUS2(10),
    DOFUS3(11),
    DOFUS4(12),
    DOFUS5(13),
    DOFUS6(14),
    BOUCLIER(15);

    @Getter
    private final int value;

    ObjectPosition(int value) {
        this.value = value;
    }
}
