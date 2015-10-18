package graviton.game.admin;

import graviton.core.Main;
import graviton.game.GameManager;
import graviton.game.client.Account;
import graviton.game.enums.Rank;


/**
 * Created by Botan on 01/10/2015.
 */
public class Admin {
    private final Rank rank;
    private final Account account;

    public Admin(Rank rank, Account account) {
        this.rank = rank;
        this.account = account;
        Main.getInstance(GameManager.class).getAdmins().add(this);
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
