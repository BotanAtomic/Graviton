package graviton.core;

import com.google.inject.Guice;
import graviton.core.injector.DefaultModule;


/**
 * Created by Botan on 16/06/2015.
 */
public class Main {

    public static void main(String[] args) {
        Guice.createInjector(new DefaultModule()).getInstance(Manager.class).start();
    }
}
