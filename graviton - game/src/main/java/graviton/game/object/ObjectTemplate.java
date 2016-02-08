package graviton.game.object;

import com.google.inject.Inject;
import com.google.inject.Injector;
import graviton.common.Utils;
import graviton.factory.ObjectFactory;
import graviton.game.enums.ObjectPosition;
import graviton.game.enums.ObjectType;
import graviton.game.statistics.Statistics;
import lombok.Data;


/**
 * Created by Botan on 21/06/2015.
 */
@Data
public class ObjectTemplate {
    @Inject
    ObjectFactory manager;

    final private Injector injector;

    final private int[] swordEffectId = {91, 92, 93, 94, 95, 96, 97, 98, 99, 100, 101};

    final private int id;
    final private ObjectType type;
    final private String name;
    final private int level;
    final private String statistics;
    final private int usedPod;
    final private int panoply;
    final private int price;
    final private String condition;
    final private String information;


    public ObjectTemplate(int id, int type, String name, int level, String statistics, int usedPod, int panoply, int price, String condition, String information,Injector injector) {
        this.injector = injector;
        injector.injectMembers(this);
        this.id = id;
        this.type = ObjectType.getTypeById(type);
        this.name = name;
        this.level = level;
        this.statistics = statistics;
        this.usedPod = usedPod;
        this.panoply = panoply;
        this.price = price;
        this.condition = condition;
        this.information = information;
    }

    public Object createObject(int qua, boolean useMax) {
        return new Object(manager.getNextId(), this.getId(), qua, ObjectPosition.NO_EQUIPED, (statistics.equals("") ? new Statistics() : this.getStatistics(statistics, useMax)),injector);
    }

    private Statistics getStatistics(String statsTemplate, boolean useMax) {
        Statistics itemStats = new Statistics();
        if (statsTemplate.equals(""))
            return itemStats;
        String[] splitted = statsTemplate.split(",");
        for (String s : splitted) {
            String[] stats = s.split("#");
            int statID = Integer.parseInt(stats[0], 16);
            boolean follow = true;
            for (int a : this.swordEffectId)//Si c'est un Effet Actif
                if (a == statID)
                    follow = false;
            if (!follow)//Si c'?tait un effet Actif d'arme
                continue;
            boolean isStatsInvalid = false;
            switch (statID) {
                case 110:
                case 139:
                case 605:
                case 614:
                    isStatsInvalid = true;
                    break;
            }
            if (isStatsInvalid)
                continue;
            String jet;
            int value = 1;
            try {
                jet = stats[4];
                value = getRandomJet(jet);
                if (useMax) {
                    try {
                        //on prend le jet max
                        int min = Integer.parseInt(stats[1], 16);
                        int max = Integer.parseInt(stats[2], 16);
                        value = min;
                        if (max != 0) value = max;
                    } catch (Exception e) {
                        value = getRandomJet(jet);
                    }

                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            itemStats.addEffect(statID, value);
        }
        return itemStats;
    }

    private int getRandomJet(String jet) {
        try {
            int num = 0;
            int des = Integer.parseInt(jet.split("d")[0]);
            int faces = Integer.parseInt(jet.split("d")[1].split("\\+")[0]);
            int add = Integer.parseInt(jet.split("d")[1].split("\\+")[1]);
            for (int a = 0; a < des; a++) {
                num += Utils.getRandomValue(1, faces);
            }
            num += add;
            return num;
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}
