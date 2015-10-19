package graviton.game.admin;

import com.google.inject.Inject;
import com.google.inject.Injector;
import graviton.core.Main;
import graviton.game.GameManager;
import graviton.game.client.Account;
import graviton.game.enums.Rank;


/**
 * Created by Botan on 01/10/2015.
 */
public class Admin {
    @Inject
    GameManager gameManager;

    private final Rank rank;
    private final Account account;

    public Admin(Rank rank, Account account,Injector injector) {
        injector.injectMembers(this);
        this.rank = rank;
        this.account = account;
        gameManager.getAdmins().add(this);
    }

    public boolean ban(Account account, String time, boolean banIp) {
        if (rank.id < Rank.MANAGER.id) return false;
        //TODO : ban
        return true;
    }

    public void mute(Account account, int time, String reason) {
        account.mute(time, this.account.getCurrentPlayer(), reason);
    }

    public void launchCommand(String command) {

    }

    public Rank getRank() {
        return this.rank;
    }

    public Account getAccount() {
        return this.account;
    }
}
