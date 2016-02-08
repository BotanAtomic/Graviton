package graviton.game.client.player.exchange;

import graviton.game.GameManager;
import graviton.game.client.player.Player;
import graviton.game.exchange.Exchanger;
import graviton.game.object.Object;

import java.util.Arrays;
import java.util.Map;

/**
 * Created by Botan on 25/12/2015.
 */

public class PlayerExchange {

    private final Exchanger first;
    private final Exchanger second;

    private final GameManager manager;

    public PlayerExchange(Player player1, Player player2) {
        this.first = new Exchanger(player1);
        this.second = new Exchanger(player2);

        this.manager = player1.getGameManager();

        player1.setExchange(this);
        player2.setExchange(this);
    }

    public void cancel() {
        this.first.send("EV");
        this.second.send("EV");

        this.first.quit();
        this.second.quit();
    }

    private void apply() {
        this.first.getCreature().addKamas((-this.first.getKamas() + this.first.getKamas()));
        this.second.getCreature().addKamas((-this.second.getKamas() + this.first.getKamas()));

        for (Integer i : first.getObjects().keySet()) {
            int quantity = first.getObjects().get(i);

            if (quantity == 0 || first.getCreature().hasObject(i)) {
                first.getObjects().remove(i);
                continue;
            }

            Object object = first.getCreature().getObjects().get(i);

            if ((object.getQuantity() - quantity) < 1) {
                first.getCreature().removeObject(i, true);
                second.getCreature().addObject(object, true);
            } else {
                first.getCreature().setObjectQuantity(object, (object.getQuantity() - quantity));
                second.getCreature().addObject(object.getClone(quantity), true);
            }
        }

        for (Integer i : second.getObjects().keySet()) {
            int quantity = second.getObjects().get(i);

            if (quantity == 0 || second.getCreature().hasObject(i)) {
                second.getObjects().remove(i);
                continue;
            }

            Object object = first.getCreature().getObjects().get(i);

            if ((object.getQuantity() - quantity) < 1) {
                second.getCreature().removeObject(i, true);
                first.getCreature().addObject(object, true);
            } else {
                second.getCreature().setObjectQuantity(object, (object.getQuantity() - quantity));
                first.getCreature().addObject(object.getClone(quantity), true);
            }
        }


        for (Exchanger exchanger : Arrays.asList(first, second)) {
            exchanger.quit();
            exchanger.send(exchanger.getCreature().getPacket("As"));
            exchanger.send("EVa");
            exchanger.getCreature().refreshQuantity();
            exchanger.getCreature().save();
        }
    }

    public void addObject(int idObject, int quantity, int idPlayer) {
        this.first.setOk(false);
        this.second.setOk(false);

        this.sendOk(this.first);
        this.sendOk(this.second);

        Object object = manager.getObject(idObject);

        if (object == null)
            return;

        String str = idObject + "|" + quantity;
        String add = "|" + object.getTemplate().getId() + "|" + object.parseEffects();

        if (this.first.getCreature().getId() == idPlayer) {
            if (first.getObjects().get(idObject) != null) {
                int newQuantity = first.getObjects().get(idObject) + quantity;
                first.getObjects().remove(idObject);
                first.getObjects().put(idObject, newQuantity);
                this.sendMoveOk(this.first.getCreature(), this.second.getCreature(), 'O', "+", idObject + "|" + newQuantity, add);
                return;
            }
            this.sendMoveOk(this.first.getCreature(), this.second.getCreature(), 'O', "+", str, add);
            this.first.getObjects().put(idObject, quantity);
        } else if (this.second.getCreature().getId() == idPlayer) {
            if (second.getObjects().get(idObject) != null) {
                int newQuantity = second.getObjects().get(idObject) + quantity;
                second.getObjects().remove(idObject);
                second.getObjects().put(idObject, newQuantity);
                this.sendMoveOk(this.second.getCreature(), this.first.getCreature(), 'O', "+", idObject + "|" + newQuantity, add);
                return;
            }
            this.sendMoveOk(this.second.getCreature(), this.first.getCreature(), 'O', "+", str, add);
            this.second.getObjects().put(idObject, quantity);
        }
    }

    public void removeObject(int idObject, int quantity, int idPlayer) {
        this.first.setOk(false);
        this.second.setOk(false);

        this.sendOk(this.first);
        this.sendOk(this.second);

        Object object = manager.getObject(idObject);

        if (object == null)
            return;

        String add = "|" + object.getTemplate().getId() + "|" + object.parseEffects();

        if (this.first.getCreature().getId() == idPlayer) {
            int newQuantity = this.first.getObjects().get(idObject) - quantity;
            this.first.getObjects().remove(idObject);
            if (newQuantity < 1)
                this.sendMoveOk(this.first.getCreature(), this.second.getCreature(), 'O', "-", String.valueOf(idObject), "");
            else {
                this.first.getObjects().put(idObject, newQuantity);
                this.sendMoveOk(this.first.getCreature(), this.second.getCreature(), 'O', "+", idObject + "|" + newQuantity, add);
            }
        } else if (this.second.getCreature().getId() == idPlayer) {
            int newQuantity = this.second.getObjects().get(idObject) - quantity;
            this.second.getObjects().remove(idObject);
            if (newQuantity < 1)
                this.sendMoveOk(this.second.getCreature(), this.first.getCreature(), 'O', "-", String.valueOf(idObject), "");
            else {
                this.second.getObjects().put(idObject, newQuantity);
                this.sendMoveOk(this.second.getCreature(), this.first.getCreature(), 'O', "+", idObject + "|" + newQuantity, add);
            }
        }
    }

    public void toogleOk(int id) {
        if (this.first.getCreature().getId() == id) {
            this.first.setOk(!this.first.isOk());
            this.sendOk(this.first);
        } else if (this.second.getCreature().getId() == id) {
            this.second.setOk(!this.second.isOk());
            this.sendOk(this.second);
        } else
            return;


        if (this.first.isOk() && this.second.isOk())
            apply();
    }

    public void editKamas(int idPlayer, long kamas) {
        this.first.setOk(false);
        this.second.setOk(false);

        this.sendOk(this.first);
        this.sendOk(this.second);

        if (this.first.getCreature().getId() == idPlayer) {
            this.first.setKamas(kamas);
            this.sendMoveOk(this.first.getCreature(), this.second.getCreature(), 'G', "", String.valueOf(kamas), "");
        } else if (this.second.getCreature().getId() == idPlayer) {
            this.second.setKamas(kamas);
            this.sendMoveOk(this.second.getCreature(), this.first.getCreature(), 'G', "", String.valueOf(kamas), "");
        }
    }

    private void sendOk(Exchanger exchanger) {
        String packet = "EK" + (exchanger.isOk() ? "1" : "0") + exchanger.getCreature().getId();
        this.first.send(packet);
        this.second.send(packet);
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
        Map<Integer, Integer> objects;

        if (first.getCreature().getId() == creature.getId())
            objects = first.getObjects();
        else
            objects = first.getObjects();

        return objects.containsKey(idObject) ? objects.get(idObject) : 0;
    }
}