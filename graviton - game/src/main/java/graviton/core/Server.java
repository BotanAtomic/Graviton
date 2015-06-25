package graviton.core;

import com.google.inject.Inject;
import graviton.api.Manager;
import graviton.console.Console;
import graviton.database.DatabaseManager;
import graviton.game.GameManager;
import graviton.game.packet.PacketManager;
import graviton.network.NetworkManager;
import lombok.Getter;

import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Botan on 16/06/2015.
 */
@Singleton
public class Server {
    @Inject
    DatabaseManager databaseManager;
    @Inject
    NetworkManager networkManager;
    @Inject
    GameManager gameManager;
    @Inject
    PacketManager packetManager;

    @Getter
    private List<Manager> managers;
    @Getter
    private Configuration configuration;
    @Getter
    private boolean running;
    @Getter
    private Console console;

    @Inject
    public Server(Configuration configuration, Console console) {
        this.running = false;
        this.configuration = configuration;
        this.console = console;
       }

    public Server configure() {
        this.managers = getNewList(databaseManager, networkManager, gameManager, packetManager);
        this.managers.forEach(Manager::configure);
        this.running = true;
        console.initialize(this);
        return this;
    }

    public void stop() {
        this.running = false;
        this.managers.forEach(Manager::stop);
    }

    public List<Manager> getNewList(Manager... a) {
        List<Manager> managers = new ArrayList<>();
        for(Manager manager : a)
            managers.add(manager);
        return managers;
    }
}
