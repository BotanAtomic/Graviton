package graviton.game.fight;

/**
 * Created by Botan on 22/06/2015.
 */
public interface Fight {

    enum FightType {
        DEFY(0), PVP(1), PVM(4), COLLECTOR(5);
        public final int id;

        FightType(int id) {
            this.id = id;
        }
    }

}


