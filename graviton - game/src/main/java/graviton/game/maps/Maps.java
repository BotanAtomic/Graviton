package graviton.game.maps;

import graviton.common.Hash;
import graviton.game.client.player.Player;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;


/**
 * Created by Botan on 22/06/2015.
 */
@Data

public class Maps {
    private int id;
    private long date;
    private int width, heigth;
    private String places, key, data;
    private Map<Integer, Cell> cells;
    private Map<Integer, Player> players;
    private ReentrantLock locker;

    private String descriptionMapMessage;
    private String loadingMapMessage;

    public Maps(int id, long date, int width, int heigth, String places, String key, String data) {
        this.id = id;
        this.date = date;
        this.width = width;
        this.heigth = heigth;
        this.places = places;
        this.key = key;
        this.data = data;
        this.players = new ConcurrentHashMap<>();
        this.cells = decompileCells(data);
        this.locker = new ReentrantLock();
        this.loadingMapMessage =  "GA;2;" + this.getId() + ";";
        this.descriptionMapMessage = "GDM|"+id+"|0"+date+"|"+(!key.isEmpty() ? key : data);
    }

    public Cell getCell(int id) {
        return cells.get(id);
    }

    public void addPlayer(Player player) {
        players.put(player.getId(),player);
    }

    public void removePlayer(Player player) {
        if (this.players.containsKey(player.getId())) {
            this.players.remove(player);
            player.getPosition().getCell().getPlayers().remove(player);
        }
    }

    private Map<Integer, Cell> decompileCells(String data) {
        Map<Integer, Cell> cells = new ConcurrentHashMap<>();
        String cellData;
        List<Byte> cellInfos = new ArrayList<>();
        for (int f = 0; f < data.length(); f += 10) {
            cellData = data.substring(f, f + 10);
            for (int i = 0; i < cellData.length(); i++)
                cellInfos.add((byte) getHashValue(cellData.charAt(i)));
            boolean walkable = (cellInfos.get(2) & 56) >> 3 != 0;
            cells.put(f / 10, new Cell(f / 10, this, walkable));
            cellInfos.clear();
        }
        return cells;
    }

    private int getHashValue(char value) {
        char[] hash = Hash.HASH;
        for (int a = 0; a <= hash.length; a++)
            if (hash[a] == value)
                return a;
        return -1;
    }

    public void send(String packet) {
        locker.lock();
        players.values().forEach(player -> player.send(packet));
        locker.unlock();
    }


}
