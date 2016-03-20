package graviton.api;


import graviton.database.Database;
import graviton.enums.DataType;

import java.beans.XMLDecoder;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Botan on 26/12/2015.
 */
public abstract class Factory<T> {

    protected Database database;

    public Factory(Database database) {
        this.database = database;
    }

    public abstract DataType getType();

    public abstract Map<Integer, T> getElements();

    public abstract T get(Object object);

    public abstract void configure();

    public abstract void save();

    public Object decodeObject(String name) {
        try {
            XMLDecoder decoder = new XMLDecoder(new FileInputStream(new File("data/" + name)));
            return decoder.readObject();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Object> decodeObjects(String directory) {
        List<Object> objects = new ArrayList<>();
        File folder = new File("data/" + directory);
        for (File file : folder.listFiles())
            objects.add(decodeObject(directory + "/" + file.getName()));
        return objects;
    }
}
