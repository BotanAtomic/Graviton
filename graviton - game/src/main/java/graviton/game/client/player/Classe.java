package graviton.game.client.player;

import lombok.Getter;

/**
 * Created by Botan on 21/06/2015.
 */
public enum Classe {
    FECA(1, 10300, 323, 7398, 299),
    OSAMODAS(2, 10284, 372, 7545, 340),
    ENUTROF(3, 10299, 271, 7442, 182),
    SRAM(4, 10285, 263, 7392, 313),
    XELOR(5, 10298, 300, 7332, 327),
    ECAFLIP(6, 10276, 296, 7446, 313),
    ENIRIPSA(7, 10283, 299, 7316, 222),
    IOP(8, 10294, 280, 7427, 267),
    CRA(9, 10292, 284, 7378, 310),
    SADIDA(10, 10279, 254, 7395, 371),
    SACRIEUR(11, 10296, 243, 7336, 197),
    PANDAWA(12, 10289, 236, 10289, 236);

    @Getter
    private final int id, incarnamMap, incarnamCell, astrubMap, astrubCell;

    Classe(int id, int incarnamMap, int incarnamCell, int astrubMap, int astrubCell) {
        this.id = id;
        this.incarnamMap = incarnamMap;
        this.incarnamCell = incarnamCell;
        this.astrubMap = astrubMap;
        this.astrubCell = astrubCell;
    }
}
