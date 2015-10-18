package graviton.api;

import org.apache.mina.core.buffer.IoBuffer;

import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;

/**
 * Created by Botan on 06/07/2015.
 */
public interface NetworkService {
    void start();

    void stop();

    default String decryptPacket(Object o) {
        IoBuffer buffer = IoBuffer.allocate(2048);
        buffer.put((IoBuffer) o);
        buffer.flip();
        CharsetDecoder cd = Charset.forName("UTF-8").newDecoder();
        try {
            return buffer.getString(cd);
        } catch (CharacterCodingException e) {
            return "undefined";
        }
    }
}
