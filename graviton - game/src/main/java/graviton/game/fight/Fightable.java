package graviton.game.fight;

import graviton.game.enums.IdType;

/**
 * Created by Botan on 13/04/2016.
 */
public interface Fightable {
    void setFighter(Fighter fighter);

    String getFightGm();

    IdType getType();
}
