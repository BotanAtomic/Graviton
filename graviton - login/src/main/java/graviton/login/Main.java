package graviton.login;

import com.google.inject.Guice;
import com.google.inject.Injector;
import graviton.database.DatabaseDefaultModule;
import graviton.network.NetworkDefaultModule;

/**
 * Created by Botan on 05/06/2015.
 */
public class Main {

    public static void main (String[] args){
        Injector injector = Guice.createInjector(
                new LoginDefaultModule(),
                new DatabaseDefaultModule(),
                new NetworkDefaultModule());
        final LoginManager manager = injector.getInstance(LoginManager.class).start();
        Runtime.getRuntime().addShutdownHook(new Thread(manager::stop));
    }

}
