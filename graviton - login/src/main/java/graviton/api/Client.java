package graviton.api;

/**
 * Created by Botan on 13/07/2015.
 */
public interface Client {
    long getId();
    void parsePacket(String packet) throws Exception;
}
