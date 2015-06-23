package graviton.core.injector;

import com.google.inject.AbstractModule;
import graviton.game.manager.GameManager;

/**
 * Created by Botan on 19/06/2015.
 */
public class GameModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(GameManager.class).asEagerSingleton();
    }
}
