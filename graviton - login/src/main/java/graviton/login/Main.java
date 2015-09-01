package graviton.login;


import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.MembersInjector;
import graviton.database.Database;
import graviton.login.modules.DefaultModule;
import graviton.login.modules.NetworkModule;


/**
 * Created by Botan on 05/06/2015.
 */
public class Main {

    public static Injector injector;

    public static void main (String[] args){
        injector = Guice.createInjector(new DefaultModule(),new NetworkModule());
        injector.getInstance(Login.class).start();
    }

    public static final <T> T getInstance(Class<T> clazz) {
        return injector.getInstance(clazz);
    }
}
