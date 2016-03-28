package graviton.game;

import com.google.inject.Inject;
import com.google.inject.Injector;
import graviton.core.Manager;
import lombok.Data;

/**
 * Created by Botan on 07/06/2015.
 */
@Data
public class Player {
    @Inject
    Manager manager;

    private int id, server;
    private String name;

    public Player(int id, String name, int server,Injector injector) {
        injector.injectMembers(this);
        this.id = id;
        this.name = name;
        this.server = server;
        manager.getPlayers().put(id, this);
    }

    public Player(int server) {
        this.server = server;
    }

}
