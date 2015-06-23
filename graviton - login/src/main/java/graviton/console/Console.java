package graviton.console;

import graviton.login.LoginManager;
import org.fusesource.jansi.AnsiConsole;

import java.io.PrintStream;
import java.util.NoSuchElementException;
import java.util.Scanner;

/**
 * Created by Botan on 15/06/2015.
 */
public class Console extends Thread {
    private LoginManager manager;
    private Scanner scanner;
    private Logger logger;

    public Console() {
        this.logger = new Logger();
    }

    public void initialize(LoginManager manager) {
        this.scanner = new Scanner(System.in);
        super.setDaemon(true);
        super.start();
        this.manager = manager;
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

    public void initializeEmulatorName() {
        int color = this.getRandomColor();
        String name = ("  _______  ______           ___   ____    ____  __   ___________   ______    __   __ \n /  _____||   _  \\         /   \\  \\   \\  /   / |  | |           | /  __  \\  |  \\ |  |\n|  |  __  |  |_)  |       /  ^  \\  \\   \\/   /  |  | `---|  |----`|  |  |  | |   \\|  |\n|  | |_ | |      /       /  /_\\  \\  \\      /   |  |     |  |     |  |  |  | |  . `  |\n|  |__| | |  |\\  \\____  /  _____  \\  \\    /    |  |     |  |     |  `--'  | |  |\\   |\n \\______| | _| `______|/__/     \\__\\  \\__/     |__|     |__|      \\______/  |__| \\__|");
        AnsiConsole.out.println("\033[" + color + "m" + name + "\033[" + 0 + "m");
        AnsiConsole.out.println();
        this.setTitle("Graviton - Login");
    }

    public void parse(String line) {
        switch (line.toLowerCase()) {
            case "stop":
                println("Closing server..", false);
                System.exit(1);
                break;
            case "restart":
                println("Restarting server..", false);
                System.exit(0);
                break;
        }
    }

    public void println(String line, boolean error) {
        logger.add(line, error);
        PrintStream printer = error ? AnsiConsole.err : AnsiConsole.out;
        printer.println(line);
    }

    public void setTitle(String title) {
        AnsiConsole.out.append("\033]0;").append(title).append("\007");
    }

    public void println(String line, int color) {
        logger.add(line, false);
        AnsiConsole.out.println("\033[" + color + "m" + line + "\033[" + 0 + "m");
    }

    public int getRandomColor() {
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
