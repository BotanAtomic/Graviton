package graviton.core.injector;

import com.google.inject.AbstractModule;
import graviton.console.Console;
import graviton.core.Configuration;
import graviton.core.ServerManager;


/**
 * Created by Botan on 16/06/2015.
 */
public class DefaultModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(Configuration.class).asEagerSingleton();
        bind(Console.class).asEagerSingleton();
        bind(ServerManager.class).asEagerSingleton();
    }
}