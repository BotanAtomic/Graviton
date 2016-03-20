package graviton.factory;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import graviton.api.Factory;
import graviton.database.Database;
import graviton.enums.DataType;
import graviton.game.spells.Animation;
import graviton.game.spells.SpellTemplate;
import lombok.Getter;
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

    @Getter
    private final Map<Integer, Animation> animations;

    private final Map<Integer, SpellTemplate> spells;

    @Inject
    public SpellFactory(@Named("database.game") Database database) {
        super(database);
        this.spells = new ConcurrentHashMap<>();
        this.animations = new ConcurrentHashMap<>();
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
        for (Object object : decodeObjects("animations")) {
            Animation animation = (Animation) object;
            this.animations.put(animation.getId(), animation);
        }
        for(Record record : database.getResult(SPELLS))
            this.spells.put(record.getValue(SPELLS.ID), new SpellTemplate(record.getValue(SPELLS.ID), record.getValue(SPELLS.SPRITE), record.getValue(SPELLS.SPRITEINFOS), record));
    }

    @Override
    public DataType getType() {
        return DataType.SPELL;
    }

    @Override
    public void save() {


    }
}
