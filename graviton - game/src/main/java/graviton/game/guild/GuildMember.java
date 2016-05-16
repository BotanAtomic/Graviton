package graviton.game.guild;

import graviton.game.client.player.Player;
import lombok.Data;
import org.joda.time.Days;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by Botan on 03/10/2015.
 */
@Data
public class GuildMember {
    private Player player;

    private final int id;
    private final Guild guild;
    private final String name;

    private int gfx, level;
    private String lastConnection;

    private int rank;
    private int right;

    private byte experienceGive,align;
    private long experienceGave;

    private Map<GuildRight, Boolean> rights;

    public GuildMember(int id, Guild guild, String name, byte align, int gfx, int level, String lastConnection, int rank, int right) {
        this.id = id;
        this.guild = guild;
        this.rank = rank;
        this.name = name;
        this.align = align;
        this.gfx = gfx;
        this.right = right;
        this.lastConnection = lastConnection;
        this.level = level;
        this.experienceGive = 0;
        this.experienceGave = 0;
        this.rights = new HashMap<>();
    }

    public void attributePlayer(Player player) {
        this.player = player;
        this.lastConnection = new SimpleDateFormat("dd-MM-yyyy").format(new Date());
    }

    public void send(String packet) {
        player.send(packet);
    }

    public long getLastConnectionHours() {
        try {
            return TimeUnit.HOURS.convert(new Date().getTime() - new SimpleDateFormat("dd-MM-yyyy").parse(lastConnection).getTime(), TimeUnit.MILLISECONDS);
        } catch (ParseException e) {
            return 0;
        }
    }

    public String getRights() {
        return Integer.toString(this.right, 36);
    }

    private enum GuildRight {
        SET_BOOST(2),                // Gerer les boosts
        SET_RIGHT(4),                // Gerer les droits
        CAN_INVITE(8),            // Inviter des joueurs
        CAN_BAN(16),                // Bannir un joueur
        ALL_XP(32),                // Gerer les xps
        HIS_XP(64),                // Gerer son xp
        SET_RANK(128),            // Gerer les rangs
        POS_COLLECTOR(256),        // Poser un percepteur
        GET_COLLECTOR(512),        // Prendre les percepteurs
        /**
         * 1024 & 2048 ?
         **/
        USE_PARK(4096),            // Utiliser les enclos
        ADJUST_PARK(8192),            // Amenager les enclos
        ADJUST_OTHER_MOUNT(16384);    // Amenager les montures

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
