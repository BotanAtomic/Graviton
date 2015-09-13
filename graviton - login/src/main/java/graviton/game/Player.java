package graviton.game;

import graviton.login.Login;
import graviton.login.Main;
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
        Main.getInstance(Login.class).getPlayers().put(id, this);
    }

    public Player(int server) {
        this.server = server;
    }

}
