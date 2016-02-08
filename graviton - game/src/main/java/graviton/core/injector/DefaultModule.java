package graviton.core.injector;

import com.google.inject.AbstractModule;
import graviton.common.Scanner;
import graviton.core.Configuration;
import graviton.core.Manager;
import graviton.factory.*;
import graviton.game.GameManager;
import graviton.game.PacketManager;
import graviton.game.client.player.component.CommandManager;
import graviton.network.NetworkManager;
import graviton.network.exchange.ExchangeNetwork;
import graviton.network.game.GameNetwork;


/**
 * Created by Botan on 16/06/2015.
 */
public class DefaultModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(Configuration.class).asEagerSingleton();
        bind(Scanner.class).asEagerSingleton();

        bind(GameManager.class).asEagerSingleton();
        bind(CommandManager.class).asEagerSingleton();
        bind(NetworkManager.class).asEagerSingleton();
        bind(PacketManager.class).asEagerSingleton();
        bind(Manager.class).asEagerSingleton();

        bind(GameNetwork.class).asEagerSingleton();
        bind(ExchangeNetwork.class).asEagerSingleton();

        bind(PlayerFactory.class).asEagerSingleton();
        bind(AccountFactory.class).asEagerSingleton();
        bind(MapFactory.class).asEagerSingleton();
        bind(ObjectFactory.class).asEagerSingleton();
        bind(SpellFactory.class).asEagerSingleton();
        bind(NpcFactory.class).asEagerSingleton();
    }
}