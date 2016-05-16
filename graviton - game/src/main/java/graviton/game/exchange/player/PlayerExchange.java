package graviton.game.exchange.player;

import graviton.game.GameManager;
import graviton.game.client.player.Player;
import graviton.game.action.player.ActionManager;
import graviton.game.client.player.packet.Packets;
import graviton.game.exchange.api.Exchange;
import graviton.game.exchange.api.Exchanger;
import graviton.game.object.Object;

import java.util.Arrays;
import java.util.Map;

/**
 * Created by Botan on 25/12/2015.
 */

public class PlayerExchange implements Exchange {

    private final Exchanger firstExchanger;
    private final Exchanger secondExchanger;

    private final GameManager manager;

    public PlayerExchange(Player player1, Player player2) {
        this.firstExchanger = new Exchanger(player2);
        this.secondExchanger = new Exchanger(player1);

        this.manager = player1.getGameManager();

        player1.setExchange(this);
        player2.setExchange(this);

        Arrays.asList(player1, player2).forEach(exchanger -> exchanger.getActionManager().setStatus(ActionManager.Status.EXCHANGING));
    }

    public void cancel() {
        Arrays.asList(firstExchanger, secondExchanger).forEach(exchanger -> {
            exchanger.send("EV");
            exchanger.quit();
            exchanger.getCreature().getActionManager().resetActions();
        });
    }

    public void apply() {
        Arrays.asList(firstExchanger, secondExchanger).forEach(exchanger -> apply(exchanger));

        Arrays.asList(firstExchanger, secondExchanger).forEach(exchanger -> {
            exchanger.send("EVa");
            exchanger.quit();
            exchanger.send(exchanger.getCreature().getPacket(Packets.As));
            exchanger.getCreature().getActionManager().resetActions();
            exchanger.getCreature().refreshQuantity();
            exchanger.getCreature().save();
        });
    }

    private void apply(Exchanger exchanger) {
        Exchanger other = exchanger == firstExchanger ? secondExchanger : firstExchanger;

        exchanger.getObjects().keySet().forEach(i -> {
            int quantity = exchanger.getObjects().get(i);
            if (quantity != 0 && exchanger.getCreature().hasObject(i)) {
                Object object = exchanger.getCreature().getObjects().get(i);

                if ((object.getQuantity() - quantity) < 1) {
                    exchanger.getCreature().removeObject(i, true);
                    other.getCreature().addObject(object, true);
                } else {
                    exchanger.getCreature().setObjectQuantity(object, (object.getQuantity() - quantity));
                    other.getCreature().addObject(object.getClone(quantity, false), true);
                }
            } else
                exchanger.getObjects().remove(i);
        });
        setKamas(exchanger, other);
    }

    private void setKamas(Exchanger exchanger, Exchanger other) {
        exchanger.getCreature().addKamas(-exchanger.getKamas());
        other.getCreature().addKamas(exchanger.getKamas());
    }

    public void addObject(int idObject, int quantity, int idPlayer) {
        Arrays.asList(firstExchanger, secondExchanger).forEach(exchanger -> {
            exchanger.setReady(false);
            this.sendOk(exchanger);
        });

        Exchanger exchanger = idPlayer == firstExchanger.getCreature().getId() ? firstExchanger : secondExchanger;
        Exchanger other = exchanger == firstExchanger ? secondExchanger : firstExchanger;

        Object object = exchanger.getCreature().getObjects().get(idObject);

        if (object == null)
            return;

        String add = "|" + object.getTemplate().getId() + "|" + object.parseEffects();

        if (exchanger.getObjects().get(idObject) != null) {
            int newQuantity = exchanger.getObjects().get(idObject) + quantity;
            exchanger.getObjects().remove(idObject);
            exchanger.getObjects().put(idObject, newQuantity);
            this.sendMoveOk(exchanger.getCreature(), other.getCreature(), 'O', "+", idObject + "|" + newQuantity, add);
            return;
        }
        this.sendMoveOk(exchanger.getCreature(), other.getCreature(), 'O', "+", idObject + "|" + quantity, add);
        exchanger.getObjects().put(idObject, quantity);
    }

    public void removeObject(int idObject, int quantity, int idPlayer) {
        Arrays.asList(firstExchanger, secondExchanger).forEach(exchanger -> {
            exchanger.setReady(false);
            this.sendOk(exchanger);
        });

        Exchanger exchanger = idPlayer == firstExchanger.getCreature().getId() ? firstExchanger : secondExchanger;
        Exchanger other = exchanger == firstExchanger ? secondExchanger : firstExchanger;
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
        Exchanger exchanger = id == firstExchanger.getCreature().getId() ? firstExchanger : secondExchanger;
        exchanger.setReady(!exchanger.isReady());
        this.sendOk(exchanger);
        if (exchanger.isReady() && (exchanger == firstExchanger ? secondExchanger : firstExchanger).isReady())
            apply();
    }

    public void editKamas(int idPlayer, long kamas) {
        Arrays.asList(firstExchanger, secondExchanger).forEach(exchanger -> {
            exchanger.setReady(false);
            this.sendOk(exchanger);
        });
        Exchanger exchanger = idPlayer == firstExchanger.getCreature().getId() ? firstExchanger : secondExchanger;
        exchanger.setKamas(kamas);
        this.sendMoveOk(exchanger.getCreature(), (exchanger == firstExchanger ? secondExchanger : firstExchanger).getCreature(), 'G', "", String.valueOf(kamas), "");
    }

    private void sendOk(Exchanger target) {
        String packet = "EK" + (target.isReady() ? "1" : "0") + target.getCreature().getId();
        Arrays.asList(firstExchanger, secondExchanger).forEach(exchanger -> exchanger.send(packet));
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
        Map<Integer, Integer> objects = (creature.getId() == firstExchanger.getCreature().getId() ? firstExchanger : secondExchanger).getObjects();
        return objects.containsKey(idObject) ? objects.get(idObject) : 0;
    }
}