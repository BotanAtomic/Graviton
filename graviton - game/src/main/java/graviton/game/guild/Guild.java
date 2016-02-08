package graviton.game.guild;

import graviton.game.client.player.Player;
import lombok.Data;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by Botan on 03/10/2015.
 */
@Data
public class Guild {
    private final String name,emblem;

    private final List<GuildMember> members;

    private final ReentrantLock locker;

    private long experience;
    private int level;
    private int capital;

    public Guild(String name,String emblem) {
        this.name = name;
        this.emblem = emblem;
        this.members = new CopyOnWriteArrayList<>();
        this.locker = new ReentrantLock();

        this.experience = 0;
        this.level = 1;
        this.capital = 0;
    }

    public void addMember(GuildMember member) {
        member.send("gS" + (name) + ("|") + (emblem.replace(',', '|')) + ("|") + Integer.toString(member.getRight(), 36));
        member.send("gCK");
        member.send("gV");
        this.members.add(member);
    }

    public void removeMember(Player player) {

    }

    public void send(String packet) {
        locker.lock();
        try {
            this.members.forEach(guildMember -> guildMember.send(packet));
        } finally {
            locker.unlock();
        }
    }

    public String getMemberForDatabase() {
        final String[] list = {""};
        locker.lock();
        try {
            this.members.forEach(guildMember -> list[0] += guildMember.getId() + ";");
        } finally {
            locker.unlock();
        }
        return list[0].substring(0,list[0].length() - 1);
    }

    public String getMembersGm(){
        final String[] packet = {""};
        locker.lock();
        try {
            this.members.forEach(guildMember ->{
                if(packet[0].length() != 0)
                    packet[0] += "|";
                packet[0] += guildMember.getId() + ";";
                packet[0] += guildMember.getPlayer().getName() + ";";
                packet[0] += guildMember.getPlayer().getLevel() + ";";
                packet[0] += guildMember.getPlayer().getGfx() + ";";
                packet[0] += guildMember.getRank() + ";";
                packet[0] += guildMember.getExperienceGave() + ";";
                packet[0] += guildMember.getExperienceGive() + ";";
                packet[0] += guildMember.getRight() + ";";
                packet[0] += (guildMember.getPlayer().isOnline() ? "1" : "0") + ";";
                packet[0] += guildMember.getPlayer().getAlignement().getType().getId() + ";";
                packet[0] += "" + ";"; //TODO : last connexion
            });
        } finally {
            locker.unlock();
        }
        return packet[0];
    }
}
