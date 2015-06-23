package graviton.console;

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

    private final List<String> logged;
    private final List<String> errorLogged;
    private final ReentrantLock locker;
    private Calendar calendar;
    private BufferedWriter writer;
    private BufferedWriter errorWriter;

    public Logger() {
        this.logged = new ArrayList<>();
        this.errorLogged = new ArrayList<>();
        this.locker = new ReentrantLock();
        this.calendar = GregorianCalendar.getInstance();
        calendar.setTime(new Date());
        createFiles();
    }

    private void createFiles() {
        try {
            writer = new BufferedWriter(new FileWriter("logs/" + getDate() + ".txt", true));
            errorWriter = new BufferedWriter(new FileWriter("logs/error/" + getDate() + ".txt", true));
        } catch (IOException e) {
            if (new File("logs").mkdirs() || new File("logs/error").mkdirs())
                createFiles();
            else {
                System.err.println("Impossible te create logs files..");
                System.exit(0);
            }
        }
    }

    public void add(String line, boolean error) {
        String finalLine = getTime() + line + "\n";
        if (!error)
            this.logged.add(finalLine);
        else
            this.errorLogged.add(finalLine);
        check();
    }

    private void check() {
        if (logged.size() + errorLogged.size() >= 300) {
            printAll();
            reset();
        }
    }

    private void printAll() {
        try {
            this.locker.lock();
            for (String line : this.logged)
                this.writer.write(line);
            writer.flush();
            for (String line : this.errorLogged)
                this.errorWriter.write(line);
            errorWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
            this.errorLogged.add(e.getMessage());
        } finally {
            this.locker.unlock();
        }
    }

    private void reset() {
        this.logged.clear();
        this.errorLogged.clear();
    }

    private String getDate() {
        return calendar.get(Calendar.DAY_OF_MONTH) + "-" + ((calendar.get(Calendar.MONTH) + 1) < 10 ? "0" + (calendar.get(Calendar.MONTH) + 1) : (calendar.get(Calendar.MONTH) + 1)) + "-" + calendar.get(Calendar.YEAR);
    }

    private String getTime() {
        return calendar.get(Calendar.HOUR_OF_DAY) + ":" + calendar.get(Calendar.MINUTE) + ":" + calendar.get(Calendar.SECOND);
    }

}
