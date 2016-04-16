package graviton.game.object;

import com.google.inject.Inject;
import com.google.inject.Injector;
import graviton.common.Pair;
import graviton.common.Parameter;
import graviton.factory.ObjectFactory;
import graviton.game.GameManager;
import graviton.game.statistics.Statistics;
import lombok.Data;

import java.util.Map;
import java.util.TreeMap;

/**
 * Created by Botan on 21/06/2015.
 */
@Data
public class Object {
    @Inject
    Injector injector;

    @Inject
    GameManager manager;

    @Inject
    ObjectFactory objectFactory;

    private final int id;
    private final ObjectTemplate template;

    private int quantity;

    private Pair<ObjectPosition,Integer> position;

    private Statistics statistics;

    public Object(int id, int template, int quantity, int position, String statistics, Injector injector) {
        injector.injectMembers(this);
        this.id = id;
        this.template = manager.getObjectTemplate(template);
        this.position = new Pair<>(ObjectPosition.get(position),position > 15 ? position : 0);
        this.quantity = quantity;
        this.statistics = this.template.getStatistics(statistics, true);
    }

    public Object(int id, int template, int quantity, ObjectPosition position, Statistics statistics, Injector injector) {
        injector.injectMembers(this);
        this.id = id;
        this.template = manager.getObjectTemplate(template);
        this.quantity = quantity;
        this.position = new Pair<>(position,0);
        this.statistics = statistics;
    }

    public Object getClone(int quantity, boolean create) {
        Object object = new Object(objectFactory.getNextId(), template.getId(), quantity, ObjectPosition.NO_EQUIPED, this.statistics, injector);
        if (create)
            objectFactory.create(object);
        return object;
    }

    public void changePlace(ObjectPosition newPlace, int shortcut) {
        position.setKey(newPlace);
        if(shortcut != 0)
            position.setValue(shortcut);
        objectFactory.update(this);
    }

    public void update() {
        objectFactory.update(this);
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
        objectFactory.update(this);
    }

    public String parseItem() {
        StringBuilder builder = new StringBuilder();
        String position = this.position.getKey() == ObjectPosition.NO_EQUIPED ? this.position.getValue() != 0 ? Integer.toHexString(this.position.getValue()) : "-1" : Integer.toHexString(this.position.getKey().id);
        builder.append(Integer.toHexString(this.getId())).append("~").append(Integer.toHexString(this.getTemplate().getId())).append("~").append(Integer.toHexString(this.getQuantity())).append("~").append(position).append("~").append(this.parseEffects()).append(";");
        return builder.toString();
    }

    public String parseEffects() {
        final StringBuilder builder = new StringBuilder();
        final java.lang.Object[] value = {null};

        this.statistics.getEffects().keySet().forEach(i -> {
            value[0] = this.statistics.getEffect(i);
            builder.append(Integer.toHexString(i)).append("#").append(Integer.toHexString((int) value[0])).append("#0#0#").append("0d0+" + value[0]).append(",");
        });

        this.statistics.getOptionalEffect().keySet().forEach(i -> {
            Parameter parameter = (Parameter) this.statistics.getOptionalEffect(i);
            builder.append(Integer.toHexString(i)).append("#").append(Integer.toHexString((int)parameter.getFirst())).append("#").append(Integer.toHexString((int)parameter.getSecond())).append("#").append(Integer.toHexString((int)parameter.getThird())).append("#").append(parameter.getFourth()).append(",");
        });

        return builder.toString().substring(0, builder.length() -1);
    }
}
