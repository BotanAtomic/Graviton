package graviton.core.injector;

import com.google.inject.AbstractModule;
import com.google.inject.MembersInjector;
import com.google.inject.TypeLiteral;
import com.google.inject.matcher.Matchers;
import com.google.inject.name.Names;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.spi.TypeListener;
import graviton.api.InjectSetting;
import graviton.common.Scanner;
import graviton.core.Manager;
import graviton.database.Database;
import graviton.factory.*;
import graviton.game.GameManager;
import graviton.network.PacketManager;
import graviton.game.action.player.CommandManager;
import graviton.network.NetworkManager;
import graviton.network.exchange.ExchangeNetwork;
import graviton.network.game.GameNetwork;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.Properties;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Created by Botan on 16/06/2015.
 */
public class DefaultModule extends AbstractModule {

    @Override
    protected void configure() {
        /** Configuration **/
        initConfiguration();

        /** Interface class **/
        bind(Manager.class).asEagerSingleton();
        bind(Scanner.class).asEagerSingleton();
        bind(GameManager.class).asEagerSingleton();
        bind(CommandManager.class).asEagerSingleton();
        bind(NetworkManager.class).asEagerSingleton();

        /** Network **/
        bind(GameNetwork.class).asEagerSingleton();
        bind(ExchangeNetwork.class).asEagerSingleton();

        /** Factory **/
        bind(PlayerFactory.class).asEagerSingleton();
        bind(AccountFactory.class).asEagerSingleton();
        bind(MapFactory.class).asEagerSingleton();
        bind(ObjectFactory.class).asEagerSingleton();
        bind(SpellFactory.class).asEagerSingleton();
        bind(NpcFactory.class).asEagerSingleton();
        bind(MonsterFactory.class).asEagerSingleton();
    }

    private void initConfiguration() {
        Properties properties = new Properties();

        try {
            properties.load(getClass().getClassLoader().getResourceAsStream("config.properties"));
        } catch (Exception e) {
            binder().addError(e);
            throw new RuntimeException(e);
        }

        binder().bindListener(Matchers.any(), listener(((type, encounter) -> {
            for (Field field : type.getRawType().getDeclaredFields()) {
                if (field.isAnnotationPresent(InjectSetting.class)) {
                    field.setAccessible(true);

                    encounter.register(injector(instance -> {
                        try {
                            Object value = properties.get(field.getAnnotation(InjectSetting.class).value());
                            field.set(instance, parse(value, field));
                        } catch (IllegalAccessException e) {
                            binder().addError(e);
                        }
                    }));
                }
            }
        })));
        bind(Database.class).annotatedWith(Names.named("database.login")).toInstance(new Database(properties, "database.login."));
        bind(Database.class).annotatedWith(Names.named("database.game")).toInstance(new Database(properties, "database.game."));

        bind(PacketManager.class).toInstance(new PacketManager(properties.getProperty("word.dictionnary").split(","), properties.getProperty("word.forbiden").split(",")));
    }

    private TypeListener listener(BiConsumer<TypeLiteral<?>, TypeEncounter<?>> consumer) {
        return consumer::accept;
    }

    private MembersInjector<Object> injector(Consumer<Object> consumer) {
        return consumer::accept;
    }

    private Object parse(Object value, Field field) {
        Type type = field.getType();

        if (type == boolean.class)
            value = Boolean.parseBoolean(value.toString());
        else if (type == int.class)
            value = Integer.parseInt(value.toString());

        return value;
    }
}