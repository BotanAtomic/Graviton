package graviton.factory;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.name.Named;
import graviton.api.Factory;
import graviton.database.Database;
import graviton.enums.DataType;
import graviton.game.maps.object.InteractiveObjectTemplate;
import graviton.game.object.Object;
import graviton.game.object.ObjectTemplate;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.jooq.Record;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static graviton.database.utils.game.Tables.ITEMS;
import static graviton.database.utils.game.Tables.ITEM_TEMPLATE;

/**
 * Created by Botan on 28/12/2015.
 */

@Slf4j
public class ObjectFactory extends Factory<ObjectTemplate> {
    private final Map<Integer, ObjectTemplate> objectsTemplate;
    private final Map<Integer, InteractiveObjectTemplate> interactiveObjectTemplate;
    @Inject
    Injector injector;
    @Getter
    private List<Integer> effects;

    @Inject
    public ObjectFactory(@Named("database.game") Database database) {
        super(database);
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
                .values(object.getId(), object.getTemplate().getId(), object.getQuantity(), object.getShortcut() != 0 ? object.getShortcut() : object.getObjectPosition().id, object.parseEffects()).execute();
    }

    public void update(Object object) {
        database.getDSLContext().update(ITEMS).set(ITEMS.QUANTITY, object.getQuantity()).set(ITEMS.POSITION, object.getShortcut() != 0 ? object.getShortcut() : object.getObjectPosition().id)
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
                record.getValue(ITEM_TEMPLATE.LEVEL), record.getValue(ITEM_TEMPLATE.STATSTEMPLATE), record.getValue(ITEM_TEMPLATE.POD),
                record.getValue(ITEM_TEMPLATE.PRICE), record.getValue(ITEM_TEMPLATE.CONDITION),
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
        for (java.lang.Object object : decodeObjects("objects/interactive")) {
            InteractiveObjectTemplate template = (InteractiveObjectTemplate) object;
            this.interactiveObjectTemplate.put(template.getId(), template);
        }
        this.effects = (ArrayList<Integer>) decodeObject("objects/effects");
    }

    @Override
    public DataType getType() {
        return DataType.OBJECT;
    }

    @Override
    public void save() {

    }
}
