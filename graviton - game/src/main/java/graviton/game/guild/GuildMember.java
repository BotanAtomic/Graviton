package graviton.game.guild;

import graviton.game.client.player.Player;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

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

    private Map<Integer,Boolean> rights;

    public GuildMember(Player player,int rank, int right) {
        this.id = player.getId();
        this.guild = player.getGuild();
        this.player = player;
        this.rank = rank;
        this.right = right;
        this.experienceGive = 0;
        this.experienceGave = 0;
        this.rights = new HashMap<>();
        this.configureRight(right);
    }

    private void initRight(boolean withRight) {
        for(GuildRight right : GuildRight.values())
            this.rights.put(right.getId(), withRight);
    }

    public void configureRight(int right) {


    }

    public void send(String packet) {
        player.send(packet);
    }

    enum GuildRight {
        SET_BOOST(2),			 	// Gerer les boosts
        SET_RIGHT(4), 			 	// Gerer les droits
        CAN_INVITE(8),  		 	// Inviter des joueurs
        CAN_BAN(16),  				// Bannir un joueur
        ALL_XP(32),   				// Gerer les xps
        HIS_XP(64),   				// Gerer son xp
        SET_RANK(128),   			// Gerer les rangs
        POS_COLLECTOR(256), 		// Poser un percepteur
        GET_COLLECTOR(512), 		// Prendre les percepteurs
        /** 1024 & 2048 ? **/
        USE_PARK(4096), 			// Utiliser les enclos
        ADJUST_PARK(8192), 			// Amenager les enclos
        ADJUST_OTHER_MOUNT(16384); 	// Amenager les montures

        private final int id;

        GuildRight(int id) {
            this.id = id;
        }

        public int getId() {
            return id;
        }

        public String toString() {
            return String.valueOf(id);
        }
    }
}
