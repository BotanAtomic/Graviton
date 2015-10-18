package graviton.game.enums;

import graviton.game.GameManager;
import graviton.game.spells.SpellStats;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;


/**
 * Created by Botan on 21/06/2015.
 */
public enum Classe {
    FECA(1, 10300, 323, 7398, 299, 3, 6, 17),
    OSAMODAS(2, 10284, 372, 7545, 340, 34, 21, 23),
    ENUTROF(3, 10299, 271, 7442, 182, 51, 43, 41),
    SRAM(4, 10285, 263, 7392, 313, 61, 72, 65),
    XELOR(5, 10298, 300, 7332, 327, 82, 81, 83),
    ECAFLIP(6, 10276, 296, 7446, 313, 102, 103, 105),
    ENIRIPSA(7, 10283, 299, 7316, 222, 125, 128, 121),
    IOP(8, 10294, 280, 7427, 267, 143, 141, 142),
    CRA(9, 10292, 284, 7378, 310, 161, 169, 164),
    SADIDA(10, 10279, 254, 7395, 371, 183, 200, 193),
    SACRIEUR(11, 10296, 243, 7336, 197, 432, 431, 434),
    PANDAWA(12, 10289, 236, 10289, 236, 686, 692, 687);

    @Getter
    private final int id, incarnamMap, incarnamCell, astrubMap, astrubCell, startSpell1, startSpell2, startSpell3;

    Classe(int id, int incarnamMap, int incarnamCell, int astrubMap, int astrubCell, int startSpell1, int startSpell2, int startSpell3) {
        this.id = id;
        this.incarnamMap = incarnamMap;
        this.incarnamCell = incarnamCell;
        this.astrubMap = astrubMap;
        this.astrubCell = astrubCell;
        this.startSpell1 = startSpell1;
        this.startSpell2 = startSpell2;
        this.startSpell3 = startSpell3;
    }

    public Map<Integer, SpellStats> getStartSpells(GameManager manager, int level) {
        Map<Integer, SpellStats> spells = new HashMap<>();
        spells.put(startSpell1, manager.getSpells().get(startSpell1).getStats(1));
        spells.put(startSpell2, manager.getSpells().get(startSpell2).getStats(1));
        spells.put(startSpell3, manager.getSpells().get(startSpell3).getStats(1));
        for (Integer i : manager.getClassData().get(this).keySet()) {
            if (i > level) continue;
            spells.put(manager.getClassData().get(this).get(i), manager.getSpells().get(manager.getClassData().get(this).get(i)).getStats(1));
        }
        return spells;
    }

    public Map<Integer, Character> getStartPlace(GameManager manager) {
        Map<Integer, Character> startPlaces = new HashMap<>();
        startPlaces.put(manager.getSpells().get(startSpell1).getId(), 'b');
        startPlaces.put(manager.getSpells().get(startSpell2).getId(), 'c');
        startPlaces.put(manager.getSpells().get(startSpell3).getId(), 'd');
        return startPlaces;
    }

    public int getCost(int stats, int value, Classe classe) {
        switch (stats) {
            case 10://Force
                switch (classe) {
                    case OSAMODAS:
                    case ENIRIPSA:
                    case FECA:
                    case XELOR:
                        if (value < 50) return 2;
                        if (value < 150) return 3;
                        if (value < 250) return 4;
                        return 5;

                    case IOP:
                    case ECAFLIP:
                    case SRAM:
                        if (value < 100) return 1;
                        if (value < 200) return 2;
                        if (value < 300) return 3;
                        if (value < 400) return 4;
                        return 5;

                    case PANDAWA:
                        if (value < 50) return 1;
                        if (value < 200) return 2;
                        return 3;

                    case SADIDA:
                        if (value < 50) return 1;
                        if (value < 250) return 2;
                        if (value < 300) return 3;
                        if (value < 400) return 4;
                        return 5;

                    case CRA:
                    case ENUTROF:
                        if (value < 50) return 1;
                        if (value < 150) return 2;
                        if (value < 250) return 3;
                        if (value < 350) return 4;
                        return 5;
                }
                break;
            case 13://Chance
                switch (classe) {
                    case IOP:
                    case FECA:
                    case CRA:
                    case XELOR:
                    case SRAM:
                        if (value < 20) return 1;
                        if (value < 40) return 2;
                        if (value < 60) return 3;
                        if (value < 80) return 4;
                        return 5;

                    case OSAMODAS:
                    case SADIDA:
                        if (value < 100) return 1;
                        if (value < 200) return 2;
                        if (value < 300) return 3;
                        if (value < 400) return 4;
                        return 5;

                    case PANDAWA:
                        if (value < 50) return 1;
                        if (value < 200) return 2;
                        return 3;

                    case ENUTROF:
                        if (value < 100) return 1;
                        if (value < 150) return 2;
                        if (value < 230) return 3;
                        if (value < 330) return 4;
                        return 5;

                    case ECAFLIP:
                    case ENIRIPSA:
                        if (value < 20) return 1;
                        if (value < 40) return 2;
                        if (value < 60) return 3;
                        if (value < 80) return 4;
                        return 5;
                }
                break;
            case 14://Agilite
                switch (classe) {
                    case SADIDA:
                    case IOP:
                    case ENIRIPSA:
                    case FECA:
                    case ENUTROF:
                    case OSAMODAS:
                    case XELOR:
                        if (value < 20) return 1;
                        if (value < 40) return 2;
                        if (value < 60) return 3;
                        if (value < 80) return 4;
                        return 5;

                    case SRAM:
                        if (value < 100) return 1;
                        if (value < 200) return 2;
                        if (value < 300) return 3;
                        if (value < 400) return 4;
                        return 5;

                    case PANDAWA:
                        if (value < 50) return 1;
                        if (value < 200) return 2;
                        return 3;

                    case ECAFLIP:
                    case CRA:
                        if (value < 50) return 1;
                        if (value < 100) return 2;
                        if (value < 150) return 3;
                        if (value < 200) return 4;
                        return 5;

                }
                break;
            case 15://Intelligence
                switch (classe) {
                    case XELOR:
                    case SADIDA:
                    case ENIRIPSA:
                    case OSAMODAS:
                    case FECA:
                        if (value < 100) return 1;
                        if (value < 200) return 2;
                        if (value < 300) return 3;
                        if (value < 400) return 4;
                        return 5;

                    case SRAM:
                        if (value < 50) return 2;
                        if (value < 150) return 3;
                        if (value < 250) return 4;
                        return 5;

                    case ENUTROF:
                        if (value < 20) return 1;
                        if (value < 60) return 2;
                        if (value < 100) return 3;
                        if (value < 140) return 4;
                        return 5;

                    case PANDAWA:
                        if (value < 50) return 1;
                        if (value < 200) return 2;
                        return 3;

                    case CRA:
                        if (value < 50) return 1;
                        if (value < 150) return 2;
                        if (value < 250) return 3;
                        if (value < 350) return 4;
                        return 5;

                    case IOP:
                    case ECAFLIP:
                        if (value < 20) return 1;
                        if (value < 40) return 2;
                        if (value < 60) return 3;
                        if (value < 80) return 4;
                        return 5;
                }
                break;
        }
        return 5;
    }

}
