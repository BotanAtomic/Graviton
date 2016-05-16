package graviton.game.admin;

import com.google.inject.Inject;
import com.google.inject.Injector;
import graviton.game.GameManager;
import graviton.game.client.Account;
import graviton.game.enums.Rank;


/**
 * Created by Botan on 01/10/2015.
 */
public class Admin {
    private final Rank rank;
    private final Account account;
    @Inject
    private GameManager gameManager;

    public Admin(Account account,Injector injector) {
        injector.injectMembers(this);
        this.rank = account.getRank();
        this.account = account;
        gameManager.getAdmins().add(this);
    }

    public void remove() {
        gameManager.getAdmins().remove(this);
    }

    public Rank getRank() {
        return this.rank;
    }

    public Account getAccount() {
        return this.account;
    }
}
