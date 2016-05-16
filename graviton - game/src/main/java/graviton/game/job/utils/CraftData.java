package graviton.game.job.utils;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Botan on 29/06/2016.
 */
@Data
public final class CraftData {
    private final Map<Short, Short> ingredients;
    private final short result;

    public CraftData(String data, short result) {
        this.ingredients = getIngredients(data);
        this.result = result;
    }

    private Map<Short, Short> getIngredients(String data) {
        Map<Short, Short> ingredients = new HashMap();
        for (String craft : data.split(";")) {
            ingredients.put(Short.parseShort(craft.split("\\*")[0]), Short.parseShort(craft.split("\\*")[1]));
        }
        return ingredients;
    }

    public boolean check(Map<Short, Short> ingredients) {
        return this.ingredients.equals(ingredients);
    }
}
