package graviton.network.security;


import graviton.database.Database;
import org.apache.mina.core.session.IoSession;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Botan on 03/04/2016.
 */
public class Filter {

    private final Database database;

    private final int maxConnexion;
    private final int delay;

    private final Map<String, IpInstance> data;

    public Filter(int connexion, int delay, Database database) {
        this.maxConnexion = connexion;
        this.delay = delay;
        this.data = new HashMap<>();
        this.database = database;
    }

    public boolean check(IoSession session) {
        String ip = session.getRemoteAddress().toString().split(":")[0].substring(1);
        IpInstance ipInstance = search(ip);

        if (ipInstance.isBanned() || database.isBanned(ip)) {
            session.write("AlEb");
            session.close(true);
            return false;
        }
        ipInstance.addConnection();

        long newTime = System.currentTimeMillis();

        if ((newTime - ipInstance.getLastConnection()) < delay) {
            if (ipInstance.getConnection() > maxConnexion) {
                if (ipInstance.addWarning())
                    database.banIp(ip);
                session.close(true);
            }
            return false;
        } else {
            ipInstance.resetConnection();
            ipInstance.resetWarning();
        }

        ipInstance.setLastConnexion(newTime);
        return true;
    }

    private IpInstance search(String ip) {
        IpInstance ipInstance;
        if (!data.containsKey(ip))
            data.put(ip, ipInstance = new IpInstance());
        else
            ipInstance = data.get(ip);
        return ipInstance;
    }

    public final static class IpInstance {
        private int connection = 0;
        private long lastConnection;
        private int warning = 0;

        private boolean banned = false;

        public IpInstance() {
            lastConnection = new Date().getTime();
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

        public long getLastConnection() {
            return lastConnection;
        }

        public void setLastConnexion(long time) {
            lastConnection = time;
        }

        public void addConnection() {
            connection++;
        }

        public void resetWarning() {
            warning = 0;
        }

        public boolean addWarning() {
            warning++;
            if (warning >= 3)
                return banned = true;
            return false;
        }
    }
}


