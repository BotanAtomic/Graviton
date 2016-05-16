package graviton.game.exchange.npc;

import graviton.game.GameManager;
import graviton.game.client.player.Player;
import graviton.game.client.player.packet.Packets;
import graviton.game.creature.npc.Npc;
import graviton.game.creature.npc.exchange.ExchangeEntity;
import graviton.game.creature.npc.exchange.NpcTemplateExchange;
import graviton.game.exchange.api.Exchange;
import graviton.game.exchange.api.Exchanger;
import graviton.game.object.Object;
import graviton.game.object.ObjectTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Botan on 16/05/2016.
 */
public class NpcExchange implements Exchange {
    final private Exchanger player;
    final private NpcTemplateExchange npc;

    final private Map<Integer, Integer> npcObjects;

    public NpcExchange(Player player, Npc npc) {
        this.player = new Exchanger(player);
        this.npc = (NpcTemplateExchange) npc.getTemplate();
        this.npcObjects = new HashMap();
    }

    @Override
    public void cancel() {
        player.send("EV");
        player.quit();
        player.getCreature().getActionManager().resetActions();
    }

    @Override
    public void apply() {
        GameManager gameManager = player.getCreature().getGameManager();

        player.getCreature().addKamas(-player.getKamas());
        npcObjects.keySet().forEach(objectTemplate -> player.getCreature().addObject(gameManager.getObjectTemplate(objectTemplate).createObject(npcObjects.get(objectTemplate), false), true));
        player.getObjects().keySet().forEach(object -> player.getCreature().removeObject(object, true));

        player.send("EVa");
        player.quit();
        player.send(player.getCreature().getPacket(Packets.As));
        player.getCreature().getActionManager().resetActions();
        player.getCreature().refreshQuantity();
        player.getCreature().save();
    }

    @Override
    public void toogleOk(int id) {
        player.send("EK" + (!player.isReady() ? "1" : "0") + id);
        player.setReady(!player.isReady());
        if (player.isReady() && !npcObjects.isEmpty())
            apply();
    }

    @Override
    public void addObject(int idObject, int quantity, int idPlayer) {
        player.setReady(false);

        Object object = player.getCreature().getObjects().get(idObject);

        if (object == null)
            return;

        if (player.getObjects().get(idObject) != null) {
            int newQuantity = player.getObjects().get(idObject) + quantity;
            player.getObjects().remove(idObject);
            player.getObjects().put(idObject, newQuantity);
            player.send("EMKO+" + idObject + "|" + newQuantity);
        } else {
            player.send("EMKO+" + idObject + "|" + quantity);
            player.getObjects().put(idObject, quantity);
        }
        List<ExchangeEntity.ObjectExchange> result = npc.check(player.getCreature(), player.getObjects());
        editObjectToNpc(result);
    }

    private void editObjectToNpc(List<ExchangeEntity.ObjectExchange> objects) {
        if (objects == null) {
            for (Integer object : this.npcObjects.keySet())
                player.send("EmKO-" + object);
            player.send("EK0" + npc.getId());
            npcObjects.clear();
        } else {
            for (ExchangeEntity.ObjectExchange objectExchange : objects) {
                ObjectTemplate template = player.getCreature().getGameManager().getObjectTemplate(objectExchange.getId());
                npcObjects.put(template.getId(), objectExchange.getQuantity());
                player.send("EmKO+" + template.getId() + "|" + objectExchange.getQuantity() + "|" + template.getId() + "|" + template.getStatistics());
            }
            player.send("EK1" + npc.getId());
        }
    }


    @Override
    public void removeObject(int idObject, int quantity, int idPlayer) {
        player.setReady(false);

        Object object = player.getCreature().getObjects().get(idObject);

        if (object == null)
            return;

        if (!player.getCreature().hasObject(idObject) || (quantity <= 0))
            return;

        int newQuantity = player.getObjects().get(idObject) - quantity;
        player.getObjects().remove(idObject);
        if (newQuantity < 1)
            player.send("EMKO-" + idObject + "|" + newQuantity);
        else {
            player.getObjects().put(idObject, newQuantity);
            player.send("EMKO+" + idObject + "|" + newQuantity);
        }
        List<ExchangeEntity.ObjectExchange> result = npc.check(player.getCreature(), player.getObjects());
        editObjectToNpc(result);
    }

    @Override
    public void editKamas(int idPlayer, long kamas) {
        player.setReady(false);
        player.send("EK0" + idPlayer);
        player.setKamas(kamas);
        player.send("EMKG" + kamas);
    }

    @Override
    public int getObjectQuantity(Player player, int id) {
        return 0;
    }
}
