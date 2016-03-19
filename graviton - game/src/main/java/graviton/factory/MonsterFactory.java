package graviton.factory;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import graviton.api.Factory;
import graviton.database.Database;
import graviton.enums.DataType;
import graviton.game.creature.monster.MonsterTemplate;
import org.jooq.Record;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static graviton.database.utils.game.Tables.MONSTERS;

/**
 * Created by Botan on 17/01/2016.
 */
public class MonsterFactory extends Factory<MonsterTemplate> {
    private final Map<Integer, MonsterTemplate> monsters;

    @Inject
    public MonsterFactory(@Named("database.game") Database database) {
        super(database);
        this.monsters = new ConcurrentHashMap<>();
    }

    @Override
    public DataType getType() {
        return DataType.MONSTER;
    }

    private MonsterTemplate load(int id) {
        Record record = database.getRecord(MONSTERS, MONSTERS.ID.equal(id));
        if (record != null) {
            MonsterTemplate template = new MonsterTemplate(record.getValue(MONSTERS.ID), record.getValue(MONSTERS.GFXID), record.getValue(MONSTERS.ALIGN), record.getValue(MONSTERS.COLORS), record.getValue(MONSTERS.GRADES), record.getValue(MONSTERS.SPELLS), record.getValue(MONSTERS.STATS));
            monsters.put(id, template);
            return template;
        }
        return null;
    }

    @Override
    public Map<Integer, MonsterTemplate> getElements() {
        return this.monsters;
    }

    @Override
    public MonsterTemplate get(Object object) {
        return monsters.get(object) == null ? load((int) object) : monsters.get(object);
    }

    @Override
    public void configure() {

    }

    @Override
    public void save() {

    }
}
