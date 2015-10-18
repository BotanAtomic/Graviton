package graviton.network.attack;


import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by Botan on 04/10/2015.
 */
public class Attack {
    private ScheduledExecutorService scheduler;


    public Attack(String ip, int time) {
        this.scheduler = Executors.newScheduledThreadPool(1);

    }

    private void stop() {

    }

    private void schedule(int time) {
        scheduler.schedule(() -> stop(), time, TimeUnit.MINUTES);
    }


}
