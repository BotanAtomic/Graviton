package graviton.factory;

import graviton.api.Factory;
import graviton.enums.DataType;
import graviton.enums.DatabaseType;
import graviton.game.spells.SpellTemplate;
import lombok.extern.slf4j.Slf4j;
import org.jooq.Record;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static graviton.database.utils.game.Tables.SPELLS;

/**
 * Created by Botan on 17/01/2016.
 */
@Slf4j
public class SpellFactory extends Factory<SpellTemplate> {

    private Map<Integer, SpellTemplate> spells;

    public SpellFactory() {
        super(DatabaseType.GAME);
    }

    @Override
    public Map<Integer, SpellTemplate> getElements() {
        return spells;
    }

    @Override
    public SpellTemplate get(Object object) {
        return spells.get(object);
    }

    public void configure() {
        super.configureDatabase();

        this.spells = new ConcurrentHashMap<>();

        for(Record record : database.getResult(SPELLS))
            spells.put(record.getValue(SPELLS.ID),  new SpellTemplate(record.getValue(SPELLS.ID), record.getValue(SPELLS.SPRITE), record.getValue(SPELLS.SPRITEINFOS), record));
    }

    @Override
    public DataType getType() {
        return DataType.SPELL;
    }

    @Override
    public void save() {


    }
}
