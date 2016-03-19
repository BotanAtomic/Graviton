package graviton.game.fight.type;


import graviton.api.Manager;
import graviton.game.fight.Fight;

import java.util.List;
import java.util.concurrent.ScheduledFuture;

/**
 * Created by Botan on 06/03/2016.
 */
public class FightManager implements Manager {

    private List<Fight> fights;

    private ScheduledFuture<?> scheduler;

    public void startFight(Fight fight) {

    }

    public void stopFight(Fight fight) {

    }

    @Override
    public void load() {

    }

    @Override
    public void unload() {

    }
}
