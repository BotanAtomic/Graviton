package graviton.core.injector;

import com.google.inject.AbstractModule;
import graviton.common.Scanner;
import graviton.core.Configuration;
import graviton.core.Manager;


/**
 * Created by Botan on 16/06/2015.
 */
public class DefaultModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(Configuration.class).asEagerSingleton();
        bind(Manager.class).asEagerSingleton();
        bind(Scanner.class).asEagerSingleton();
    }
}