package graviton.game.job;

import graviton.game.client.player.Player;
import graviton.game.client.player.packet.Packets;
import graviton.game.job.actions.JobAction;
import graviton.game.job.actions.type.Craft;
import graviton.game.job.utils.CraftData;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Botan on 13/02/2016.
 */
@Data
public class Job {
    private final JobsTemplate template;

    private List<JobAction> jobActions;

    private final byte position;
    private final Player player;

    private long experience;
    private byte level= 1;

    public Job(byte position, JobsTemplate template, Player player) {
        this.position = position;
        this.template = template;
        this.experience = 0;
        this.player = player;
        this.jobActions = player.getGameManager().getJobFactory().getJobActions().get(template.getId()).get(this);
    }

    public Job(byte position, byte template, long experience, Player player) {
        this.position = position;
        this.template = JobsTemplate.get(template);
        this.experience = experience;
        this.player = player;
        this.configureLevel(experience);
        this.jobActions = player.getGameManager().getJobFactory().getJobActions().get((int)template).get(this);
    }

    private void configureLevel(long experience) {
        while (experience >= player.getGameManager().getJobExperience(level + 1) && level < 100)
            this.level++;
    }

    public void addExperience(byte experience) {
        this.experience += experience;

        while (this.experience >= player.getGameManager().getJobExperience(level + 1) && level < 100)
            levelUp();

        player.send("JX|" + template.getId() + ";" + level + ";" + getExperience(true) + ";");
    }

    private void levelUp() {
        this.level++;
        player.send("JN" + template.getId() + "|" + level);
        this.jobActions = player.getGameManager().getJobFactory().getJobActions().get(template.getId()).get(this);
        player.send("JS" + this.getJS());
    }

    public byte getLevel() {
        return this.level;
    }

    public String getJS() {
        StringBuilder builder = new StringBuilder();
        this.jobActions.forEach(jobAction -> builder.append(jobAction.toString()));
        return "|".concat(String.valueOf(this.template.getId()).concat(";")).concat(builder.length() > 2 ? builder.toString().substring(1) : builder.toString());
    }

    public String getExperience(boolean data) {
        if (!data)
            return String.valueOf(this.experience);

         return new StringBuilder().append(player.getGameManager().getJobExperience(level)).append(";").append(this.experience).append(";").append(player.getGameManager().getJobExperience(level + 1)).toString();
    }

}
