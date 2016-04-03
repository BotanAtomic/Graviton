package graviton.network.security;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Botan on 03/04/2016.
 */
public class Filtrer {
    private final int maxConnexion;
    private final int delay;

    private final Map<String,IpInstance> data;

    public Filtrer(int connexion,int delay) {
        this.maxConnexion = connexion;
        this.delay = delay;
        this.data = new HashMap<>();
    }

    public boolean check(String ip) {
        System.err.println("Checking IP " + ip );
        if(!data.containsKey(ip)) {
            data.put(ip,new IpInstance(ip));
            return true;
        }
        IpInstance ipInstance = data.get(ip);
        if(ipInstance.banned)
            return false;
        ipInstance.addConnexion();
        if(ipInstance.connexions >= maxConnexion) {
            if(((System.currentTimeMillis() - ipInstance.lastConnexion) / 100) < delay) {
                ipInstance.warning++;
                return false;
            }
        }
        return true;
    }

    public static class IpInstance {
        private final String adress;
        private int connexions = 0;
        private long lastConnexion;
        private int warning = 0;

        private boolean banned = false;

        public IpInstance(String ip) {
            this.adress = ip;
        }

        public void addConnexion() {
            connexions++;
        }

        public void addWarning() {
            warning++;
            if(warning == 3)
                banned = true;
        }
    }
}


