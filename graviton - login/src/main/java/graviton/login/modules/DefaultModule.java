package graviton.login.modules;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import graviton.common.Scanner;
import graviton.database.Database;
import graviton.login.Manager;
import graviton.network.NetworkManager;
import graviton.network.application.ApplicationNetwork;
import graviton.network.exchange.ExchangeNetwork;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import sun.misc.BASE64Decoder;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.sql.SQLException;

/**
 * Created by Botan on 05/06/2015.
 */

public class DefaultModule extends AbstractModule {

    @Override
    protected void configure() {
        /** Configuration **/
        bindConstant().annotatedWith(Names.named("login.ip")).to("127.0.0.1");
        bindConstant().annotatedWith(Names.named("exchange.ip")).to("127.0.0.1");
        bindConstant().annotatedWith(Names.named("login.port")).to(699);
        bindConstant().annotatedWith(Names.named("exchange.port")).to(807);

        bind(DSLContext.class).toInstance(getContext());

        bind(Database.class).asEagerSingleton();
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

    private DSLContext getContext() {
        HikariDataSource source = new HikariDataSource(new HikariConfig() {
            {
                setDataSourceClassName("com.mysql.jdbc.jdbc2.optional.MysqlDataSource");
                addDataSourceProperty("serverName", decrypt("PRFPOUz8cPFkhmkZatwE6A=="));
                addDataSourceProperty("port", 3306);
                addDataSourceProperty("databaseName", decrypt("XtxV1iNc82puQyu1UdWQKg=="));
                addDataSourceProperty("user", decrypt("psUkfKpV6xHmdvuIMk05CQ=="));
                addDataSourceProperty("password", "");
            }
        });

        try {
            return DSL.using(source.getConnection(), SQLDialect.MYSQL);
        } catch (SQLException e) {
            binder().addError(e);
        }
        return null;
    }
}
