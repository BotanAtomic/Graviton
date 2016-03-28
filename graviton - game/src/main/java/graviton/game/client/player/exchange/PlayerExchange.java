package graviton.game.client.player.exchange;

import graviton.game.GameManager;
import graviton.game.client.player.Player;
import graviton.game.client.player.component.ActionManager;
import graviton.game.client.player.packet.Packets;
import graviton.game.exchange.Exchange;
import graviton.game.exchange.Exchanger;
import graviton.game.object.Object;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Botan on 25/12/2015.
 */

public class PlayerExchange implements Exchange {

    private final Map<Integer, Exchanger> exchangers;

    private final GameManager manager;

    public PlayerExchange(Player player1, Player player2) {
        this.exchangers = new HashMap<>();

        this.exchangers.put(player1.getId(), new Exchanger(player1));
        this.exchangers.put(player2.getId(), new Exchanger(player2));

        this.manager = player1.getGameManager();

        player1.setExchange(this);
        player2.setExchange(this);

        Arrays.asList(player1, player2).forEach(exchanger -> exchanger.getActionManager().setStatus(ActionManager.Status.EXCHANGING));
    }

    public void cancel() {
        exchangers.values().forEach(exchanger -> exchanger.send("EV"));
        exchangers.values().forEach(exchanger -> exchanger.quit());
        exchangers.values().forEach(exchanger -> exchanger.getCreature().getActionManager().resetActions());
    }

    public void apply() {
        Exchanger exchanger = null;
        for(Exchanger exchanger1 : exchangers.values())
            exchanger = exchanger1;

        Exchanger other = getOther(exchanger);

        for (Integer i : exchanger.getObjects().keySet()) {
            int quantity = exchanger.getObjects().get(i);

            if (quantity == 0 || !exchanger.getCreature().hasObject(i)) {
                exchanger.getObjects().remove(i);
                continue;
            }

            Object object = exchanger.getCreature().getObjects().get(i);

            if ((object.getQuantity() - quantity) < 1) {
                exchanger.getCreature().removeObject(i, true);
                other.getCreature().addObject(object, true);
            } else {
                exchanger.getCreature().setObjectQuantity(object, (object.getQuantity() - quantity));
                other.getCreature().addObject(object.getClone(quantity, false), true);
            }
        }

        for (Integer i : other.getObjects().keySet()) {
            int quantity = other.getObjects().get(i);

            if (quantity == 0 || !other.getCreature().hasObject(i)) {
                other.getObjects().remove(i);
                continue;
            }

            Object object = other.getCreature().getObjects().get(i);

            if ((object.getQuantity() - quantity) < 1) {
                other.getCreature().removeObject(i, true);
                exchanger.getCreature().addObject(object, true);
            } else {
                other.getCreature().setObjectQuantity(object, (object.getQuantity() - quantity));
                exchanger.getCreature().addObject(object.getClone(quantity, false), true);
            }
        }


        for (Exchanger exchangers : this.exchangers.values()) {
            exchangers.quit();
            exchangers.send(exchangers.getCreature().getPacket(Packets.As));
            exchangers.send("EVa");
            exchangers.getCreature().refreshQuantity();
            exchangers.getCreature().save();
        }
    }

    private Exchanger getOther(Exchanger exchanger) {
        for (Exchanger exchanger1 : this.exchangers.values())
            if (exchanger1 != exchanger)
                return exchanger1;
        return null;
    }

    public void addObject(int idObject, int quantity, int idPlayer) {
        exchangers.values().forEach(exchanger -> exchanger.setOk(false));
        exchangers.values().forEach(exchanger -> this.sendOk(exchanger));

        Exchanger exchanger = exchangers.get(idPlayer);
        Exchanger other = getOther(exchanger);

        Object object = exchanger.getCreature().getObjects().get(idObject);

        if (object == null)
            return;

        String str = idObject + "|" + quantity;
        String add = "|" + object.getTemplate().getId() + "|" + object.parseEffects();

        if (exchanger.getObjects().get(idObject) != null) {
            int newQuantity = exchanger.getObjects().get(idObject) + quantity;
            exchanger.getObjects().remove(idObject);
            exchanger.getObjects().put(idObject, newQuantity);
            this.sendMoveOk(exchanger.getCreature(), other.getCreature(), 'O', "+", idObject + "|" + newQuantity, add);
            return;
        }
        this.sendMoveOk(exchanger.getCreature(), other.getCreature(), 'O', "+", str, add);
        exchanger.getObjects().put(idObject, quantity);
    }

    public void removeObject(int idObject, int quantity, int idPlayer) {
        exchangers.values().forEach(exchanger -> exchanger.setOk(false));
        exchangers.values().forEach(exchanger -> this.sendOk(exchanger));

        Exchanger exchanger = exchangers.get(idPlayer);
        Exchanger other = getOther(exchanger);
        Object object = exchanger.getCreature().getObjects().get(idObject);

        if (object == null)
            return;

        if (!exchanger.getCreature().hasObject(idObject) || (quantity <= 0))
            return;

        String add = "|" + object.getTemplate().getId() + "|" + object.parseEffects();

        int newQuantity = exchanger.getObjects().get(idObject) - quantity;
        exchanger.getObjects().remove(idObject);
        if (newQuantity < 1)
            this.sendMoveOk(exchanger.getCreature(), other.getCreature(), 'O', "-", String.valueOf(idObject), "");
        else {
            exchanger.getObjects().put(idObject, newQuantity);
            this.sendMoveOk(exchanger.getCreature(), other.getCreature(), 'O', "+", idObject + "|" + newQuantity, add);
        }
    }

    public void toogleOk(int id) {
        Exchanger exchanger = exchangers.get(id);
        exchanger.setOk(!exchanger.isOk());
        this.sendOk(exchanger);
        if (exchanger.isOk() && getOther(exchanger).isOk())
            apply();
    }

    public void editKamas(int idPlayer, long kamas) {
        exchangers.values().forEach(exchanger -> exchanger.setOk(false));
        exchangers.values().forEach(exchanger -> this.sendOk(exchanger));
        Exchanger exchanger = exchangers.get(idPlayer);
        exchanger.setKamas(kamas);
        this.sendMoveOk(exchanger.getCreature(), getOther(exchanger).getCreature(), 'G', "", String.valueOf(kamas), "");
    }

    private void sendOk(Exchanger exchanger) {
        String packet = "EK" + (exchanger.isOk() ? "1" : "0") + exchanger.getCreature().getId();
        exchangers.values().forEach(exchanger1 -> exchanger1.send(packet));
    }

    private void sendMoveOk(Player player1, Player player2, char type, String signe, String str, String add) {
        if (player1 != null) {
            String packet1 = "EMK" + type + signe + (!str.equals("") ? str : "");
            player1.send(packet1);
        }
        if (player2 != null) {
            String packet2 = "EmK" + type + signe + (!str.equals("") ? str : "") + add;
            player2.send(packet2);
        }
    }

    public int getObjectQuantity(Player creature, int idObject) {
        Map<Integer, Integer> objects = exchangers.get(creature.getId()).getObjects();
        return objects.containsKey(idObject) ? objects.get(idObject) : 0;
    }
}