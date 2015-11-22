package graviton.game.guild;

import graviton.game.client.player.Player;

/**
 * Created by Botan on 03/10/2015.
 */
public class Guild {
    private final int id;
   // private final String name;
    //private final int background,backgroundColor;
    //private final int motif,motifColor;

    public Guild(int id,String parameters) {
        this.id = id;
        String[] arguements = parameters.split("\\|");
    }

    public void addMember(Player player) {

    }

    public void removeMember(Player player) {

    }

    public void send(String packet) {

    }
}
