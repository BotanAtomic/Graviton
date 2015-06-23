package graviton.enums;

/**
 * Created by Botan on 11/06/2015.
 */
public enum AccountState {
    DISCONNECTED(0),
    LOGIN(1),
    GAME(2),
    BANNED(3);

    public final int id;

    AccountState(int id) {
        this.id = id;
    }

    public static AccountState getStateById(int id) {
        switch(id) {
            case 0 : return DISCONNECTED;
            case 1 : return LOGIN;
            case 2 : return GAME;
            case 3 : return BANNED;
        }
        return null;
    }

    public int getId() {
        return this.id;
    }
}
