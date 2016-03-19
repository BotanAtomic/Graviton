package graviton.game;


import com.google.inject.Inject;
import com.google.inject.Injector;
import graviton.login.Manager;
import graviton.network.login.LoginClient;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Botan on 07/06/2015.
 */
@Slf4j
@Data
public class Account {
    private final int id, rank;
    private final String pseudo;
    @Inject
    Manager manager;
    private String name, password, question;
    private LoginClient client;
    private List<Player> players;

    public Account(int id, String name, String password, String pseudo, String question, int rank,Injector injector) {
        injector.injectMembers(this);
        this.id = id;
        this.name = name;
        this.password = password;
        this.pseudo = pseudo;
        this.question = question;
        this.players = new ArrayList<>();
        this.rank = rank;
        manager.getAccounts().put(id, this);
    }
    public Account(String pseudo, int rank,Injector injector) {
        injector.injectMembers(this);
        this.id = 0;
        this.pseudo = pseudo;
        this.rank = rank;
    }

    public void delete() {
        try {
            if (manager.getAccounts().get(id) != null)
                manager.getAccounts().remove(id);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }
}
