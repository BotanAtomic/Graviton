package graviton.core;

import com.google.inject.Guice;
import com.google.inject.Injector;
import graviton.core.injector.DatabaseModule;
import graviton.core.injector.DefaultModule;
import graviton.core.injector.GameModule;
import graviton.core.injector.NetworkModule;


/**
 * Created by Botan on 16/06/2015.
 */
public class Main {

    public static void main(String[] args) {
        Injector injector = Guice.createInjector(
                new DefaultModule(),
                new DatabaseModule(),
                new NetworkModule(),
                new GameModule());
        final Manager manager = injector.getInstance(Manager.class).start();
        Runtime.getRuntime().addShutdownHook(new Thread(manager::stop));
    }
}
