package graviton.login.modules;

import com.google.inject.AbstractModule;
import graviton.login.Configuration;
import graviton.login.Login;

/**
 * Created by Botan on 05/06/2015.
 */

public class DefaultModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(Login.class).asEagerSingleton();
        bind(Configuration.class).asEagerSingleton();
    }
}
