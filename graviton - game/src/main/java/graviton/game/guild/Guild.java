package graviton.game.guild;

import graviton.game.client.player.Player;
import lombok.Data;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Botan on 03/10/2015.
 */
@Data
public class Guild {
    private final int id;
    private final String name, emblem;

    private final Map<Integer, GuildMember> members;

    private long experience;
    private int level;
    private int capital;

    public Guild(int id, String name, String emblem) {
        this.id = id;
        this.name = name;
        this.emblem = emblem;
        this.members = new ConcurrentHashMap<>();

        this.experience = 0;
        this.level = 1;
        this.capital = 0;
    }

    public Guild(int id, String name, String emblem, int level, long experience, int capital) {
        this.id = id;
        this.name = name;
        this.emblem = emblem;
        this.members = new ConcurrentHashMap<>();

        this.experience = experience;
        this.level = level;
        this.capital = capital;
    }

    public void addMember(GuildMember member) {
        this.members.put(member.getId(), member);
    }

    public void sendPacket(GuildMember member) {
        member.send("gS" + (name) + ("|") + (emblem.replace(',', '|')) + ("|") + Integer.toString(member.getRight(), 36));
        member.send("gCK");
        member.send("gV");
    }

    public void removeMember(Player player) {

    }

    public void send(String packet) {
        this.members.values().forEach(guildMember -> guildMember.send(packet));
    }

    public String toString() {
        return null;
    }

    public String getMembersGm() {
        final StringBuilder builder = new StringBuilder();
        this.members.values().forEach(member -> {
            builder.append(member.getId()).append(";");
            builder.append(member.getName()).append(";");
            builder.append(member.getLevel()).append(";");
            builder.append(member.getGfx()).append(";");
            builder.append(member.getRank()).append(";");
            builder.append(member.getExperienceGave()).append(";");
            builder.append(member.getExperienceGive()).append(";");
            builder.append(member.getRights()).append(";");
            builder.append(member.getPlayer() == null ? "0" : member.getPlayer().isOnline() ? "1" : "0").append(";");
            builder.append(member.getAlign()).append(";");
            builder.append(member.getLastConnectionHours());
        });
        return builder.toString();
    }
}
