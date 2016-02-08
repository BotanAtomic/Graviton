package graviton.game.creature.action;

import graviton.api.Action;

/**
 * Created by Botan on 09/10/2015.
 */
public class Animation implements Action {

    @Override
    public boolean start() {
        return false;
    }

    @Override
    public void cancel() {

    }

    @Override
    public void onFail(String args) {

    }

    @Override
    public void onSuccess(String args) {

    }

    @Override
    public int getId() {
        return 0;
    }

    @Override
    public int getAction() {
        return 0;
    }
}
