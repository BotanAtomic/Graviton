package graviton.game.job;

import graviton.game.client.player.Player;

/**
 * Created by Botan on 13/02/2016.
 */
public class Job {
    private final int id;
    private final Player player;

    private long experience;

    public Job(int id,long experience,Player player) {
        this.id = id;
        this.experience = experience;
        this.player = player;
    }

    public void addExperience(long experience) {
        this.experience += experience;
    }


}
