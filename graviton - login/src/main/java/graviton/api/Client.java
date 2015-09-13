package graviton.api;

import graviton.login.Login;
import graviton.login.Main;
import org.apache.mina.core.session.IoSession;

/**
 * Created by Botan on 13/07/2015.
 */
public interface Client {
    Login login = Main.getInstance(Login.class);

    long getId();

    void parsePacket(String packet) throws Exception;

    void kick();

    IoSession getSession();

    void send(String packet);
}
