package graviton.console;

import com.google.inject.Singleton;
import graviton.core.Main;
import graviton.core.Server;
import graviton.game.GameManager;
import org.fusesource.jansi.AnsiConsole;

import java.io.PrintStream;
import java.util.NoSuchElementException;
import java.util.Scanner;

/**
 * Created by Botan on 16/06/2015.
 */
@Singleton
public class Console extends Thread {

    private final Logger logger;
    private Server manager;
    private Scanner scanner;

    public Console() {
        this.logger = new Logger();
        super.setDaemon(true);
    }

    public void initialize(Server manager) {
        this.scanner = new Scanner(System.in);
        this.manager = manager;
        this.initializeEmulatorName();
        super.start();
    }

    @Override
    public void run() {
        while (manager.isRunning()) {
            try {
                AnsiConsole.out.println("Console > \n");
                parse(scanner.nextLine());
            } catch (NoSuchElementException ignored) {

            }
        }
        super.interrupt();
    }

    private void initializeEmulatorName() {
        AnsiConsole.out.println("\033[" + getRandomColor() + "m" + "                 _____                     _  _                \n                / ____|                   (_)| |               \n               | |  __  _ __  __ _ __   __ _ | |_  ___   _ __  \n               | | |_ || '__|/ _` |\\ \\ / /| || __|/ _ \\ | '_ \\ \n               | |__| || |  | (_| | \\ V / | || |_| (_) || | | |\n                \\_____||_|   \\__,_|  \\_/  |_| \\__|\\___/ |_| |_|" + "\033[" + 0 + "m");
        AnsiConsole.out.println();
        this.setTitle();
    }

    private void parse(String line) {
        switch (line.toLowerCase()) {
            case "stop":
                println("Closing server..", false);
                System.exit(1);
                break;
            case "close":
                println("Closing server..", false);
                System.exit(1);
                break;
            case "restart":
                println("Restarting server..", false);
                System.exit(0);
                break;
            case "ram" :
                double currentMemory = ( ((double)(Runtime.getRuntime().totalMemory()/1024)/1024))- (((double)(Runtime.getRuntime().freeMemory()/1024)/1024));
                AnsiConsole.out.println("Current memory usage: " + Double.toString(currentMemory).substring(0, 4) + " Mb / " + Double.toString(currentMemory / 8).substring(0, 4) + " Mo");
                break;
            case "save" :
                Main.getInstance(GameManager.class).save();
            case "clean" :
                Runtime.getRuntime().gc();
                break;
            case "help" :
                AnsiConsole.err.println("List controls..\n");
                AnsiConsole.err.println("stop / close > turn off the server");
                AnsiConsole.err.println("restart > restart the server");
                AnsiConsole.err.println("ram > view current memory usage");
                AnsiConsole.err.println("clean > clean memory,thread and empty class");
                break;
            default :
                AnsiConsole.out.println("Command ["+line+"] not found - > help ?");
                break;
        }
    }

    public void println(String line, boolean error) {
        PrintStream printer = error ? AnsiConsole.err : AnsiConsole.out;
        printer.println(line);
        logger.add(line, error);
    }

    public void addToLogs(String line, boolean error) {
        logger.add(line, error);
    }

    public void println(String line) {
        AnsiConsole.out.println(line);
        logger.add(line, false);
    }

    private void setTitle() {
        AnsiConsole.out.append("\033]0;").append("Graviton - Game").append("\007");
    }

    private int getRandomColor() {
        int randomNum = (int) (Math.random() * 4);
        switch (randomNum) {
            case 0:
                return 34;
            case 1:
                return 31;
            case 2:
                return 33;
            case 3:
                return 35;
            case 4:
                return 46;
            default:
                return getRandomColor();
        }
    }

}
