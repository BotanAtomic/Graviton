package graviton.login;


import com.google.inject.Guice;
import graviton.login.modules.DefaultModule;
import graviton.login.modules.NetworkModule;

/**
 * Created by Botan on 05/06/2015.
 */
public class Main {

    public static void main(String[] args) {
        Runtime.getRuntime().addShutdownHook(new Thread(Guice.createInjector(new DefaultModule(), new NetworkModule()).getInstance(Manager.class).start()::stop));
    }
}

