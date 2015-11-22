package graviton.common;

import com.google.inject.Inject;
import graviton.core.Manager;
import graviton.game.GameManager;
import graviton.network.exchange.ExchangeNetwork;
import lombok.extern.slf4j.Slf4j;
import org.fusesource.jansi.AnsiConsole;
import org.joda.time.Interval;
import org.joda.time.Period;

import java.util.Date;

/**
 * Created by Botan on 19/09/2015.
 */
@Slf4j
public class Scanner extends Thread implements graviton.api.Manager {
    @Inject
    ExchangeNetwork exchangeNetwork;
    @Inject
    GameManager gameManager;
    @Inject
    Manager manager;

    private final java.util.Scanner scanner;

    public Scanner() {
        this.scanner = new java.util.Scanner(System.in);
    }

    @Override
    public void start() {
        AnsiConsole.out.println("                 _____                     _  _                \n                / ____|                   (_)| |               \n               | |  __  _ __  __ _ __   __ _ | |_  ___   _ __  \n               | | |_ || '__|/ _` |\\ \\ / /| || __|/ _ \\ | '_ \\ \n               | |__| || |  | (_| | \\ V / | || |_| (_) || | | |\n                \\_____||_|   \\__,_|  \\_/  |_| \\__|\\___/ |_| |_|\n");
        AnsiConsole.out.append("\033]0;").append("Graviton - Game").append("\007");
        super.setDaemon(true);
        super.start();
    }

    @Override
    public void run() {
        while (true) {
            System.out.println("\nConsole > ");
            execute(scanner.next());
        }
    }

    public String launch(String line) {
        String message = "";
        switch (line.toLowerCase()) {
            case "restart":
                exchangeNetwork.send("RRestarting server...");
                System.exit(0);
                break;
            case "infos":
                StringBuilder builder = new StringBuilder();
                Period period = new Interval(manager.getDateOfStart().getTime(), new Date().getTime()).toPeriod();
                double currentMemory = (((double) (Runtime.getRuntime().totalMemory() / 1024) / 1024)) - (((double) (Runtime.getRuntime().freeMemory() / 1024) / 1024));
                builder.append("Number of accounts : " + (gameManager.getAccounts().size()));
                builder.append(";Number of players : " + gameManager.getPlayers().size());
                builder.append(";Number of loaded maps : " + gameManager.getMaps().size());
                builder.append(";Number of connected admin : " + gameManager.getAdmins().size() + gameManager.getAdminsName());
                builder.append(";Current memory usage: " + Double.toString(currentMemory).substring(0, 4) + " Mb / " + Double.toString(currentMemory / 8).substring(0, 4) + " Mo");
                builder.append(";Uptime : " + period.getDays() + "d " + period.getHours() + "h " + period.getMinutes() + "m " + period.getSeconds() + "s");
                message = builder.toString();
                break;
            default:
                message = ("Command not found");
        }
        return message;
    }

    private void execute(String line) {
        log.trace("User launch command : " + line);
        switch (line.toLowerCase()) {
            case "restart":
                System.exit(0);
                break;
            case "infos":
                ExchangeNetwork network = exchangeNetwork.launchPing();
                Period period = new Interval(manager.getDateOfStart().getTime(), new Date().getTime()).toPeriod();
                double currentMemory = (((double) (Runtime.getRuntime().totalMemory() / 1024) / 1024)) - (((double) (Runtime.getRuntime().freeMemory() / 1024) / 1024));
                System.out.println(" _________________________________________");
                System.out.println("| Number of accounts : " + (gameManager.getAccounts().size()));
                System.out.println("| Number of players : " + gameManager.getPlayers().size());
                System.out.println("| Number of loaded maps : " + gameManager.getMaps().size());
                System.out.println("| Number of connected admin : " + gameManager.getAdmins().size() + gameManager.getAdminsName());
                System.out.println("| Current memory usage: " + Double.toString(currentMemory).substring(0, 4) + " Mb / " + Double.toString(currentMemory / 8).substring(0, 4) + " Mo");
                System.out.println("| Login response time : " + network.getResponseTime() + "ms");
                System.out.println("| Uptime : " + period.getDays() + "d " + period.getHours() + "h " + period.getMinutes() + "m " + period.getSeconds() + "s");
                break;
            default:
                System.err.println("Command not found");
        }
    }

}
