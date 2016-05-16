package graviton.factory.type;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import graviton.api.Factory;
import graviton.database.Database;
import graviton.enums.DataType;
import graviton.factory.FactoryManager;
import graviton.game.GameManager;
import graviton.game.guild.Guild;
import lombok.extern.slf4j.Slf4j;
import org.jooq.Record;

import java.util.Map;

import static graviton.database.utils.game.Tables.GUILD;

@Slf4j
public class GuildFactory extends Factory<Guild> {
    @Inject
    GameManager gameManager;

    private Map<Integer, Guild> guilds;

    @Inject
    public GuildFactory(@Named("database.game") Database database,FactoryManager factoryManager) {
        super(database);
        factoryManager.addFactory(this);
    }

    public boolean check(String arguments, boolean name) {
        Record record = database.getRecord(GUILD, name ? GUILD.NAME.equal(arguments) : GUILD.EMBLEM.equal(arguments));
        return record == null;
    }


    @Override
    public DataType getType() {
        return DataType.GUILD;
    }

    @Override
    public Map<Integer, Guild> getElements() {
        return this.guilds;
    }

    public Guild get(Object object) {
        return null;
    }

    @Override
    public void configure() {
    }

    @Override
    public void save() {

    }
}