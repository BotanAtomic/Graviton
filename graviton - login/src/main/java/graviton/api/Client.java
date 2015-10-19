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

    IoSession getSession();

    void send(String packet);

    default IoBuffer cryptPacket(String packet) {
        IoBuffer ioBuffer = IoBuffer.allocate(2048);
        ioBuffer.put(packet.getBytes());
        ioBuffer.flip();
        return ioBuffer;
    }
}
