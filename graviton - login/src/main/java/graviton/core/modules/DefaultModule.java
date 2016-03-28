package graviton.core.modules;

import com.google.inject.AbstractModule;
import com.google.inject.MembersInjector;
import com.google.inject.TypeLiteral;
import com.google.inject.matcher.Matchers;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.spi.TypeListener;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import graviton.api.InjectSetting;
import graviton.common.Scanner;
import graviton.database.Database;
import graviton.core.Manager;
import graviton.network.NetworkManager;
import graviton.network.application.ApplicationNetwork;
import graviton.network.exchange.ExchangeNetwork;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import sun.misc.BASE64Decoder;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.Properties;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Created by Botan on 05/06/2015.
 */

public class DefaultModule extends AbstractModule {

    @Override
    protected void configure() {
        /** Configuration **/
        initConfiguration();

        bind(Manager.class).asEagerSingleton();
        bind(Scanner.class).asEagerSingleton();

        bind(NetworkManager.class).asEagerSingleton();
        bind(ExchangeNetwork.class).asEagerSingleton();
        bind(ApplicationNetwork.class).asEagerSingleton();
    }


    private String decrypt(String value) {
        try {
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec("1Hbfh667adfDEJ78".getBytes(), "AES"));
            byte[] decryptedValue64 = new BASE64Decoder().decodeBuffer(value);
            byte[] decryptedByteValue = cipher.doFinal(decryptedValue64);
            return new String(decryptedByteValue, "utf-8");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private void initConfiguration() {
        Properties properties = new Properties();

        try {
            properties.load(getClass().getClassLoader().getResourceAsStream("config.properties"));
            bind(Database.class).toInstance(new Database(DSL.using(new HikariDataSource(new HikariConfig() {
                {
                    setDataSourceClassName("com.mysql.jdbc.jdbc2.optional.MysqlDataSource");
                    addDataSourceProperty("serverName", decrypt(properties.getProperty("database.ip")));
                    addDataSourceProperty("port", 3306);
                    addDataSourceProperty("databaseName", decrypt(properties.getProperty("database.name")));
                    addDataSourceProperty("user", decrypt(properties.getProperty("database.user")));
                    addDataSourceProperty("password", decrypt(properties.getProperty("database.password")));
                }
            }).getConnection(), SQLDialect.MYSQL)));
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
    }

    TypeListener listener(BiConsumer<TypeLiteral<?>, TypeEncounter<?>> consumer) {
        return consumer::accept;
    }

    MembersInjector<Object> injector(Consumer<Object> consumer) {
        return consumer::accept;
    }

    Object parse(Object value, Field field) {
        Type type = field.getType();

        if (type == boolean.class)
            value = Boolean.parseBoolean(value.toString());
        else if (type == int.class)
            value = Integer.parseInt(value.toString());

        return value;
    }
}
