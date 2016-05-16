package graviton.game.creature.npc.exchange;

import graviton.game.GameManager;
import graviton.game.client.player.Player;
import graviton.game.creature.npc.NpcTemplate;
import graviton.game.exchange.api.Exchanger;
import graviton.game.object.Object;
import graviton.game.object.ObjectTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Botan on 16/05/2016.
 */
public class NpcTemplateExchange extends NpcTemplate {

    private final List<ExchangeEntity> exchanges;

    public NpcTemplateExchange(GameManager gameManager, int id, int gfx, int sex, int[] colors, String accessories, int extraClip, int customArt, int initQuestion, String objects, String exchange) {
        super(gameManager, id, gfx, sex, colors, accessories, extraClip, customArt, initQuestion, objects);
        this.exchanges = configureData(exchange);
    }

    private List<ExchangeEntity> configureData(String exchangeData) {
        List<ExchangeEntity> exchanges = new ArrayList<>();

        for (String data : exchangeData.split("~"))
            exchanges.add(new ExchangeEntity(data));

        return exchanges;
    }

    public List<ExchangeEntity.ObjectExchange> check(Player player, Map<Integer, Integer> objects) {
        if (objects.isEmpty())
            return null;

        List<ExchangeEntity.ObjectExchange> exchanges;
        for (ExchangeEntity entity : this.exchanges) {
            if ((exchanges = checkExchange(entity, player, objects)) != null)
                return exchanges;
        }
        return null;
    }

    private List<ExchangeEntity.ObjectExchange> checkExchange(ExchangeEntity entity, Player player, Map<Integer, Integer> objects) {
        List<Integer> coefficient = new ArrayList<>();

        for (ExchangeEntity.ObjectExchange exchange : entity.getGives()) {
            boolean haveTemplate = false;

            for (int object : objects.keySet()) {
                if (exchange.getId() == player.getObjects().get(object).getTemplate().getId()) {
                    if (objects.get(object) % exchange.getQuantity() == 0)
                        coefficient.add(objects.get(object) / exchange.getQuantity());
                    else
                        coefficient.add(1);
                    if (exchange.getQuantity() * (objects.get(object) % exchange.getQuantity() == 0 ? objects.get(object) / exchange.getQuantity() : 1) == objects.get(object)) {
                        haveTemplate = true;
                    } else {
                        return null;
                    }
                }
            }

            if (!haveTemplate)
                return null;

        }
        return entity.getGets(getMinimumValue(coefficient));
    }

    private int getMinimumValue(List<Integer> data) {
        if (data.isEmpty())
            return 1;

        int minimumValue = Integer.MAX_VALUE;
        for (int value : data) {
            if (value < minimumValue)
                minimumValue = value;
        }
        System.err.println("minimum value = " + minimumValue);
        return minimumValue;
    }
}
