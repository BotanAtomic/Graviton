package graviton.factory;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.name.Named;
import graviton.api.Factory;
import graviton.database.Database;
import graviton.enums.DataType;
import graviton.game.action.Action;
import graviton.game.maps.Cell;
import graviton.game.maps.Maps;
import graviton.game.maps.Zaap;
import graviton.game.trunk.Trunk;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.jooq.Record;
import org.jooq.Result;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

import static graviton.database.utils.game.Tables.*;

@Slf4j
public class MapFactory extends Factory<Maps> {
    private final Map<Integer, Maps> maps;
    @Getter
    private final List<Zaap> zaaps;
    @Inject
    Injector injector;

    @Inject
    public MapFactory(@Named("database.game") Database database) {
        super(database);
        this.maps = new ConcurrentHashMap<>();
        this.zaaps = new CopyOnWriteArrayList<>();
    }

    @Override
    public DataType getType() {
        return DataType.MAPS;
    }

    @Override
    public Map<Integer, Maps> getElements() {
        return maps;
    }

    @Override
    public Maps get(Object object) {
        if (maps.containsKey(object))
            return maps.get(object);
        return load((int) object);
    }

    private Maps load(int id) {
        Record record = database.getRecord(MAPS, MAPS.ID.equal(id));
        if (record != null) {
            final Maps finalMap = new Maps(id, record, injector);
            database.getResult(CELLS, CELLS.MAP.equal(finalMap.getId())).forEach(record1 -> finalMap.getCells().get(record1.getValue(CELLS.CELL)).addAction(new Action(record1.getValue(CELLS.ACTION), record1.getValue(CELLS.ARGS))));
            this.maps.put(id, finalMap);
            loadTrunks(finalMap);
            return finalMap;
        }
        return null;
    }

    private void loadTrunks(Maps maps) {
        for (Record record : database.getResult(TRUNKS, TRUNKS.MAPID.equal(maps.getId()))) {
            Cell cell = maps.getCell(record.getValue(TRUNKS.CELLID));
            cell.setTrunk(new Trunk(record.getValue(TRUNKS.ID), record.getValue(TRUNKS.KAMAS), maps, cell, record.getValue(TRUNKS.OBJECT), injector));
        }
    }

    public void updateTrunk(Trunk trunk) {
        if (!trunk.isBank())
            database.getDSLContext().update(TRUNKS).set(TRUNKS.KAMAS, trunk.getKamas()).set(TRUNKS.OBJECT, trunk.parseToDatabase()).where(TRUNKS.ID.equal(trunk.getId())).execute();
    }

    public Maps getByPosition(int x1, int y1) {
        String position[];
        Result<Record> result = database.getResult(MAPS, MAPS.MAPPOS.contains(x1 + "," + y1));
        for (Record record : result) {
            position = record.getValue(MAPS.MAPPOS).split(",");
            try {
                if (x1 == Integer.parseInt(position[0]) && y1 == Integer.parseInt(position[1]))
                    return get(record.getValue(MAPS.ID));
            } catch (Exception e) {
                log.error("parsing result for loading map by position {}", e);
            }
        }
        return null;
    }

    @Override
    public void configure() {
        Map<Integer, Integer> allZaaps = (Map<Integer, Integer>) decodeObject("zaaps");
        this.zaaps.addAll(allZaaps.keySet().stream().map(i -> new Zaap(get(i), allZaaps.get(i))).collect(Collectors.toList()));
    }

    @Override
    public void save() {

    }
}