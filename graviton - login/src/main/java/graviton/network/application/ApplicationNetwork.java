package graviton.network.application;

import lombok.extern.slf4j.Slf4j;

/**
 * Created by Botan on 18/06/2015.
 */
@Slf4j
public class ApplicationNetwork {
}  /**
 * TODO :implements NetworkService,IoHandler
 * <p>
 * private int port;
 * private SocketAcceptor acceptor;
 * <p>
 * private String user = "test";
 * private String password = "pass";
 * private IoSession session;
 * <p>
 * public ApplicationNetwork() {
 * this.port = 5000;
 * this.acceptor = new NioSocketAcceptor();
 * }
 *
 * @Override public void start() {
 * acceptor.setReuseAddress(true);
 * acceptor.setHandler(this);
 * if (acceptor.isActive())
 * return;
 * try {
 * acceptor.bind(new InetSocketAddress(port));
 * } catch (IOException e) {
 * log.error("Fail to bind Application acceptor : {}", e);
 * }
 * }
 * @Override public void stop() {
 * acceptor.unbind();
 * acceptor.getManagedSessions().values().forEach(session -> session.close(true));
 * }
 * @Override public void sessionCreated(IoSession session) throws Exception {
 * this.session = session;
 * System.out.println("[Application " + session.getId() + "] is connected");
 * }
 * @Override public void sessionOpened(IoSession session) throws Exception {
 * <p>
 * }
 * @Override public void sessionClosed(IoSession session) throws Exception {
 * System.out.println("[Application " + session.getId() + "] is deconnected");
 * }
 * @Override public void sessionIdle(IoSession session, IdleStatus status) throws Exception {
 * System.out.println("[Application " + session.getId() + "] is idle");
 * }
 * @Override public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
 * <p>
 * }
 * @Override public void messageReceived(IoSession session, Object message) throws Exception {
 * String packet = decodePacket(message);
 * System.out.println("[Application " + session.getId() + "] recv < " + packet);
 * this.parse(packet);
 * }
 * @Override public void messageSent(IoSession session, Object message) throws Exception {
 * System.out.println("[Application " + session.getId() + "] send > " + decodePacket(message));
 * }
 * @Override public void inputClosed(IoSession session) throws Exception {
 * System.out.println("[Application " + session.getId() + "] is closed");
 * }
 * <p>
 * private void send(String packet) {
 * IoBuffer ioBuffer = IoBuffer.allocate(2048);
 * ioBuffer.put(packet.getBytes());
 * ioBuffer.flip();
 * session.write(ioBuffer);
 * }
 * <p>
 * private String decodePacket(Object packet) {
 * IoBuffer buffer = IoBuffer.allocate(2048);
 * buffer.put((IoBuffer) packet);
 * buffer.flip();
 * CharsetDecoder cd = Charset.forName("UTF-8").newDecoder();
 * try {
 * return buffer.getString(cd);
 * } catch (CharacterCodingException e) {
 * <p>
 * }
 * return "undefined";
 * }
 * <p>
 * private void parse(String packet) {
 * String finalPacket = packet.substring(1);
 * switch (packet.charAt(0)) {
 * case 'C': /** Connection [C+USER;PASS]
 * String user = null;
 * String pass = null;
 * try {
 * user = finalPacket.split(";")[0];
 * pass = finalPacket.split(";")[1];
 * } catch (Exception e) {
 * <p>
 * }
 * if (!this.user.equals(user) || !this.password.equals(pass)) {
 * send("BAD");
 * return;
 * }
 * send("GOOD");
 * break;
 * case 'S': /** Stop
 * System.exit(0);
 * break;
 * case 'R': /** Restart
 * System.exit(1);
 * break;
 * case 'V': /** Save
 * //TODO : Save
 * send("SAVE1");
 * break;
 * case 'K':/** Kick
 * //TODO : Kick Player by Server id
 * break;
 * <p>
 * }
 * }
 * <p>
 * <p>
 * }
 **/