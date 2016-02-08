package graviton.game.exchange;


import graviton.game.client.player.Player;

/**
 * Created by Botan on 25/12/2015.
 */

public abstract class Exchange {

    private final Exchanger exchanger1;
    private final Exchanger exchanger2;

    public Exchange(Player creature1, Player creature2) {
        this.exchanger1 = new Exchanger(creature1);
        this.exchanger2 = new Exchanger(creature2);
    }

    public synchronized void cancel() {
        doCancel();
    }

    protected abstract void doCancel();

    public synchronized void apply() {
        doApply();
    }

    protected abstract void doApply();

    public synchronized void toogleOk(int id) {
        doToogleOk(id);
    }

    protected abstract void doToogleOk(int id);

    public synchronized void addObject(int idObject, int quantity, int idPlayer) {
        doAddObject(idObject, quantity, idPlayer);
    }

    protected abstract void doAddObject(int idObject, int quantity, int idPlayer);

    public synchronized void removeObject(int idObject, int quantity, int idPlayer) {
        doRemoveObject(idObject, quantity, idPlayer);
    }

    protected abstract void doRemoveObject(int idObject, int quantity, int idPlayer);

    public synchronized void editKamas(int idPlayer, long kamas) {
        doEditKamas(idPlayer, kamas);
    }

    protected abstract void doEditKamas(int idPlayer, long kamas);

}