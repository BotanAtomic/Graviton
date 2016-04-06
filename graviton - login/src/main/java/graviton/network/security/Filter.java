package graviton.network.security;


import graviton.database.Database;
import lombok.extern.slf4j.Slf4j;
import org.apache.mina.core.session.IoSession;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by Botan on 03/04/2016.
 */
@Slf4j
public class Filter {

    private final Database database;

    private final int maxConnexion;
    private final int delay;

    private final Map<String, IpInstance> data;

    private final Lock locker = new ReentrantLock(); /** Synchronization **/

    public Filter(int connexion, int delay, Database database) {
        this.maxConnexion = connexion;
        this.delay = delay;
        this.data = new HashMap<>();
        this.database = database;
    }

    public boolean isAttacker(IoSession session) {
        String ip;
        IpInstance ipInstance = search(ip =  session.getRemoteAddress().toString().split(":")[0].substring(1));
        return ipInstance.isAttacker() || ipInstance.isBanned() || database.isBanned(ip);
    }

    public boolean check(IoSession session) {
        locker.lock();
        try {
            String ip = session.getRemoteAddress().toString().split(":")[0].substring(1);
            IpInstance ipInstance = search(ip);
            ipInstance.addConnection();

            if (ipInstance.isAttacker()) {
                session.close(true);
                return false;
            }

            long newTime = System.currentTimeMillis();
            long lastTime = ipInstance.getAndSetLastTime(newTime);
            long difference = newTime - lastTime;

            if (ipInstance.isBanned() || database.isBanned(ip)) {
                if (difference < 200) {
                    if (ipInstance.getConnection() > 10) {
                        log.error("The system has detected an attack from the address {} with {} ms of interval", ip, difference == 0 ? 1 : difference);
                        ipInstance.isAttacker(true);
                    } else if (ipInstance.getConnection() > maxConnexion && !database.isBanned(ip))
                        database.banIp(ip);
                    session.close(true);
                    return false;
                }
                session.write("AlEb");
                session.close(true);
                return false;
            }

            if (difference < delay) {
                if (ipInstance.getConnection() > maxConnexion) {
                    if (ipInstance.addWarning())
                        database.banIp(ip);
                    session.close(true);
                }
                return true;
            } else {
                ipInstance.resetConnection();
                ipInstance.resetWarning();
            }
            return true;
        } finally {
            locker.unlock();
        }
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

        private boolean attacker = false;
        private boolean banned = false;

        public boolean isAttacker() {
            return attacker;
        }

        public void isAttacker(boolean attacker) {
            this.attacker = attacker;
        }

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
            if (warning >= 3)
                return banned = true;
            return false;
        }
    }
}


