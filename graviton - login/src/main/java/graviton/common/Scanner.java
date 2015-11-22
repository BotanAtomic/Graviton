package graviton.common;

import com.google.inject.Inject;
import graviton.login.Manager;
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
public class Scanner extends Thread {
    @Inject
    ApplicationNetwork network;

    private final java.util.Scanner scanner;
    private Manager manager;

    public Scanner() {
        this.scanner = new java.util.Scanner(System.in);
    }

    public void start(Manager manager) {
        AnsiConsole.out.println("                 _____                     _  _                \n                / ____|                   (_)| |               \n               | |  __  _ __  __ _ __   __ _ | |_  ___   _ __  \n               | | |_ || '__|/ _` |\\ \\ / /| || __|/ _ \\ | '_ \\ \n               | |__| || |  | (_| | \\ V / | || |_| (_) || | | |\n                \\_____||_|   \\__,_|  \\_/  |_| \\__|\\___/ |_| |_|\n");
        AnsiConsole.out.append("\033]0;").append("Graviton - Login").append("\007");
        super.setDaemon(true);
        super.start();
        this.manager = manager;
    }

    @Override
    public void run() {
        while (!super.isInterrupted()) {
            System.out.println("\nConsole > ");
            execute(scanner.next());
        }
    }

    private void execute(String line) {
        log.trace("User launch command : " + line);
        switch (line.toLowerCase()) {
            case "restart":
                System.exit(0);
                break;
            case "infos":
                Period period = new Interval(manager.getDateOfStart().getTime(), new Date().getTime()).toPeriod();
                double currentMemory = (((double) (Runtime.getRuntime().totalMemory() / 1024) / 1024)) - (((double) (Runtime.getRuntime().freeMemory() / 1024) / 1024));
                System.out.println(" ______________________________________________");
                System.out.println("| Total server : " + (manager.getServers().size()) + manager.getServerName(false));
                System.out.println("| Number of connected servers : " + manager.getExchangeClients().size() + manager.getServerName(true));
                System.out.println("| Number of connected clients on the manager : " + manager.getLoginClients().size());
                System.out.println("| Application is connected : " + network.applicationIsConnected());
                System.out.println("| Process PID : " + ManagementFactory.getRuntimeMXBean().getName().split("@")[0]);
                System.out.println("| Client who passed and connected : " + manager.getConnected().size());
                System.out.println("| Memory usage: " + Double.toString(currentMemory).substring(0, 4) + " Mb / " + Double.toString(currentMemory / 8).substring(0, 4) + " Mo");
                System.out.println("| Uptime : " + period.getDays() + "d " + period.getHours() + "h " + period.getMinutes() + "m " + period.getSeconds() + "s");
                break;
            default:
                System.err.println("Command not found");
        }
    }


}
