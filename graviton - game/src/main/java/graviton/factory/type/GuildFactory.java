package graviton.factory.type;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import graviton.api.Factory;
import graviton.database.Database;
import graviton.enums.DataType;
import graviton.factory.FactoryManager;
import graviton.game.GameManager;
import graviton.game.guild.Guild;
import graviton.game.guild.GuildMember;
import lombok.extern.slf4j.Slf4j;
import org.jooq.Record;
import org.jooq.Result;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static graviton.database.utils.game.Tables.GUILDS;
import static graviton.database.utils.game.Tables.GUILD_MEMBERS;


@Slf4j
public class GuildFactory extends Factory<Guild> {
    @Inject
    GameManager gameManager;

    private Map<Integer, Guild> guilds;
    private Map<Integer, GuildMember> guildMembers;

    @Inject
    public GuildFactory(@Named("database.game") Database database, FactoryManager factoryManager) {
        super(database);
        factoryManager.addFactory(this);
        this.guilds = new ConcurrentHashMap<>();
        this.guilds = new ConcurrentHashMap<>();
    }

    public boolean check(String arguments, boolean name) {
        Record record = database.getRecord(GUILDS, name ? GUILDS.NAME.equal(arguments) : GUILDS.EMBLEM.equal(arguments));
        return record == null;
    }

    public void createGuild(Guild guild, GuildMember member) {
        database.getDSLContext().insertInto(GUILDS, GUILDS.ID, GUILDS.NAME, GUILDS.EMBLEM, GUILDS.LEVEL, GUILDS.EXPERIENCE,
                GUILDS.CAPITAL, GUILDS.MAX, GUILDS.SPELL, GUILDS.STATS).values(guild.getId(), guild.getName(), guild.getEmblem(), 1, (long) 0, 0, 0, "", "").execute();
        database.getDSLContext().insertInto(GUILD_MEMBERS, GUILD_MEMBERS.ID, GUILD_MEMBERS.GUILD, GUILD_MEMBERS.NAME, GUILD_MEMBERS.LEVEL,
                GUILD_MEMBERS.GFX, GUILD_MEMBERS.RANK, GUILD_MEMBERS.XPGIVE, GUILD_MEMBERS.XPGAVE, GUILD_MEMBERS.RIGHTS, GUILD_MEMBERS.ALIGN, GUILD_MEMBERS.LASTCONNECTION)
                .values(member.getId(), member.getGuild().getId(), member.getName(), member.getLevel(), member.getGfx(), member.getRank(), (short) member.getExperienceGive(),
                        member.getExperienceGave(), member.getRight(), member.getAlign(), member.getLastConnection()).execute();

        this.guilds.put(guild.getId(), guild);
    }

    public int getNextId() {
        return database.getNextId(GUILDS, GUILDS.ID);
    }

    @Override
    public DataType getType() {
        return DataType.GUILD;
    }

    @Override
    public Map<Integer, Guild> getElements() {
        return this.guilds;
    }

    public Guild get(int object) {
        if (this.guilds.containsKey(object))
            return this.guilds.get(object);
        else
            return load(object);
    }

    @Override
    public void configure() {

    }

    private Guild load(int id) {
        Record found = database.getRecord(GUILDS, GUILDS.ID.equal(id));
        Guild guild = new Guild(found.getValue(GUILDS.ID), found.getValue(GUILDS.NAME), found.getValue(GUILDS.EMBLEM), found.getValue(GUILDS.LEVEL), found.getValue(GUILDS.EXPERIENCE), found.getValue(GUILDS.CAPITAL));

        database.getResult(GUILD_MEMBERS, GUILD_MEMBERS.GUILD.equal(id)).forEach(record -> guild.addMember(new GuildMember(record.getValue(GUILD_MEMBERS.ID), guild, record.getValue(GUILD_MEMBERS.NAME), record.getValue(GUILD_MEMBERS.ALIGN), record.getValue(GUILD_MEMBERS.GFX), record.getValue(GUILD_MEMBERS.LEVEL), record.getValue(GUILD_MEMBERS.LASTCONNECTION), record.getValue(GUILD_MEMBERS.RANK), record.getValue(GUILD_MEMBERS.RIGHTS))));
        this.guilds.put(id, guild);
        return guild;
    }

    @Override
    public void save() {

    }
}