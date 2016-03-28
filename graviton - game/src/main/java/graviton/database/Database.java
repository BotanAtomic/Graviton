package graviton.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.Data;
import org.jooq.*;
import org.jooq.impl.DSL;
import sun.misc.BASE64Decoder;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.sql.SQLException;
import java.util.Properties;

import static org.jooq.impl.DSL.max;

/**
 * Created by Botan on 16/06/2015.
 */
@Data
public class Database {

    private DSLContext dslContext;

    public Database(Properties propreties, String prefix) {
        try {
            this.dslContext = DSL.using(new HikariDataSource(new HikariConfig() {
                {
                    setDataSourceClassName("com.mysql.jdbc.jdbc2.optional.MysqlDataSource");
                    addDataSourceProperty("serverName", decrypt(propreties.getProperty(prefix + "ip")));
                    addDataSourceProperty("port", 3306);
                    addDataSourceProperty("databaseName", decrypt(propreties.getProperty(prefix + "name")));
                    addDataSourceProperty("user", decrypt(propreties.getProperty(prefix + "user")));
                    addDataSourceProperty("password", decrypt(propreties.getProperty(prefix + "password")));
                }
            }).getConnection(), SQLDialect.MYSQL);
        } catch (SQLException e) {
            e.printStackTrace();
        }
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

    public int getNextId(Table<?> table, Field<?> field) {
        try {
            return (int) dslContext.select(max(field).add(1)).from(table).fetchOne().getValue(0);
        } catch (Exception e) {
            return 1;
        }
    }

    public DSLContext getDSLContext() {
        return this.dslContext;
    }

    public void remove(Table<?> table, Condition condition) {
        dslContext.delete(table).where(condition).execute();
    }

    public Result<Record> getResult(Table<?> table) {
        return dslContext.select().from(table).fetch();
    }

    public Result<Record> getResult(Table<?> table, Condition condition) {
        return dslContext.select().from(table).where(condition).fetch();
    }

    public Record getRecord(Table<?> table) {
        return dslContext.select().from(table).fetchOne();
    }

    public Record getRecord(Table<?> table, Condition condition) {
        return dslContext.select().from(table).where(condition).fetchOne();
    }
}
