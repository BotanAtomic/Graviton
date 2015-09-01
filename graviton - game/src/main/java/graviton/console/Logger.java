package graviton.console;

import graviton.common.Pair;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by Botan on 16/06/2015.
 */
public class Logger {

    /**
     * second = error
     **/
    private final Pair<List<String>, List<String>> logged;
    private final ReentrantLock locker;
    private final Calendar calendar;

    private Pair<BufferedWriter, BufferedWriter> writers;

    public Logger() {
        this.logged = new Pair<>(new ArrayList<>(), new ArrayList<>());
        this.locker = new ReentrantLock();
        this.calendar = Calendar.getInstance();
        createFiles();
    }

    private void createFiles() {
        try {
            writers = new Pair<>((new BufferedWriter(new FileWriter("logs/" + getDate() + ".txt", true))),new BufferedWriter(new FileWriter("logs/error/" + getDate() + ".txt", true)));
        } catch (IOException e) {
            if (new File("logs").mkdirs() || new File("logs/error").mkdirs())
                createFiles();
            else {
                System.err.println("Unable to create logs files..");
                System.exit(0);
            }
        }
    }

    public void add(String line, boolean error) {
        String finalLine = getTime() + line + "\n";
        if (!error)
            this.logged.getFirst().add(finalLine);
        else
            this.logged.getSecond().add(finalLine);
        check();
    }

    private void check() {
        if (logged.getFirst().size() + logged.getSecond().size() >= 300) {
            printAll();
            reset();
        }
    }

    private void printAll() {
        try {
            this.locker.lock();
            logged.getFirst().forEach(line -> {
                try {
                    writers.getFirst().write(line);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            logged.getSecond().forEach(line -> {
                try {
                    writers.getSecond().write(line);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            writers.getFirst().flush();
            writers.getSecond().flush();
        } catch (IOException e) {
            e.printStackTrace();
            logged.getSecond().add(e.getMessage());
        } finally {
            this.locker.unlock();
        }
    }

    private void reset() {
        this.logged.getFirst().clear();
        this.logged.getSecond().clear();
    }

    private String getDate() {
        return calendar.get(Calendar.DAY_OF_MONTH) + "-" + ((calendar.get(Calendar.MONTH) + 1) < 10 ? "0" + (calendar.get(Calendar.MONTH) + 1) : (calendar.get(Calendar.MONTH) + 1)) + "-" + calendar.get(Calendar.YEAR);
    }

    private String getTime() {
        return calendar.get(Calendar.HOUR_OF_DAY) + ":" + calendar.get(Calendar.MINUTE) + ":" + calendar.get(Calendar.SECOND);
    }

}
