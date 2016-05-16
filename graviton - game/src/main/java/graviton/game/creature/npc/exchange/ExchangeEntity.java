package graviton.game.creature.npc.exchange;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Botan on 16/05/2016.
 */
public class ExchangeEntity {
    @Getter
    private final List<ObjectExchange> gives, gets;

    public ExchangeEntity(String data) {
        String[] split = data.split("\\|");
        this.gives = configure(split[0]);
        this.gets = configure(split[1]);
    }

    private List<ObjectExchange> configure(String data) {
        List<ObjectExchange> exchanges = new ArrayList<>();
        for (String values : data.split(","))
            exchanges.add(new ObjectExchange(Integer.parseInt(values.split(":")[0]), Short.parseShort(values.split(":")[1])));
        return exchanges;
    }

    private List<ObjectExchange> getGets() {
        return gets;
    }

    public List<ObjectExchange> getGets(int coefficient) {
        if(coefficient == 1)
            return getGets();
        return gets.stream().map(objectExchange -> new ObjectExchange(objectExchange.getId(), objectExchange.getQuantity() * coefficient)).collect(Collectors.toList());
    }

    public static class ObjectExchange {
        private final int id;
        private final int quantity;

        public ObjectExchange(int id, int quantity) {
            this.id = id;
            this.quantity = quantity;
        }

        public int getId() {
            return id;
        }

        public int getQuantity() {
            return quantity;
        }
    }
}
