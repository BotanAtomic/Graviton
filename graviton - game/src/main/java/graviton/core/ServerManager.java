package graviton.core;

import com.google.inject.Inject;
import graviton.api.Manager;
import graviton.console.Console;
import graviton.database.DatabaseManager;
import graviton.game.manager.GameManager;
import graviton.game.packet.PacketManager;
import graviton.network.NetworkManager;
import lombok.Getter;

import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Botan on 16/06/2015.
 */
@Singleton
public class ServerManager {
    @Inject
    private DatabaseManager databaseManager;
    @Inject
    private NetworkManager networkManager;
    @Inject
    private GameManager gameManager;
    @Inject
    private PacketManager packetManager;

    @Getter
    private List<Manager> managers;
    @Getter
    private Configuration configuration;
    @Getter
    private boolean running;
    @Getter
    private Console console;

    @Inject
    public ServerManager(Configuration configuration, Console console) {
        this.running = false;
        this.configuration = configuration;
        this.console = console;
    }

    public ServerManager configure() {
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
        Collections.addAll(managers, a);
        return managers;
    }
}
