package graviton.core.injector;

import com.google.inject.AbstractModule;
import graviton.database.DatabaseManager;

/**
 * Created by Botan on 16/06/2015.
 */
public class DatabaseModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(DatabaseManager.class).asEagerSingleton();
    }
}
