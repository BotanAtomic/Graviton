package graviton.core;


import com.google.inject.Guice;
import graviton.core.modules.DefaultModule;

/**
 * Created by Botan on 05/06/2015.
 */
public class Main {

    public static void main(String[] args) {
        Runtime.getRuntime().addShutdownHook(new Thread(Guice.createInjector(new DefaultModule()).getInstance(Manager.class).start()::stop));
    }
}

