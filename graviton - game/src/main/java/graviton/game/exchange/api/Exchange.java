package graviton.game.exchange.api;


import graviton.game.client.player.Player;

/**
 * Created by Botan on 25/12/2015.
 */

public interface Exchange {

    void cancel();

    void apply();

    void toogleOk(int id);

    void addObject(int idObject, int quantity, int idPlayer);

    void removeObject(int idObject, int quantity, int idPlayer);

    void editKamas(int idPlayer, long kamas);

    int getObjectQuantity(Player player,int id);

}