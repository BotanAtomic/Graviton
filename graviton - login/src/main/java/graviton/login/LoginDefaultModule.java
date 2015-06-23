package graviton.login;

import com.google.inject.AbstractModule;
import graviton.console.Console;

/**
 * Created by Botan on 05/06/2015.
 */

public class LoginDefaultModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(Configuration.class).asEagerSingleton();
        bind(LoginManager.class).asEagerSingleton();
        bind(Console.class).asEagerSingleton();
    }
}
