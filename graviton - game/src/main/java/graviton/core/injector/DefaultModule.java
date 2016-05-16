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
import graviton.factory.FactoryManager;
import graviton.factory.type.*;
import graviton.game.GameManager;
import graviton.game.action.player.CommandManager;
import graviton.network.NetworkManager;
import graviton.network.PacketManager;
import graviton.network.exchange.ExchangeNetwork;
import graviton.network.game.GameNetwork;
import lombok.extern.slf4j.Slf4j;
import org.fusesource.jansi.AnsiConsole;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.Properties;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Created by Botan on 16/06/2015.
 */

@Slf4j
public class DefaultModule extends AbstractModule {
    private final long startTime = System.currentTimeMillis();

    @Override
    protected void configure() {
        System.out.println("                 _____                     _  _                \n                / ____|                   (_)| |               \n               | |  __  _ __  __ _ __   __ _ | |_  ___   _ __  \n               | | |_ || '__|/ _` |\\ \\ / /| || __|/ _ \\ | '_ \\ \n               | |__| || |  | (_| | \\ V / | || |_| (_) || | | |\n                \\_____||_|   \\__,_|  \\_/  |_| \\__|\\___/ |_| |_|\n");
        AnsiConsole.out.append("\033]0;").append("Graviton - Game").append("\007");

        /** Configuration **/
        initConfiguration();

        /** Interface class **/
        bind(Manager.class).asEagerSingleton();
        bind(GameManager.class).asEagerSingleton();
        bind(CommandManager.class).asEagerSingleton();
        bind(Scanner.class).asEagerSingleton();
        bind(NetworkManager.class).asEagerSingleton();

        /** Network **/
        bind(GameNetwork.class).asEagerSingleton();
        bind(ExchangeNetwork.class).asEagerSingleton();

        /** Factory **/
        bind(FactoryManager.class).asEagerSingleton();
        bind(PlayerFactory.class).asEagerSingleton();
        bind(AccountFactory.class).asEagerSingleton();
        bind(MapFactory.class).asEagerSingleton();
        bind(ObjectFactory.class).asEagerSingleton();
        bind(SpellFactory.class).asEagerSingleton();
        bind(NpcFactory.class).asEagerSingleton();
        bind(MonsterFactory.class).asEagerSingleton();
        bind(JobFactory.class).asEagerSingleton();
        bind(GuildFactory.class).asEagerSingleton();
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
                            field.set(instance, parse(value, field.getType()));
                        } catch (IllegalAccessException e) {
                            binder().addError(e);
                        }
                    }));
                }
            }
        })));
        log.info("Config file loaded");
        bind(Database.class).annotatedWith(Names.named("database.login")).toInstance(new Database(properties, "database.login."));
        bind(Database.class).annotatedWith(Names.named("database.game")).toInstance(new Database(properties, "database.game."));
        log.info("Databases loaded");
        bind(PacketManager.class).toInstance(new PacketManager(properties.getProperty("word.dictionary").split(","), properties.getProperty("word.forbidden").split(",")));
    }

    private TypeListener listener(BiConsumer<TypeLiteral<?>, TypeEncounter<?>> consumer) {
        return consumer::accept;
    }

    private MembersInjector<Object> injector(Consumer<Object> consumer) {
        return consumer::accept;
    }

    private Object parse(Object value, Type type) {
        if (type == boolean.class)
            value = Boolean.parseBoolean(value.toString());
        else if (type == int.class)
            value = Integer.parseInt(value.toString());
        return value;
    }
}