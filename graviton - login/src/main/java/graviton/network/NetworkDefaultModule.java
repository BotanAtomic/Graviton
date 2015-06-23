package graviton.network;

import com.google.inject.AbstractModule;

/**
 * Created by Botan on 06/06/2015.
 */
public class NetworkDefaultModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(NetworkManager.class).asEagerSingleton();
    }
}
