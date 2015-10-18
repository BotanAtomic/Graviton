package graviton.login.modules;

import com.google.inject.AbstractModule;
import graviton.common.Scanner;
import graviton.login.Configuration;
import graviton.login.Manager;

/**
 * Created by Botan on 05/06/2015.
 */

public class DefaultModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(Manager.class).asEagerSingleton();
        bind(Configuration.class).asEagerSingleton();
        bind(Scanner.class).asEagerSingleton();
    }
}
