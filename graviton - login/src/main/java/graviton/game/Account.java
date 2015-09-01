package graviton.game;


import graviton.login.Login;
import graviton.login.Main;
import graviton.network.login.LoginClient;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Botan on 07/06/2015.
 */
@Data
public class Account {
    private final int id;
    private final String name, password, question;

    private String pseudo;
    private int rank;
    private LoginClient client;
    private List<Player> players;

    public Account(int id, String name, String password, String pseudo, String question,int rank) {
        this.id = id;
        this.name = name;
        this.password = password;
        this.pseudo = pseudo;
        this.question = question;
        this.players = new ArrayList<>();
        this.rank = rank;
        Main.getInstance(Login.class).getAccounts().put(id,this);
    }

    public final void delete() {

    }
}
