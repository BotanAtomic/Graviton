package graviton.factory;

import com.google.inject.Inject;
import com.google.inject.Injector;
import graviton.api.Factory;
import graviton.enums.DataType;
import graviton.enums.DatabaseType;
import graviton.game.maps.object.InteractiveObjectTemplate;
import graviton.game.object.Object;
import graviton.game.object.ObjectTemplate;
import lombok.extern.slf4j.Slf4j;
import org.jooq.Record;
import org.jooq.Result;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static graviton.database.utils.game.Tables.*;

/**
 * Created by Botan on 28/12/2015.
 */

@Slf4j
public class ObjectFactory extends Factory<ObjectTemplate> {
    @Inject
    Injector injector;

    private final Map<Integer, ObjectTemplate> objectsTemplate;
    private final Map<Integer, InteractiveObjectTemplate> interactiveObjectTemplate;


    public ObjectFactory() {
        super(DatabaseType.GAME);
        this.objectsTemplate = new ConcurrentHashMap<>();
        this.interactiveObjectTemplate = new ConcurrentHashMap<>();
    }

    public Object load(int id) {
        Object object = null;
        Record record = database.getRecord(ITEMS, ITEMS.ID.equal(id));

        if (record != null)
            object = new Object(id, record.getValue(ITEMS.TEMPLATE), record.getValue(ITEMS.QUANTITY), record.getValue(ITEMS.POSITION), record.getValue(ITEMS.STATS), injector);

        return object;
    }

    public void create(Object object) {
        database.getDSLContext()
                .insertInto(ITEMS, ITEMS.ID, ITEMS.TEMPLATE, ITEMS.QUANTITY, ITEMS.POSITION, ITEMS.STATS)
                .values(object.getId(), object.getTemplate().getId(), object.getQuantity(), object.getPosition().id,
                        object.parseEffects()).execute();
    }

    public void update(Object object) {
        database.getDSLContext().update(ITEMS).set(ITEMS.QUANTITY, object.getQuantity()).set(ITEMS.POSITION, object.getPosition().id)
                .set(ITEMS.STATS, object.parseEffects()).where(ITEMS.ID.equal(object.getId())).execute();
    }

    public int getNextId() {
        return database.getNextId(ITEMS, ITEMS.ID);
    }

    public void remove(int id) {
        database.remove(ITEMS, ITEMS.ID.equal(id));
    }

    private ObjectTemplate loadTemplate(int id) {
        Record record = database.getRecord(ITEM_TEMPLATE, ITEM_TEMPLATE.ID.equal(id));

        if (record != null)
            return get(record);

        return null;
    }

    private ObjectTemplate get(Record record) {
        return new ObjectTemplate(record.getValue(ITEM_TEMPLATE.ID), record.getValue(ITEM_TEMPLATE.TYPE), record.getValue(ITEM_TEMPLATE.NAME),
                record.getValue(ITEM_TEMPLATE.PANOPLIE), record.getValue(ITEM_TEMPLATE.STATSTEMPLATE), record.getValue(ITEM_TEMPLATE.POD),
                record.getValue(ITEM_TEMPLATE.PANOPLIE), record.getValue(ITEM_TEMPLATE.PRICE), record.getValue(ITEM_TEMPLATE.CONDITION),
                record.getValue(ITEM_TEMPLATE.ARMEINFOS), injector);
    }

    @Override
    public Map<Integer, ObjectTemplate> getElements() {
        return this.objectsTemplate;
    }

    @Override
    public ObjectTemplate get(java.lang.Object object) {
        if (!this.objectsTemplate.containsKey(object))
            return loadTemplate((int) object);
        return this.objectsTemplate.get(object);
    }

    public InteractiveObjectTemplate get(int id) {
        return interactiveObjectTemplate.get(id);
    }

    @Override
    public void configure() {
        super.configureDatabase();

        Result<Record> result = database.getResult(INTERACTIVE_TEMPLATE);

        for (Record record : result)
            this.interactiveObjectTemplate.put(record.getValue(INTERACTIVE_TEMPLATE.ID), new InteractiveObjectTemplate(record.getValue(INTERACTIVE_TEMPLATE.ID),
                    record.getValue(INTERACTIVE_TEMPLATE.RESPAWN), record.getValue(INTERACTIVE_TEMPLATE.DURATION),
                    record.getValue(INTERACTIVE_TEMPLATE.UNKNOW), record.getValue(INTERACTIVE_TEMPLATE.WALKABLE) == 1));
    }

    @Override
    public DataType getType() {
        return DataType.OBJECT;
    }

    @Override
    public void save() {

    }
}
