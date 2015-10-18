package graviton.game;

import graviton.login.Main;
import graviton.login.Manager;
import lombok.Data;

/**
 * Created by Botan on 07/06/2015.
 */
@Data
public class Player {
    private int id, server;
    private String name;

    public Player(int id, String name, int server) {
        this.id = id;
        this.name = name;
        this.server = server;
        Main.getInstance(Manager.class).getPlayers().put(id, this);
    }

    public Player(int server) {
        this.server = server;
    }

}
