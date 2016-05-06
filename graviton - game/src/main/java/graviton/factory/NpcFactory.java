package graviton.factory;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.name.Named;
import graviton.api.Factory;
import graviton.database.Database;
import graviton.enums.DataType;
import graviton.game.action.Action;
import graviton.game.creature.npc.Npc;
import graviton.game.creature.npc.NpcAnswer;
import graviton.game.creature.npc.NpcQuestion;
import graviton.game.creature.npc.NpcTemplate;
import graviton.game.maps.Maps;
import lombok.extern.slf4j.Slf4j;
import org.jooq.Record;
import org.jooq.Result;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static graviton.database.utils.game.Tables.*;

/**
 * Created by Botan on 17/01/2016.
 */
@Slf4j
public class NpcFactory extends Factory<NpcTemplate> {
    private final Map<Integer, NpcTemplate> templates;
    private final Map<Integer, NpcQuestion> questions;
    private final Map<Integer, NpcAnswer> answers;
    @Inject
    Injector injector;

    @Inject
    public NpcFactory(@Named("database.game") Database database) {
        super(database);
        this.templates = new ConcurrentHashMap<>();
        this.answers = new ConcurrentHashMap<>();
        this.questions = new ConcurrentHashMap<>();
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
            npcTemplate = new NpcTemplate(id, record.getValue(NPC_TEMPLATE.GFX), record.getValue(NPC_TEMPLATE.SEX), colors, record.getValue(NPC_TEMPLATE.ACCESSORIES), record.getValue(NPC_TEMPLATE.EXTRACLIP), record.getValue(NPC_TEMPLATE.CUSTOMARTWORK), Integer.parseInt(record.getValue(NPC_TEMPLATE.INITQUESTION)));
            this.templates.put(id, npcTemplate);
        }
        return npcTemplate;
    }

    public List<Npc> getNpcOnMap(Maps maps) {
        return database.getResult(NPCS, NPCS.MAP.equal(maps.getId())).stream().map(record -> new Npc(record.getValue(NPCS.TEMPLATE), maps, record.getValue(NPCS.CELL), record.getValue(NPCS.ORIENTATION), injector)).collect(Collectors.toList());
    }

    @Override
    public NpcTemplate get(Object object) {
        if (templates.containsKey(object))
            return templates.get(object);
        return load((int) object);
    }

    public NpcQuestion getQuestion(int id) {
        if (this.questions.containsKey(id))
            return this.questions.get(id);
        Record record = database.getRecord(NPC_QUESTIONS, NPC_QUESTIONS.ID.equal(id));
        if (record != null) {
            NpcQuestion question = new NpcQuestion(id, record.getValue(NPC_QUESTIONS.RESPONSES), record.getValue(NPC_QUESTIONS.PARAMS), record.getValue(NPC_QUESTIONS.COND), Integer.parseInt(record.getValue(NPC_QUESTIONS.IFFALSE)));
            this.questions.put(id, question);
            return question;
        }
        return null;
    }

    public NpcAnswer getAnswer(int id) {
        if (this.answers.containsKey(id))
            return this.answers.get(id);

        Result<Record> result = database.getResult(NPC_REPONSES_ACTIONS, NPC_REPONSES_ACTIONS.ID.equal(id));
        NpcAnswer answer = new NpcAnswer(id);
        this.answers.put(id, answer);
        for (Record record : result)
            answer.addAction(new Action(record.getValue(NPC_REPONSES_ACTIONS.TYPE), record.getValue(NPC_REPONSES_ACTIONS.ARGS)));
        return answer;
    }

    @Override
    public void configure() {

    }

    @Override
    public void save() {

    }
}
