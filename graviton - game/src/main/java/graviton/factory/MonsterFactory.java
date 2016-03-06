package graviton.factory;

import graviton.api.Factory;

import static graviton.database.utils.game.Tables.MONSTERS;

import graviton.enums.DataType;
import graviton.enums.DatabaseType;
import graviton.game.creature.monster.MonsterTemplate;
import org.jooq.Record;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Botan on 17/01/2016.
 */
public class MonsterFactory extends Factory<MonsterTemplate> {
    private final Map<Integer, MonsterTemplate> monsters;

    public MonsterFactory() {
        super(DatabaseType.GAME);
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
        configureDatabase();
    }

    @Override
    public void save() {

    }
}
