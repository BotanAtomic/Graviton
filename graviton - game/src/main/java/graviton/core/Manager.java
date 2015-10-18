package graviton.core;

import com.google.inject.Inject;
import graviton.common.Scanner;
import graviton.database.DatabaseManager;
import graviton.game.GameManager;
import graviton.game.client.player.component.CommandManager;
import graviton.game.packet.PacketManager;
import graviton.network.NetworkManager;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Botan on 16/06/2015.
 */
public class Manager {
    @Inject
    private DatabaseManager databaseManager;
    @Inject
    private NetworkManager networkManager;
    @Inject
    private GameManager gameManager;
    @Inject
    private PacketManager packetManager;
    @Inject
    private CommandManager commandManager;
    @Inject
    private Scanner scanner;
    @Getter
    private List<graviton.api.Manager> managers;


    public Manager start() {
        this.managers = asList(databaseManager, networkManager, gameManager, packetManager, scanner, commandManager);
        this.managers.forEach(graviton.api.Manager::start);
        return this;
    }

    public void stop() {
        this.managers.forEach(graviton.api.Manager::stop);
    }

    public final List<graviton.api.Manager> asList(graviton.api.Manager... a) {
        List<graviton.api.Manager> managers = new ArrayList<>();
        Collections.addAll(managers, a);
        return managers;
    }
}
