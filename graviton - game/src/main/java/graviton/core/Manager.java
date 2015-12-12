package graviton.core;

import com.google.inject.Inject;
import graviton.common.Scanner;
import graviton.database.DatabaseManager;
import graviton.game.GameManager;
import graviton.game.client.player.component.CommandManager;
import graviton.game.PacketManager;
import graviton.network.NetworkManager;
import lombok.Getter;

import java.util.*;

/**
 * Created by Botan on 16/06/2015.
 */
public class Manager {
    @Getter
    private final List<graviton.api.Manager> managers;

    @Getter
    final private Date dateOfStart;

    @Inject
    public Manager(DatabaseManager databaseManager,NetworkManager networkManager,GameManager gameManager,PacketManager packetManager,CommandManager commandManager, Scanner scanner) {
        this.dateOfStart = new Date();
        managers = Arrays.asList( databaseManager, networkManager, gameManager, packetManager, commandManager, scanner);
    }

    public void start() {
        this.managers.forEach(graviton.api.Manager::load);
        Runtime.getRuntime().addShutdownHook(new Thread(this::stop));
    }

    public void stop() {
        this.managers.forEach(graviton.api.Manager::unload);
    }

}
