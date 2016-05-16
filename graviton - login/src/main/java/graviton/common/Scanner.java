package graviton.common;

import com.google.inject.Inject;
import graviton.api.Manageable;
import graviton.core.GlobalManager;
import graviton.network.application.ApplicationNetwork;
import lombok.extern.slf4j.Slf4j;
import org.fusesource.jansi.AnsiConsole;
import org.joda.time.Interval;
import org.joda.time.Period;

import java.lang.management.ManagementFactory;
import java.util.Date;

/**
 * Created by Botan on 19/09/2015.
 */
@Slf4j
public class Scanner extends Thread implements Manageable {
    private final GlobalManager globalManager;
    private final Date startTime = new Date();
    private final java.util.Scanner scanner;

    @Inject
    ApplicationNetwork network;

    @Inject
    public Scanner(GlobalManager globalManager) {
        globalManager.addManageable(this);
        this.scanner = new java.util.Scanner(System.in);
        this.globalManager = globalManager;
    }

    @Override
    public void run() {
        while (!super.isInterrupted()) {
            System.out.println("\nConsole > ");
            execute(scanner.next());
        }
    }

    /**
     * long totalMemory = Runtime.getRuntime().totalMemory() / 1024 / 1024;
     * long freeMemory = Runtime.getRuntime().freeMemory() / 1024 / 1024;
     * <p>
     * <p>
     * StringBuilder builder = new StringBuilder("\n\n<u>Statistiques du serveur</u>\n\n");
     * <p>
     * builder.append((gameManager.getElements(DataType.ACCOUNT).size()) + " compte(s) chargé(s)\n");
     * builder.append((gameManager.getElements(DataType.PLAYER).size()) + " personnage(s) chargé(s)\n");
     * builder.append((gameManager.getElements(DataType.MAPS).size()) + " carte(s) chargé(s)\n\n");
     * <p>
     * builder.append("Mémoire maximale : " + totalMemory + " Mb\n");
     * builder.append("Mémoire libre : " + freeMemory + " Mb\n");
     * builder.append("Mémoire utilisée : " + (totalMemory - freeMemory) + " Mb\n\n");
     * <p>
     * builder.append("<u>" + Thread.activeCount() + " threads actifs </u>\n");
     * Thread.getAllStackTraces().keySet().forEach(thread -> builder.append(thread + " (".concat(thread.getState().name()).concat(")\n")));
     * <p>
     * player.getAccount().getClient().send("BAT0".concat(builder.toString()));
     *
     * @param line
     */

    private void execute(String line) {
        if (line.equalsIgnoreCase("restart")) {
            System.exit(0);
        } else if (line.equalsIgnoreCase("infos")) {
            long totalMemory = Runtime.getRuntime().totalMemory() / 1024 / 1024;
            long freeMemory = Runtime.getRuntime().freeMemory() / 1024 / 1024;
            Period period = new Interval(startTime.getTime(), new Date().getTime()).toPeriod();
            System.out.println(" ______________________________________________");
            System.out.println("| Servers : " + (globalManager.getServers().size()) + globalManager.getServerName(false));
            System.out.println("| Connected servers : " + globalManager.getExchangeClients().size() + globalManager.getServerName(true));
            System.out.println("| Connected clients : " + globalManager.getLoginClients().size());
            System.out.println("| Application : " + network.applicationIsConnected());
            System.out.println("| Process PID : " + ManagementFactory.getRuntimeMXBean().getName().split("@")[0]);
            System.out.println("| Transfer to server  : " + globalManager.getConnectedClient().size());
            System.out.println("| Max memory : " + +totalMemory + " Mb");
            System.out.println("| Free memory : " + +freeMemory + " Mb");
            System.out.println("| Memory usage : " + (totalMemory - freeMemory) + " Mb");
            System.out.println("| Active thread : " + Thread.activeCount());
            System.out.println("| Uptime : " + period.getDays() + "d " + period.getHours() + "h " + period.getMinutes() + "m " + period.getSeconds() + "s");
        } else if (line.equalsIgnoreCase("thread")) {
            Thread.getAllStackTraces().keySet().forEach(thread -> System.out.println(thread + " (".concat(thread.getState().name()).concat(")")));
        } else {
            System.err.println("Command not found");
        }
    }


    @Override
    public void configure() {
        AnsiConsole.out.println("                 _____                     _  _                \n                / ____|                   (_)| |               \n               | |  __  _ __  __ _ __   __ _ | |_  ___   _ __  \n               | | |_ || '__|/ _` |\\ \\ / /| || __|/ _ \\ | '_ \\ \n               | |__| || |  | (_| | \\ V / | || |_| (_) || | | |\n                \\_____||_|   \\__,_|  \\_/  |_| \\__|\\___/ |_| |_|\n");
        AnsiConsole.out.append("\033]0;").append("Graviton - Login").append("\007");
        super.setDaemon(true);
        super.start();
    }
}
