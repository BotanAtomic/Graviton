package graviton.game.exchange.trunk;

import graviton.game.GameManager;
import graviton.game.client.player.Player;
import graviton.game.action.player.ActionManager;
import graviton.game.client.player.packet.Packets;
import graviton.game.exchange.api.Exchange;
import graviton.game.object.Object;
import graviton.game.trunk.Trunk;
import lombok.Getter;

/**
 * Created by Botan on 27/02/2016.
 */
public class TrunkExchange implements Exchange {

    private final Player exchanger;
    @Getter
    private final Trunk trunk;
    private final GameManager manager;

    public TrunkExchange(Player player, Trunk trunk) {
        this.exchanger = player;
        this.trunk = trunk;
        this.manager = player.getGameManager();

        trunk.open(player);
        player.getActionManager().resetActions();
        player.getActionManager().setStatus(ActionManager.Status.EXCHANGING);
    }

    @Override
    public void cancel() {
        this.exchanger.send("EV");
        this.trunk.setUserId(0);
        this.exchanger.setActionState(ActionManager.Status.WAITING);
    }

    @Override
    public void apply() {

    }

    @Override
    public void toogleOk(int id) {

    }

    private Object getSameObject(Object object) {
        for (Object trunkObject : trunk.getObjects().values())
            if (object.getTemplate().getId() == trunkObject.getTemplate().getId() && object.getStatistics().isSameStatistics(trunkObject.getStatistics()))
                return trunkObject;
        return null;
    }

    @Override
    public void addObject(int idObject, int quantity, int idPlayer) {
        if (trunk.getObjects().size() >= 50) {
            exchanger.sendText("Le coffre est rempli, impossible d'y ajouter un nouvel objet.");
            return;
        }
        String packet = "O+";
        Object object = exchanger.getObjects().get(idObject);
        Object trunkObject = getSameObject(object);

        int newQuantity = object.getQuantity() - quantity;

        if (trunkObject != null) {
            if (newQuantity <= 0) {
                exchanger.removeObject(idObject, true);
                trunkObject.setQuantity(trunkObject.getQuantity() + object.getQuantity());
            } else {
                exchanger.setObjectQuantity(object, newQuantity);
                trunkObject.setQuantity(trunkObject.getQuantity() + quantity);
            }
            packet += trunkObject.getId() + "|" + trunkObject.getQuantity() + "|" + trunkObject.getTemplate().getId() + "|" + trunkObject.parseEffects();
        } else {
            if (newQuantity <= 0) {
                exchanger.removeObject(idObject, false);
                trunk.getObjects().put(object.getId(), object);
                packet += object.getId() + "|" + object.getQuantity() + "|" + object.getTemplate().getId() + "|" + object.parseEffects();
            } else {
                exchanger.setObjectQuantity(object, object.getQuantity() - quantity);
                Object clone = object.getClone(quantity, true);
                trunk.getObjects().put(clone.getId(), clone);
                packet += clone.getId() + "|" + quantity + "|" + clone.getTemplate().getId() + "|" + clone.parseEffects();
            }
        }
        exchanger.refreshPods();
        exchanger.send("EsK" + packet);
        manager.updateTrunk(trunk);

        if(trunk.isBank())
            exchanger.getAccount().update();
    }

    @Override
    public void removeObject(int idObject, int quantity, int idPlayer) {
        Object object = trunk.getObjects().get(idObject);
        Object playerObject = exchanger.getSameObject(object);
        int newQuantity = object.getQuantity() - quantity;

        String packet = "O+";

        if(playerObject == null) {
            if(newQuantity <= 0) {
                trunk.getObjects().remove(idObject);
                manager.removeObject(idObject);
                exchanger.addObject(object, true);
                packet = "O-" + idObject;
            } else {
                Object clone = object.getClone(quantity,false);
                object.setQuantity(object.getQuantity() - quantity);
                exchanger.addObject(clone, true);
                packet += object.getId() + "|" + object.getQuantity() + "|" + object.getTemplate().getId() + "|" + object.parseEffects();
            }
        } else {
            if(newQuantity <= 0) {
                trunk.getObjects().remove(idObject);
                manager.removeObject(idObject);
                exchanger.setObjectQuantity(playerObject, playerObject.getQuantity() + quantity);
                packet = "O-" + idObject;
            } else {
                object.setQuantity(object.getQuantity() - quantity);
                exchanger.setObjectQuantity(playerObject,playerObject.getQuantity() + quantity);
                packet += object.getId() + "|" + object.getQuantity() + "|" + object.getTemplate().getId() + "|" + object.parseEffects();
            }
        }

        exchanger.refreshPods();
        exchanger.send("EsK" + packet);
        manager.updateTrunk(trunk);

        if(trunk.isBank())
            exchanger.getAccount().update();
    }

    @Override
    public void editKamas(int idPlayer, long kamas) {
        trunk.changeKamas(kamas);
        exchanger.setKamas(exchanger.getKamas() - kamas);
        exchanger.send(exchanger.getPacket(Packets.As));
        exchanger.send("EsKG" + trunk.getKamas());

        exchanger.save();
        manager.updateTrunk(trunk);

        if(trunk.isBank())
            exchanger.getAccount().update();
    }

    @Override
    public int getObjectQuantity(Player player, int id) {
        return 0;
    }
}
