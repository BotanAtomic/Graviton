package graviton.database.data;

import graviton.api.Data;
import graviton.enums.Classe;
import graviton.enums.DataType;
import graviton.enums.DatabaseType;
import graviton.game.experience.Experience;
import graviton.game.spells.Spell;
import graviton.game.zone.SubZone;
import graviton.game.zone.Zone;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Botan on 25/06/2015.
 */
/** Chargement de toute les données statics sauf les Maps **/
public class StaticData extends Data<Object> {

    @Override
    public void configure() {
        super.type = DataType.STATIC;
        super.connection = super.databaseManager.getDatabases().get(DatabaseType.GAME).getConnection();
    }

    @Override
    public Object load(Object object) {
        return null;
    }

    @Override
    public boolean create(Object object) {
        return false;
    }

    @Override
    public Object getByResultSet(ResultSet result) throws SQLException {
        return null;
    }

    @Override
    public void update(Object object) {

    }

    @Override
    public void delete(Object object) {

    }

    @Override
    public boolean exist(Object object) {
        return false;
    }

    @Override
    public int getNextId() {
        return 0;
    }

    @Override
    public List<Object> loadAll(Object object) {
        try {
            locker.lock();
            String query = "SELECT * FROM zone;";
            ResultSet result = connection.createStatement().executeQuery(query);
            while (result.next())
                manager.getZones().put(result.getInt("id"), (new Zone(result.getInt("id"), result.getString("name"))));
            result.close();

            query = "SELECT * FROM subzone;";
            result = connection.createStatement().executeQuery(query);
            while (result.next())
                manager.getSubZones().put(result.getInt("id"),new SubZone(result.getInt("id"), result.getString("name"),manager.getZones().get(result.getInt("id")), result.getInt("alignement")));

            //TODO : Refaire tout ça !
            query = "SELECT * FROM class_data;";
            result = connection.createStatement().executeQuery(query);
            Map<Integer,Integer> feca = new LinkedHashMap<>();
            Map<Integer,Integer> sram = new LinkedHashMap<>();
            Map<Integer,Integer> eniripsa = new LinkedHashMap<>();
            Map<Integer,Integer> ecaflip = new LinkedHashMap<>();
            Map<Integer,Integer> cra = new LinkedHashMap<>();
            Map<Integer,Integer> iop = new LinkedHashMap<>();
            Map<Integer,Integer> sadida = new LinkedHashMap<>();
            Map<Integer,Integer> osamodas = new LinkedHashMap<>();
            Map<Integer,Integer> xelor = new LinkedHashMap<>();
            Map<Integer,Integer> pandawa = new LinkedHashMap<>();
            Map<Integer,Integer> enutrof = new LinkedHashMap<>();
            Map<Integer,Integer> sacrieur = new LinkedHashMap<>();
            int level;
            while (result.next()) {
                level = result.getInt("level");
                feca.put(level, result.getInt("feca"));
                sram.put(level, result.getInt("sram"));
                eniripsa.put(level, result.getInt("eniripsa"));
                ecaflip.put(level, result.getInt("ecaflip"));
                cra.put(level, result.getInt("cra"));
                iop.put(level, result.getInt("iop"));
                sadida.put(level, result.getInt("sadida"));
                osamodas.put(level, result.getInt("osamodas"));
                xelor.put(level, result.getInt("xelor"));
                pandawa.put(level, result.getInt("pandawa"));
                enutrof.put(level, result.getInt("enutrof"));
                sacrieur.put(level, result.getInt("sacrieur"));
            }
            manager.getClassData().put(Classe.FECA,feca);
            manager.getClassData().put(Classe.SRAM,sram);
            manager.getClassData().put(Classe.ENIRIPSA,eniripsa);
            manager.getClassData().put(Classe.ECAFLIP,ecaflip);
            manager.getClassData().put(Classe.CRA,cra);
            manager.getClassData().put(Classe.IOP,iop);
            manager.getClassData().put(Classe.SADIDA,sadida);
            manager.getClassData().put(Classe.OSAMODAS,osamodas);
            manager.getClassData().put(Classe.XELOR,xelor);
            manager.getClassData().put(Classe.PANDAWA,pandawa);
            manager.getClassData().put(Classe.ENUTROF, enutrof);
            manager.getClassData().put(Classe.SACRIEUR,sacrieur);

            query = "SELECT  * from experience";
            result = connection.createStatement().executeQuery(query);
            Map<Integer,Long> player = new HashMap<>();
            Map<Integer,Long> pvp = new HashMap<>();
            Map<Integer,Long> mount = new HashMap<>();
            Map<Integer,Long> job = new HashMap<>();
            while (result.next()) {
                level = result.getInt("level");
                player.put(level,result.getLong("player"));
                pvp.put(level,result.getLong("pvp"));
                mount.put(level,result.getLong("mount"));
                job.put(level,result.getLong("job"));
            }
            manager.setExperience(new Experience(player, job, mount, pvp));
            Map<Integer,Spell> spells = new ConcurrentHashMap<>();
            query = "SELECT * FROM spells";
            result = connection.createStatement().executeQuery(query);
            Spell spell;
            while(result.next()) {
                spell = new Spell(result.getInt("id"), result.getInt("sprite"),result.getString("spriteInfos"), result.getString("effectTarget"));
                spell.addSpellStats(1,result.getString("level1"));
                spell.addSpellStats(2,result.getString("level2"));
                spell.addSpellStats(3,result.getString("level3"));
                spell.addSpellStats(4,result.getString("level4"));
                spell.addSpellStats(5,result.getString("level5"));
                spell.addSpellStats(6, result.getString("level6"));
                spells.put(spell.getId(), spell);
            }
            result.close();
            manager.setSpells(spells);
        } catch (SQLException e) {
            console.println(e.getMessage(), true);
        } finally {
            locker.unlock();
        }
        return null;
    }
}
