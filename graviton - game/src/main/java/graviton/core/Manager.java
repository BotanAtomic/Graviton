package graviton.core;

import com.google.inject.Inject;
import graviton.common.Scanner;
import graviton.game.GameManager;
import graviton.network.PacketManager;
import graviton.game.client.player.component.CommandManager;
import graviton.network.NetworkManager;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;

/**
 * Created by Botan on 16/06/2015.
 */
public class Manager {
    @Getter
    final private List<graviton.api.Manager> managers;

    @Inject
    public Manager(NetworkManager networkManager, GameManager gameManager, PacketManager packetManager, CommandManager commandManager, Scanner scanner) {
        managers = Arrays.asList(gameManager, packetManager, commandManager, scanner, networkManager);
    }

    public Manager start() {
        this.managers.forEach(graviton.api.Manager::load);
        return this;
    }

    public void stop() {
        this.managers.forEach(graviton.api.Manager::unload);
    }

}
