package graviton.factory.type;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import graviton.api.Factory;
import graviton.database.Database;
import graviton.enums.DataType;
import graviton.factory.FactoryManager;
import graviton.game.job.Job;
import graviton.game.job.JobsTemplate;
import graviton.game.job.actions.JobAction;
import graviton.game.job.actions.type.Craft;
import graviton.game.job.actions.type.Harvest;
import graviton.game.job.utils.CraftData;
import lombok.Getter;

import static graviton.database.utils.game.Tables.CRAFTS;

import java.util.*;

/**
 * Created by Botan on 14/06/2016.
 */
public class JobFactory extends Factory<Job> {
    private final short[][] actions = {{101}, {6, 303}, {39, 473}, {40, 476}, {10, 460}, {141, 2357},
            {139, 2358}, {37, 471}, {154, 7013}, {33, 461}, {41, 474}, {34, 449}, {174, 7925},
            {155, 7016}, {38, 472}, {35, 470}, {158, 7014}, {48}, {32}, {24, 312}, {25, 441},
            {26, 442}, {28, 443}, {56, 445}, {162, 7032}, {55, 444}, {29, 350}, {31, 446},
            {30, 313}, {161, 7033}, {133}, {124, 1782, 1844, 603}, {125, 1844, 603, 1847, 1794},
            {126, 603, 1847, 1794, 1779}, {127, 1847, 1794, 1779, 1801}, {128, 598, 1757, 1750},
            {129, 1757, 1805, 600}, {130, 1805, 1750, 1784, 600}, {131, 600, 1805, 602, 1784}, {136, 2187},
            {140, 1759}, {140, 1799}, {23}, {68, 421}, {69, 428}, {71, 395}, {72, 380}, {73, 593},
            {74, 594}, {160, 7059}, {122}, {47}, {45, 289}, {53, 400}, {57, 533}, {46, 401},
            {50, 423}, {52, 532}, {159, 7018}, {58, 405}, {54, 425}, {109}, {27}, {135}, {134},
            {132}, {64}, {123}, {63}, {11}, {12}, {13}, {14}, {145}, {20}, {144}, {19},
            {142}, {18}, {146}, {21}, {65}, {143}, {115}, {1}, {116}, {113}, {117}, {120},
            {119}, {118}, {165}, {166}, {167}, {163}, {164}, {169}, {168}, {171}, {182},
            {15}, {149}, {17}, {147}, {16}, {148}, {156}, {151}, {110}};

    @Getter
    private Map<Integer, JobAction.Getter> jobActions;

    public short getObjectByAction(short action) {
        for (byte actions = 0; actions < this.actions.length; actions++) {
            if (this.actions[actions][0] == action) {
                short[] objects = this.actions[actions];
                if (objects.length > 2)
                    return objects[(int) System.nanoTime() % objects.length];
                if (objects.length > 1)
                    return objects[1];
            }
        }
        return 0;
    }

    @Inject
    public JobFactory(@Named("database.game") Database database, FactoryManager manager) {
        super(database);
        manager.addFactory(this);
    }

    private void configureCraft() {
        Map<Short, CraftData> elements = new HashMap<>();
        database.getResult(CRAFTS).forEach(craft -> elements.put(craft.getValue(CRAFTS.ID), new CraftData(craft.getValue(CRAFTS.CRAFT), craft.getValue(CRAFTS.ID))));

        for (JobsTemplate jobTemplate : JobsTemplate.values())
            jobTemplate.configureCraft(elements);
    }

    private void configureJobActions() {
        Map<Integer, JobAction.Getter> elements = new HashMap<>();

        /**elements.put(16, toList(job -> new Craft((short) 11),job -> new Craft((short) 12)));
         elements.put(27, toList(job -> new Craft((short) 11),job -> new Craft((short) 12)));
         elements.put(60, toList(job -> new Craft((short) 11),job -> new Craft((short) 12)));
         elements.put(65, toList(job -> new Craft((short) 11),job -> new Craft((short) 12)));
         elements.put(15, toList(job -> new Craft((short) 11),job -> new Craft((short) 12)));
         elements.put(13, toList(job -> new Craft((short) 11),job -> new Craft((short) 12)));
         elements.put(18, toList(job -> new Craft((short) 11),job -> new Craft((short) 12)));
         elements.put(19, toList(job -> new Craft((short) 11),job -> new Craft((short) 12)));
         elements.put(62, toList(job -> new Craft((short) 11),job -> new Craft((short) 12)));
         elements.put(63, toList(job -> new Craft((short) 11),job -> new Craft((short) 12)));
         elements.put(64, toList(job -> new Craft((short) 11),job -> new Craft((short) 12)));
         elements.put(11, toList(job -> new Craft((short) 11),job -> new Craft((short) 12)));
         elements.put(17, toList(job -> new Craft((short) 11),job -> new Craft((short) 12)));
         elements.put(14, toList(job -> new Craft((short) 11),job -> new Craft((short) 12)));
         elements.put(20, toList(job -> new Craft((short) 11),job -> new Craft((short) 12)));
         elements.put(31 , toList(job -> new Craft((short) 11),job -> new Craft((short) 12)));
         elements.put(47 , toList(job -> new Craft((short) 11),job -> new Craft((short) 12)));
         elements.put(43 , toList(job -> new Craft((short) 11),job -> new Craft((short) 12)));
         elements.put(44 , toList(job -> new Craft((short) 11),job -> new Craft((short) 12)));
         elements.put(45 , toList(job -> new Craft((short) 11),job -> new Craft((short) 12)));
         elements.put(46 , toList(job -> new Craft((short) 11),job -> new Craft((short) 12)));
         elements.put(48 , toList(job -> new Craft((short) 11),job -> new Craft((short) 12)));
         elements.put(49 , toList(job -> new Craft((short) 11),job -> new Craft((short) 12)));
         elements.put(50 , toList(job -> new Craft((short) 11),job -> new Craft((short) 12)));
         elements.put(41 , toList(job -> new Craft((short) 11),job -> new Craft((short) 12)));
         elements.put(56 , toList(job -> new Craft((short) 11),job -> new Craft((short) 12)));
         elements.put(58 , toList(job -> new Craft((short) 11),job -> new Craft((short) 12)));
         elements.put(25 , toList(job -> new Craft((short) 11),job -> new Craft((short) 12)));**/

        /** Harvest **/

        elements.put(28, (job -> {
            List<JobAction> jobActions = new ArrayList<>();
            if (job.getLevel() > 69)
                jobActions.add(new Harvest((short) 54, job.getLevel(), (byte) -13, (byte) 45, job));
            if (job.getLevel() > 59)
                jobActions.add(new Harvest((short) 58, job.getLevel(), (byte) -10, (byte) 40, job));
            if (job.getLevel() > 49) {
                jobActions.add(new Harvest((short) 159, job.getLevel(), (byte) -8, (byte) 35, job));
                jobActions.add(new Harvest((short) 52, job.getLevel(), (byte) -8, (byte) 35, job));
            }
            if (job.getLevel() > 39)
                jobActions.add(new Harvest((short) 50, job.getLevel(), (byte) -6, (byte) 30, job));
            if (job.getLevel() > 29)
                jobActions.add(new Harvest((short) 46, job.getLevel(), (byte) -4, (byte) 25, job));
            if (job.getLevel() > 19)
                jobActions.add(new Harvest((short) 57, job.getLevel(), (byte) -2, (byte) 20, job));
            if (job.getLevel() > 9)
                jobActions.add(new Harvest((short) 53, job.getLevel(), (byte) 0, (byte) 15, job));

            jobActions.add(new Harvest((short) 45, job.getLevel(), (byte) 1, (byte) 10, job));
            jobActions.add(new Craft((short) 47, (byte) 0, getChance(job.getLevel()), getMaxCase(job.getLevel()), job));
            jobActions.add(new Craft((short) 122, (byte) 10, (byte) 100, (byte) 1, job));
            return jobActions;
        }));
        this.jobActions = Collections.unmodifiableMap(elements);
    }

    private byte getChance(byte level) {
        return level < 10 ? (byte) 50 : (byte) (54 + (level / 10 - 1) * 5);
    }

    private byte getMaxCase(byte level) {
        return level < 10 ? 2 : level == 100 ? 9 : (byte) (level / 20 + 3);
    }

    @Override
    public DataType getType() {
        return DataType.JOB;
    }

    @Override
    public Map<Integer, Job> getElements() {
        return null;
    }

    @Override
    public void configure() {
        configureJobActions();
        configureCraft();
    }

    @Override
    public void save() {

    }
}
