package graviton.game.enums;

import graviton.game.GameManager;
import graviton.game.client.player.Player;
import graviton.game.common.Stats;
import graviton.game.spells.Spell;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;


/**
 * Created by Botan on 21/06/2015.
 */
public enum Classe {

    FECA(1, 3, 6, 17, new int[]{4, 2, 1, 9, 18, 20, 14, 19, 5, 16, 8, 12, 11, 10, 7, 15, 13, 1901}),
    OSAMODAS(2, 34, 21, 23, new int[]{26, 22, 35, 28, 37, 30, 27, 24, 33, 25, 38, 36, 32, 29, 39, 40, 31, 1902}),
    ENUTROF(3, 51, 43, 41, new int[]{49, 42, 47, 48, 45, 53, 46, 52, 44, 50, 54, 55, 56, 58, 59, 57, 60, 1903}),
    SRAM(4, 61, 72, 65, new int[]{66, 68, 63, 74, 64, 79, 78, 71, 62, 69, 77, 73, 67, 70, 75, 76, 80, 1904}),
    XELOR(5, 82, 81, 83, new int[]{84, 100, 92, 88, 93, 85, 96, 98, 86, 89, 90, 87, 94, 99, 95, 91, 97, 1905}),
    ECAFLIP(6, 102, 103, 105, new int[]{109, 113, 111, 104, 119, 101, 107, 116, 106, 117, 108, 115, 118, 110, 112, 114, 120, 1906}),
    ENIRIPSA(7, 125, 128, 121, new int[]{124, 122, 126, 127, 123, 130, 131, 132, 133, 134, 135, 129, 136, 137, 138, 139, 140, 1907}),
    IOP(8, 143, 141, 142, new int[]{144, 145, 146, 147, 148, 154, 150, 151, 155, 152, 153, 149, 156, 157, 158, 160, 159, 1908}),
    CRA(9, 161, 169, 164, new int[]{136, 165, 172, 167, 168, 162, 170, 171, 166, 173, 174, 176, 175, 178, 177, 179, 180, 1909}),
    SADIDA(10, 183, 200, 193, new int[]{198, 195, 182, 192, 197, 189, 181, 199, 191, 186, 196, 190, 194, 185, 184, 188, 187, 1910}),
    SACRIEUR(11, 432, 431, 434, new int[]{444, 449, 436, 437, 439, 433, 443, 440, 442, 441, 445, 438, 446, 447, 448, 435, 450, 1911}),
    PANDAWA(12, 686, 692, 687, new int[]{689, 690, 691, 688, 693, 694, 695, 696, 697, 698, 699, 700, 701, 702, 703, 704, 705, 1912});

    @Getter
    private final int id, startSpell1, startSpell2, startSpell3;
    @Getter
    private final int[] spells;

    Classe(int id, int startSpell1, int startSpell2, int startSpell3, int[] spells) {
        this.id = id;
        this.startSpell1 = startSpell1;
        this.startSpell2 = startSpell2;
        this.startSpell3 = startSpell3;
        this.spells = spells;
    }

    public Map<Integer, Spell> getStartSpells(GameManager manager, int level) {
        Map<Integer, Spell> spells = new HashMap<>();
        spells.put(startSpell1, manager.getSpellTemplate(startSpell1).getStats(1));
        spells.put(startSpell2, manager.getSpellTemplate(startSpell2).getStats(1));
        spells.put(startSpell3, manager.getSpellTemplate(startSpell3).getStats(1));
        for (Integer i : manager.getClassData().get(this).keySet()) {
            if (i > level) continue;
            spells.put(manager.getClassData().get(this).get(i), manager.getSpellTemplate(manager.getClassData().get(this).get(i)).getStats(1));
        }
        return spells;
    }

    public Map<Integer, Character> getStartPlace(GameManager manager) {
        Map<Integer, Character> startPlaces = new HashMap<>();
        startPlaces.put(manager.getSpellTemplate(startSpell1).getId(), 'b');
        startPlaces.put(manager.getSpellTemplate(startSpell2).getId(), 'c');
        startPlaces.put(manager.getSpellTemplate(startSpell3).getId(), 'd');
        return startPlaces;
    }

    public void boostStatistics(Player player, int id) {
        int cost = 0, value;
        switch (id) {
            case 10: //Force
                value = player.getStatistics().get(StatsType.BASE).getEffect(Stats.ADD_FORC);
                cost = player.getClasse() == Classe.SACRIEUR ? 3 : getCost(id, value, player.getClasse());
                if (cost <= player.getCapital())
                    player.getStatistics().get(StatsType.BASE).addEffect(Stats.ADD_FORC, 1);
                break;
            case 11: //Vitalite
                cost = 1;
                if (cost <= player.getCapital())
                    player.getStatistics().get(StatsType.BASE).addEffect(Stats.ADD_VITA, player.getClasse() == Classe.SACRIEUR ? 2 : 1);
                break;
            case 12: //Sagesse
                cost = 3;
                if (cost <= player.getCapital())
                    player.getStatistics().get(StatsType.BASE).addEffect(Stats.ADD_SAGE, 1);
                break;
            case 13: //Chance
                value = player.getStatistics().get(StatsType.BASE).getEffect(Stats.ADD_CHAN);
                cost = player.getClasse() == Classe.SACRIEUR ? 3 : getCost(id, value, player.getClasse());
                if (cost <= player.getCapital())
                    player.getStatistics().get(StatsType.BASE).addEffect(Stats.ADD_CHAN, 1);
                break;
            case 14: // Agilite
                value = player.getStatistics().get(StatsType.BASE).getEffect(Stats.ADD_AGIL);
                cost = player.getClasse() == Classe.SACRIEUR ? 3 : getCost(id, value, player.getClasse());
                if (cost <= player.getCapital())
                    player.getStatistics().get(StatsType.BASE).addEffect(Stats.ADD_AGIL, 1);
                break;
            case 15: //Intelligence
                value = player.getStatistics().get(StatsType.BASE).getEffect(Stats.ADD_INTE);
                cost = player.getClasse() == Classe.SACRIEUR ? 3 : getCost(id, value, player.getClasse());
                if (cost <= player.getCapital())
                    player.getStatistics().get(StatsType.BASE).addEffect(Stats.ADD_INTE, 1);
                break;
        }
        player.setCapital(player.getCapital() - cost);
        player.send(player.getPacket("As"));
        player.save();
    }

    private int getCost(int stats, int value, Classe classe) {
        switch (stats) {
            case 10://Force
                if (classe == OSAMODAS || classe == ENIRIPSA || classe == FECA || classe == XELOR) {
                    if (value < 50) return 2;
                    if (value < 150) return 3;
                    if (value < 250) return 4;
                    return 5;
                } else if (classe == IOP || classe == ECAFLIP || classe == SRAM) {
                    if (value < 100) return 1;
                    if (value < 200) return 2;
                    if (value < 300) return 3;
                    if (value < 400) return 4;
                    return 5;
                } else if (classe == PANDAWA) {
                    if (value < 50) return 1;
                    if (value < 200) return 2;
                    return 3;
                } else if (classe == SADIDA) {
                    if (value < 50) return 1;
                    if (value < 250) return 2;
                    if (value < 300) return 3;
                    if (value < 400) return 4;
                    return 5;
                } else if (classe == CRA || classe == ENUTROF) {
                    if (value < 50) return 1;
                    if (value < 150) return 2;
                    if (value < 250) return 3;
                    if (value < 350) return 4;
                    return 5;
                }
            case 13://Chance
                if (classe == IOP || classe == FECA || classe == CRA || classe == XELOR || classe == SRAM) {
                    if (value < 20) return 1;
                    if (value < 40) return 2;
                    if (value < 60) return 3;
                    if (value < 80) return 4;
                    return 5;
                } else if (classe == OSAMODAS || classe == SADIDA) {
                    if (value < 100) return 1;
                    if (value < 200) return 2;
                    if (value < 300) return 3;
                    if (value < 400) return 4;
                    return 5;
                } else if (classe == PANDAWA) {
                    if (value < 50) return 1;
                    if (value < 200) return 2;
                    return 3;
                } else if (classe == ENUTROF) {
                    if (value < 100) return 1;
                    if (value < 150) return 2;
                    if (value < 230) return 3;
                    if (value < 330) return 4;
                    return 5;
                } else if (classe == ECAFLIP || classe == ENIRIPSA) {
                    if (value < 20) return 1;
                    if (value < 40) return 2;
                    if (value < 60) return 3;
                    if (value < 80) return 4;
                    return 5;
                }
            case 14://Agilite
                if (classe == IOP || classe == SADIDA || classe == ENIRIPSA || classe == FECA || classe == ENUTROF || classe == OSAMODAS || classe == XELOR) {
                    if (value < 20) return 1;
                    if (value < 40) return 2;
                    if (value < 60) return 3;
                    if (value < 80) return 4;
                    return 5;
                } else if (classe == SRAM) {
                    if (value < 100) return 1;
                    if (value < 200) return 2;
                    if (value < 300) return 3;
                    if (value < 400) return 4;
                    return 5;
                } else if (classe == PANDAWA) {
                    if (value < 50) return 1;
                    if (value < 200) return 2;
                    return 3;
                } else if (classe == ECAFLIP) {
                    if (value < 50) return 1;
                    if (value < 100) return 2;
                    if (value < 150) return 3;
                    if (value < 200) return 4;
                    return 5;
                } else if (classe == CRA) {
                    if (value < 50) return 1;
                    if (value < 100) return 2;
                    if (value < 150) return 3;
                    if (value < 200) return 4;
                    return 5;
                }
                break;
            case 15://Intelligence
                if (classe == XELOR || classe == SADIDA || classe == ENIRIPSA || classe == FECA || classe == ENUTROF || classe == OSAMODAS) {
                    if (value < 100) return 1;
                    if (value < 200) return 2;
                    if (value < 300) return 3;
                    if (value < 400) return 4;
                    return 5;
                } else if (classe == SRAM) {
                    if (value < 50) return 2;
                    if (value < 150) return 3;
                    if (value < 250) return 4;
                    return 5;
                } else if (classe == ENUTROF) {
                    if (value < 20) return 1;
                    if (value < 60) return 2;
                    if (value < 100) return 3;
                    if (value < 140) return 4;
                    return 5;
                } else if (classe == PANDAWA) {
                    if (value < 50) return 1;
                    if (value < 200) return 2;
                    return 3;
                } else if (classe == CRA) {
                    if (value < 50) return 1;
                    if (value < 150) return 2;
                    if (value < 250) return 3;
                    if (value < 350) return 4;
                    return 5;
                }
                break;
        }
        return 5;
    }


}
