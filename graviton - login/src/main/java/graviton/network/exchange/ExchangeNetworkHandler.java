package graviton.network.exchange;

import graviton.console.Console;
import graviton.network.NetworkManager;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.service.IoHandler;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;

import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;

/**
 * Created by Botan on 06/06/2015.
 */
public class ExchangeNetworkHandler implements IoHandler {
    private final NetworkManager manager;
    private Console console;

    public ExchangeNetworkHandler(NetworkManager networkManager, Console console) {
        this.manager = networkManager;
        this.console = console;
    }

    @Override
    public void sessionCreated(IoSession session) throws Exception {
        console.println("[(E)Session " + session.getId() + "] created...", false);
        new ExchangeClient(session, manager);
        IoBuffer ioBuffer = IoBuffer.allocate(2048);
        ioBuffer.put("S?".getBytes());
        ioBuffer.flip();
        session.write(ioBuffer);
    }

    @Override
    public void sessionOpened(IoSession session) throws Exception {
        console.println("[(E)Session " + session.getId() + "] opened...", false);
    }

    @Override
    public void sessionClosed(IoSession session) throws Exception {
        console.println("[(E)Session " + session.getId() + "] closed...", false);
        ExchangeClient client = manager.getExchangeClients().get(session.getId());
        client.getServer().setState(0);
        manager.getExchangeClients().remove(session.getId());
    }

    @Override
    public void sessionIdle(IoSession session, IdleStatus status) throws Exception {
        console.println("[(E)Session " + session.getId() + "] idle...", false);
    }

    @Override
    public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
        console.println(cause.getMessage(), true);
    }

    @Override
    public void messageReceived(IoSession session, Object message) throws Exception {
        String packet = decodePacket(message);
        console.println("[(E)Session " + session.getId() + "] recv < " + packet, false);
        manager.getExchangeClients().get(session.getId()).parsePacket(packet);
    }

    @Override
    public void messageSent(IoSession session, Object message) throws Exception {
        console.println("[(E)Session " + session.getId() + "] send > " + decodePacket(message), false);
    }

    @Override
    public void inputClosed(IoSession session) throws Exception {

    }

    private String decodePacket(Object o) {
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
