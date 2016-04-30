package graviton.network.security;


/**
 * Created by Botan on 14/04/2016.
 */
public final class IpInstance {
    private int connection;
    private long lastConnection;
    private int warning;

    private boolean attacker = false;
    private boolean banned = false;

    public IpInstance() {
        this.lastConnection = 0;
        this.connection = 0;
        this.warning = 0;
    }

    public boolean isAttacker() {
        return attacker;
    }

    public void isAttacker(boolean attacker) {
        this.attacker = attacker;
    }

    public void resetConnection() {
        connection = 1;
    }

    public int getConnection() {
        return connection;
    }

    public boolean isBanned() {
        return banned;
    }

    public long getAndSetLastTime(long time) {
        long lastTime = lastConnection;
        this.lastConnection = time;
        return lastTime;
    }

    public void addConnection() {
        connection++;
    }

    public void resetWarning() {
        warning = 0;
    }

    public boolean addWarning() {
        warning++;
        return (warning >= 3) ? banned = true : false;
    }
}
