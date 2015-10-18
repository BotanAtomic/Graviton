package graviton.core.injector;

import com.google.inject.AbstractModule;
import graviton.game.GameManager;
import graviton.game.client.player.component.CommandManager;

/**
 * Created by Botan on 19/06/2015.
 */
public class GameModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(GameManager.class).asEagerSingleton();
        bind(CommandManager.class).asEagerSingleton();
    }
}
