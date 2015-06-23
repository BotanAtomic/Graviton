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

    private final ReentrantLock locker;
    private Calendar calendar;
    private List<String> logged;
    private List<String> errorLogged;
    private BufferedWriter writer;
    private BufferedWriter errorWriter;

    public Logger() {
        this.logged = new ArrayList<>();
        this.errorLogged = new ArrayList<>();
        this.locker = new ReentrantLock();
        this.configure();
    }

    public void configure() {
        Date date = new Date();
        this.calendar = GregorianCalendar.getInstance();
        calendar.setTime(date);
        createFiles();
    }

    private void createFiles() {
        try {
            writer = new BufferedWriter(new FileWriter("logs/" + getDate() + ".txt", true));
            errorWriter = new BufferedWriter(new FileWriter("logs/error/" + getDate() + ".txt", true));
        } catch (IOException e) {
            new File("logs").mkdirs();
            new File("logs/error").mkdirs();
            createFiles();
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
        if (logged.size() + errorLogged.size() >= 400) {
            printAll();
            reset();
        }
    }

    public void printAll() {
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

    public String getDate() {
        return calendar.get(Calendar.DAY_OF_MONTH) + "-" + ((calendar.get(Calendar.MONTH) + 1) < 10 ? "0" + (calendar.get(Calendar.MONTH) + 1) : (calendar.get(Calendar.MONTH) + 1)) + "-" + calendar.get(Calendar.YEAR);
    }

    public String getTime() {
        return calendar.get(Calendar.HOUR_OF_DAY) + ":" + calendar.get(Calendar.MINUTE) + ":" + calendar.get(Calendar.SECOND) + " ";
    }

}
