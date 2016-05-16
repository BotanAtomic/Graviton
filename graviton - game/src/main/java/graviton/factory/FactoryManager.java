package graviton.factory;

import graviton.api.Factory;
import graviton.enums.DataType;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Botan on 15/05/2016.
 */
public class FactoryManager {
    private Map<DataType, Factory<?>> factorys;

    public FactoryManager() {
        this.factorys = new ConcurrentHashMap<>();
    }

    public void addFactory(Factory factory) {
        this.factorys.put(factory.getType(),factory);
    }

    public Map<DataType,Factory<?>> getFactorys() {
        return this.factorys;
    }

    public Factory get(DataType dataType) {
        return this.factorys.get(dataType);
    }

}
