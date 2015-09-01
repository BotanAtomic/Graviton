package graviton.network.application;

import graviton.api.NetworkService;
import lombok.extern.slf4j.Slf4j;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.service.IoHandler;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.transport.socket.SocketAcceptor;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;

/**
 * Created by Botan on 18/06/2015.
 */
@Slf4j
public class ApplicationNetwork implements NetworkService,IoHandler {
    private int port;
    private SocketAcceptor acceptor;

    private String user = "test";
    private String password = "pass";
    private IoSession session;

    public ApplicationNetwork() {
        this.port = 500;
        this.acceptor = new NioSocketAcceptor();
        acceptor = new NioSocketAcceptor();
    }

    @Override
    public void start() {
        acceptor.setReuseAddress(true);
        acceptor.setHandler(this);
        if (acceptor.isActive())
            return;
        try {
            acceptor.bind(new InetSocketAddress(port));
        } catch (IOException e) {
            log.warn("Fail to bind Application acceptor : {}", e);
        }
    }

    @Override
    public void stop() {
        acceptor.unbind();
        acceptor.getManagedSessions().values().forEach(session -> session.close(true));
    }

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
                //TODO : Kick Player by Server id
                break;

        }
    }


}
