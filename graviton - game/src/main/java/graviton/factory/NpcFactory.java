package graviton.factory;

import graviton.api.Factory;
import graviton.enums.DataType;
import graviton.enums.DatabaseType;
import graviton.game.creature.npc.NpcTemplate;
import lombok.extern.slf4j.Slf4j;
import org.jooq.Record;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static graviton.database.utils.game.Tables.NPC_TEMPLATE;

/**
 * Created by Botan on 17/01/2016.
 */
@Slf4j
public class NpcFactory extends Factory<NpcTemplate> {
    private final Map<Integer, NpcTemplate> templates;

    public NpcFactory() {
        super(DatabaseType.GAME);
        this.templates = new ConcurrentHashMap<>();
    }

    @Override
    public DataType getType() {
        return DataType.NPC;
    }

    @Override
    public Map<Integer, NpcTemplate> getElements() {
        return templates;
    }

    private NpcTemplate load(int id) {
        NpcTemplate npcTemplate = null;
        Record record = database.getRecord(NPC_TEMPLATE, NPC_TEMPLATE.ID.equal(id));
        if (record != null) {
            int[] colors = {record.getValue(NPC_TEMPLATE.COLOR1), record.getValue(NPC_TEMPLATE.COLOR2), record.getValue(NPC_TEMPLATE.COLOR3)};
            npcTemplate = new NpcTemplate(id, record.getValue(NPC_TEMPLATE.GFX), record.getValue(NPC_TEMPLATE.SEX), colors, record.getValue(NPC_TEMPLATE.ACCESSORIES), record.getValue(NPC_TEMPLATE.EXTRACLIP), record.getValue(NPC_TEMPLATE.CUSTOMARTWORK));
            this.templates.put(id, npcTemplate);
        }
        return npcTemplate;
    }

    @Override
    public NpcTemplate get(Object object) {
        if (templates.containsKey(object))
            return templates.get(object);
        return load((int) object);
    }

    @Override
    public void configure() {
        super.configureDatabase();
    }
}
