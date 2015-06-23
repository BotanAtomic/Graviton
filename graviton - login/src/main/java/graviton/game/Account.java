package graviton.game;


import graviton.network.login.LoginClient;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Botan on 07/06/2015.
 */
public class Account {
    @Getter
    @Setter
    private int id, rank;
    @Getter
    @Setter
    private String name, password, pseudo, question;
    @Getter
    @Setter
    private LoginClient client;
    @Getter
    @Setter
    private List<Player> players;

    public Account(int id,String name,String password,String pseudo,String question) {
        this.id = id;
        this.name = name;
        this.password = password;
        this.pseudo = pseudo;
        this.question = question;
        this.players = new ArrayList<>();
    }

    public Account(int id, String name) {
        this.id = id;
        this.name = name;
        this.players = new ArrayList<>();
    }
}
