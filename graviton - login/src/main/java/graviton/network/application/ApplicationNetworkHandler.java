package graviton.network.application;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.service.IoHandler;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;

import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;

/**
 * Created by Botan on 18/06/2015.
 */
public class ApplicationNetworkHandler implements IoHandler {
    private boolean autorized = false;
    private String user = "test";
    private String password = "pass";
    private IoSession session;

    @Override
    public void sessionCreated(IoSession session) throws Exception {
        this.session = session;
        System.out.println("[Application " + session.getId() + "] is connected..");
    }

    @Override
    public void sessionOpened(IoSession session) throws Exception {

    }

    @Override
    public void sessionClosed(IoSession session) throws Exception {
        System.out.println("[Application " + session.getId() + "] is deconnected..");
    }

    @Override
    public void sessionIdle(IoSession session, IdleStatus status) throws Exception {
        System.out.println("[Application " + session.getId() + "] is idle..");
    }

    @Override
    public void exceptionCaught(IoSession session, Throwable cause) throws Exception {

    }

    @Override
    public void messageReceived(IoSession session, Object message) throws Exception {
        String packet = decodePacket(message);
        System.out.println("[Application " + session.getId() + "] recv < " + packet);
        this.parse(packet);
    }

    @Override
    public void messageSent(IoSession session, Object message) throws Exception {
        System.out.println("[Application " + session.getId() + "] send > " + decodePacket(message));
    }

    @Override
    public void inputClosed(IoSession session) throws Exception {
        System.out.println("[Application " + session.getId() + "] is closed..");
    }

    private void send(String packet) {
        IoBuffer ioBuffer = IoBuffer.allocate(2048);
        ioBuffer.put(packet.getBytes());
        ioBuffer.flip();
        session.write(ioBuffer);
    }

    private String decodePacket(Object packet) {
        IoBuffer buffer = IoBuffer.allocate(2048);
        buffer.put((IoBuffer) packet);
        buffer.flip();

        CharsetDecoder cd = Charset.forName("UTF-8").newDecoder();

        try {
            return buffer.getString(cd);
        } catch (CharacterCodingException e) {

        }
        return "undefined";
    }

    private void parse(String packet) {
        String finalPacket = packet.substring(1);
        if (!autorized && packet.charAt(0) != 'C') {
            System.out.println("The application does not have the rights necessary to perform an action..");
            return;
        }
        switch (packet.charAt(0)) {
            case 'C': /** Connection [C+USER;PASS]**/
                String user = null;
                String pass = null;
                try {
                    user = finalPacket.split(";")[0];
                    pass = finalPacket.split(";")[1];
                } catch (Exception e) {

                }
                if (!this.user.equals(user) || !this.password.equals(pass)) {
                    send("BAD");
                    return;
                }
                send("GOOD");
                this.autorized = true;
                break;
            case 'S': /** Stop **/
                System.exit(0);
                break;
            case 'R': /** Restart **/
                System.exit(1);
                break;
            case 'V': /** Save **/
                //TODO : Save
                send("SAVE1");
                break;
            case 'K':/** Kick **/
                int serverId = 0;
                String playerName = null;
                try {
                    serverId = Integer.parseInt(finalPacket.split(";")[0]);
                    playerName = finalPacket.split(";")[1];
                } catch (Exception e) {

                }
                //TODO : Kick Player by Server id
                break;

        }
    }
}
