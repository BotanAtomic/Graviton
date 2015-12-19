package graviton.game.guild;

import graviton.game.client.player.Player;
import lombok.Data;

/**
 * Created by Botan on 03/10/2015.
 */
@Data
public class GuildMember {
    private final int id;
    private final Guild guild;
    private final Player player;

    private int rank;
    private int right;

    private byte experienceGive;
    private long experienceGave;

    public GuildMember(Player player,int rank) {
        this.id = player.getId();
        this.guild = player.getGuild();
        this.player = player;
        this.rank = rank;
        this.right = 1;
        this.experienceGive = 0;
        this.experienceGave = 0;
    }


    public void send(String packet) {
        player.send(packet);
    }
}
