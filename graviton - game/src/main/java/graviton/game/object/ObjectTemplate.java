package graviton.game.object;

import com.google.inject.Inject;
import com.google.inject.Injector;
import graviton.common.Parameter;
import graviton.factory.ObjectFactory;
import graviton.game.object.panoply.PanoplyTemplate;
import graviton.game.statistics.Statistics;
import lombok.Data;

import java.util.stream.IntStream;


/**
 * Created by Botan on 21/06/2015.
 */
@Data
public class ObjectTemplate {
    final private Injector injector;
    final private int id;
    final private ObjectType type;
    final private String name;
    final private int level;
    final private String statistics;
    final private int usedPod;
    final private int price;
    final private String condition;
    final private String information;
    @Inject
    ObjectFactory factory;
    private PanoplyTemplate panoplyTemplate;

    public ObjectTemplate(int id, int type, String name, int level, String statistics, int usedPod, int price, String condition, String information, int panoplyId, Injector injector) {
        this.injector = injector;
        injector.injectMembers(this);
        this.id = id;
        this.type = ObjectType.getTypeById(type);
        this.name = name;
        this.level = level;
        this.statistics = statistics;
        this.usedPod = usedPod;
        this.price = price;
        this.condition = condition;
        this.information = information;
        this.factory.addObjectTemplate(this);
        if (panoplyId > 0)
            this.panoplyTemplate = factory.getPanoply(panoplyId);
    }

    public String getStatistics() {
        return String.valueOf(this.id).concat(";").concat(statistics);
    }

    public Object createObject(int qua, boolean useMax) {
        return new Object(factory.getNextId(), this.getId(), qua, -1, (statistics.equals("") ? new Statistics() : this.getStatistics(statistics, useMax)), injector);
    }

    public Statistics getStatistics(String statisticsTemplate, boolean useMax) {
        Statistics statistic = new Statistics();
        if(statisticsTemplate.isEmpty()) return statistic;
        int maximum;
        for (String statisticTemplate : statisticsTemplate.split(",")) {
            String[] arguments = statisticTemplate.split("#");
            final int statisticId = Integer.parseInt(arguments[0], 16);

            if (statisticId >= 91 && statisticId <= 101) //sword effect
                continue;

            if(arguments.length < 5)
                continue;

            String argument = arguments[4];

            if (argument.contains("d") && argument.contains("+") && factory.getEffects().contains(statisticId)) {
                maximum = Integer.parseInt(arguments[2], 16);
                statistic.addEffect(statisticId, useMax ? (maximum > 0 ? maximum : Integer.parseInt(arguments[1], 16)) : this.getRandomJet(argument));
            } else
                statistic.addOptionalEffect(statisticId, new Parameter<>(Integer.parseInt(arguments[1], 16), Integer.parseInt(arguments[2], 16), Integer.parseInt(arguments[3], 16), argument));
        }
        return statistic;
    }

    private int getRandomJet(String jet) {
        int faces = Integer.parseInt(jet.split("d")[1].split("\\+")[0]);
        final int[] number = {Integer.parseInt(jet.split("d")[1].split("\\+")[1]) + (int) (Math.random() + 1 * faces)};
        IntStream.range(1, Integer.parseInt(jet.split("d")[0])).forEach(i -> number[0] += (int) (Math.random() * faces));
        return number[0];
    }
}
