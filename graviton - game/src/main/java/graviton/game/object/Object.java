package graviton.game.object;

import com.google.inject.Inject;
import com.google.inject.Injector;
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

    private ObjectPosition position;

    private Statistics statistics;
    private Map<Integer, String> stringStats;

    public Object(int id, int template, int quantity, int position, String statistics,Injector injector) {
        injector.injectMembers(this);
        this.id = id;
        this.template = manager.getObjectTemplate(template);
        this.position = ObjectPosition.get(position);
        this.quantity = quantity;
        this.statistics = new Statistics();
        this.stringStats = new TreeMap<>();
        this.parseStringToStats(statistics);
    }

    public Object(int id, int template, int quantity, ObjectPosition position, Statistics statistics,Injector injector) {
        injector.injectMembers(this);
        this.id = id;
        this.template = manager.getObjectTemplate(template);
        this.quantity = quantity;
        this.position = position;
        this.statistics = statistics;
        this.stringStats = new TreeMap<>();
    }

    public Object getClone(int quantity,boolean create) {
        Object object =  new Object(objectFactory.getNextId(),template.getId(),quantity,ObjectPosition.NO_EQUIPED,this.statistics,injector);
        if(create)
            objectFactory.create(object);
        return object;
    }

    public void changePlace(ObjectPosition newPlace) {
        this.position = newPlace;
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
        String position = this.getPosition() == ObjectPosition.NO_EQUIPED ? "" : Integer.toHexString(this.getPosition().id);
        builder.append(Integer.toHexString(this.getId())).append("~").append(Integer.toHexString(this.getTemplate().getId())).append("~").append(Integer.toHexString(this.getQuantity())).append("~").append(position).append("~").append(this.parseEffects()).append(";");
        return builder.toString();
    }

    public String parseEffects() {
        StringBuilder builder = new StringBuilder();
        boolean isFirst = true;

        for (Map.Entry<Integer, Integer> entry : this.statistics.getEffects().entrySet()) {
            if (!isFirst)
                builder.append(",");
            String jet = "0d0+" + entry.getValue();
            builder.append(Integer.toHexString(entry.getKey())).append("#").append(Integer.toHexString(entry.getValue())).append("#0#0#").append(jet);
            isFirst = false;
        }

        for (Map.Entry<Integer, String> entry : stringStats.entrySet()) {
            if (!isFirst)
                builder.append(",");
            builder.append(Integer.toHexString(entry.getKey())).append("#0#0#0#").append(entry.getValue());
            isFirst = false;
        }
        return builder.toString();
    }

    public void parseStringToStats(String statistics) {
        if (statistics.equals("")) return;
        for (String s : statistics.split(",")) {
            try {
                String[] stats = s.split("#");
                int id = Integer.parseInt(stats[0], 16);
                if (id == 997 || id == 996) {
                    stringStats.put(id, stats[4]);
                    continue;
                }
                if ((!stats[3].equals("") && !stats[3].equals("0"))) {
                    stringStats.put(id, stats[3]);
                    continue;
                }
                boolean follow1 = true;
                switch (id) {
                    case 110:
                    case 139:
                    case 605:
                    case 614:
                        follow1 = false;
                        break;
                }
                if (!follow1)
                    continue;

                boolean follow2 = true;
                for (int a : template.getSwordEffectId())
                    if (a == id)
                        follow2 = false;

                if (!follow2)
                    continue;
                this.statistics.addEffect(id, Integer.parseInt(stats[1], 16));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }
}
