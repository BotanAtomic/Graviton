package graviton.game;


import graviton.login.Main;
import graviton.login.Manager;
import graviton.network.login.LoginClient;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Botan on 07/06/2015.
 */
@Slf4j
public class Account {
    @Getter
    private final int id;
    @Getter
    private final String name, password, question;

    @Getter
    @Setter
    private String pseudo;
    @Getter
    private int rank;
    @Getter
    @Setter
    private LoginClient client;
    @Getter
    private List<Player> players;

    public Account(int id, String name, String password, String pseudo, String question, int rank) {
        this.id = id;
        this.name = name;
        this.password = password;
        this.pseudo = pseudo;
        this.question = question;
        this.players = new ArrayList<>();
        this.rank = rank;
        Main.getInstance(Manager.class).getAccounts().put(id, this);
    }

    public final void delete() {
        try {
            if (Main.getInstance(Manager.class).getAccounts().get(id) != null)
                Main.getInstance(Manager.class).getAccounts().remove(id);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }
}
