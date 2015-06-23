package graviton.database;

import com.google.inject.AbstractModule;

/**
 * Created by Botan on 06/06/2015.
 */
public class DatabaseDefaultModule extends AbstractModule {

    @Override
    public void configure() {
        bind(DatabaseManager.class).asEagerSingleton();
    }
}
