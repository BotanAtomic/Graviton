package graviton.core;

import com.google.inject.Inject;
import graviton.common.Scanner;
import graviton.game.GameManager;
import graviton.game.PacketManager;
import graviton.game.client.player.component.CommandManager;
import graviton.game.fight.type.FightManager;
import graviton.network.NetworkManager;
import lombok.Getter;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Created by Botan on 16/06/2015.
 */
public class Manager {
    @Getter
    private final List<graviton.api.Manager> managers;

    @Getter
    final private Date dateOfStart;

    @Inject
    public Manager(NetworkManager networkManager, GameManager gameManager, PacketManager packetManager, CommandManager commandManager, FightManager fightManager, Scanner scanner) {
        this.dateOfStart = new Date();
        managers = Arrays.asList(gameManager, packetManager, commandManager, scanner, networkManager, fightManager);
    }

    public void start() {
        this.managers.forEach(graviton.api.Manager::load);
        Runtime.getRuntime().addShutdownHook(new Thread(this::stop));
    }

    private void stop() {
        this.managers.forEach(graviton.api.Manager::unload);
    }

}
