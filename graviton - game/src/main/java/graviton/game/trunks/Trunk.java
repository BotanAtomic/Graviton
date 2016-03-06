package graviton.game.trunks;

import com.google.inject.Inject;
import com.google.inject.Injector;
import graviton.game.GameManager;
import graviton.game.client.player.Player;
import graviton.game.maps.Cell;
import graviton.game.maps.Maps;
import graviton.game.object.Object;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Botan on 27/02/2016.
 */

@Data
public class Trunk {
    @Inject
    GameManager gameManager;

    private final int id;
    private final Map<Integer, Object> objects;

    private final Maps maps;
    private final Cell cell;

    private long kamas;

    private boolean inUse = false;
    private boolean isBank = false;

    public Trunk(int id, long kamas, Maps maps, Cell cell, String objects, Injector injector) {
        injector.injectMembers(this);
        this.id = id;
        this.kamas = kamas;
        this.maps = maps;
        this.cell = cell;
        this.objects = getObjectList(objects);
    }

    public Trunk(String data, Injector injector) {
        injector.injectMembers(this);
        this.id = 0;
        this.isBank = true;
        this.maps = null;
        this.cell = null;

        if (data == null || data.isEmpty()) {
            this.objects = new HashMap<>();
            return;
        }
        long kamas = Long.parseLong(data.split(";")[0]);
        this.objects = getObjectList(data.split(";")[1]);
        this.kamas = kamas;
    }

    public void changeKamas(long kamas) {
        this.kamas += kamas;
    }

    public void open(Player player) {
        if (inUse) {
            player.send("Im120");
            return;
        }

        player.send("ECK5");
        player.send("EL" + getPacket());
        this.inUse = true;
    }

    private Map<Integer, Object> getObjectList(String objects) {
        if (objects == null || objects.isEmpty()) return new HashMap<>();

        Map<Integer, Object> objectsList = new HashMap<>();
        for (String data : objects.split(",")) {
            Object object = gameManager.getObject(Integer.parseInt(data));
            if (object != null)
                objectsList.put(object.getId(), object);
        }
        return objectsList;
    }

    public String parseToDatabase() {
        StringBuilder builder = new StringBuilder();
        objects.values().forEach(object -> builder.append(object.getId()).append(","));
        return builder.toString();
    }

    private String getPacket() {
        StringBuilder packet = new StringBuilder();
        for (Object object : this.objects.values())
            packet.append("O").append(object.parseItem()).append(";");
        if (this.kamas != 0)
            packet.append("G").append(this.kamas);
        return packet.toString();
    }
}
