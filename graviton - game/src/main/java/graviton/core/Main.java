package graviton.core;

import com.google.inject.Guice;
import com.google.inject.Injector;
import graviton.core.injector.DatabaseModule;
import graviton.core.injector.DefaultModule;
import graviton.core.injector.GameModule;
import graviton.core.injector.NetworkModule;
import java.util.*;


/**
 * Created by Botan on 16/06/2015.
 */
public class Main {

    private static Injector injector;
    private static int serverId;

    public static void main(String[] args) {
        injector = Guice.createInjector(
                new DefaultModule(),
                new DatabaseModule(),
                new NetworkModule(),
                new GameModule());
         final Server manager = injector.getInstance(Server.class).configure();
         Runtime.getRuntime().addShutdownHook(new Thread(manager::stop));
    }

    public final static int getServerId() {
        return serverId = (serverId == 0 ? injector.getInstance(Configuration.class).getServerId() : serverId);
    }

    public final static <T> T getInstance(Class<T> instance) {
        return injector.getInstance(instance);
    }
}
