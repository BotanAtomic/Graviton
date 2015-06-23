package graviton.game;

import lombok.Getter;

/**
 * Created by Botan on 07/06/2015.
 */
public class Player {
    @Getter private int id,server;
    @Getter
    private String name;

    public Player(int id, String name, int server) {
        this.id = id;
        this.name = name;
        this.server = server;
    }

}
