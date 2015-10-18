package graviton.login;


import com.google.inject.Guice;
import com.google.inject.Injector;
import graviton.login.modules.DefaultModule;
import graviton.login.modules.NetworkModule;


/**
 * Created by Botan on 05/06/2015.
 */
public class Main {

    private static Injector injector;

    public static void main(String[] args) {
        injector = Guice.createInjector(new DefaultModule(),new NetworkModule());
        final Manager manager = injector.getInstance(Manager.class).start();
        Runtime.getRuntime().addShutdownHook(new Thread(manager::stop));
    }

    public static <T> T getInstance(Class<T> clazz) {
        return injector.getInstance(clazz);
    }
}
