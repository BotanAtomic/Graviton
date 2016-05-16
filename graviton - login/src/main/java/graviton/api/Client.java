package graviton.api;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;

/**
 * Created by Botan on 13/07/2015.
 */
public interface Client {
    long getId();

    void parsePacket(String packet);

    void kick();

    void send(String packet);

    default IoBuffer cryptPacket(String packet) {
        return IoBuffer.allocate(2048).put(packet.getBytes()).flip();
    }
}
