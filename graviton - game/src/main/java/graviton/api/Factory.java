package graviton.api;


import com.google.inject.Inject;
import graviton.core.Configuration;
import graviton.database.Database;
import graviton.enums.DataType;
import graviton.enums.DatabaseType;

import java.beans.XMLDecoder;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;

/**
 * Created by Botan on 26/12/2015.
 */
public abstract class Factory<T> {
    @Inject
    Configuration configuration;

    protected DatabaseType databaseType;

    protected Database database;

    protected Factory(DatabaseType type) {
        this.databaseType = type;
    }

    public void configureDatabase() {
        this.database = databaseType == DatabaseType.LOGIN ? configuration.getLoginDatabase() : configuration.getGameDatabase();
    }

    public abstract DataType getType();

    public abstract Map<Integer,T> getElements();

    public abstract T get(Object object);

    public abstract void configure();

    public Object decodeObject(String name) {
        try {
            XMLDecoder decoder = new XMLDecoder(new FileInputStream(new File("data/" + name)));
            return decoder.readObject();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
