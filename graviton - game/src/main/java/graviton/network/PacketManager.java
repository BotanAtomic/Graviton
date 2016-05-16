package graviton.network;

import com.google.inject.Inject;
import graviton.api.InjectSetting;
import graviton.api.PacketParser;
import graviton.common.Utils;
import graviton.factory.type.PlayerFactory;
import graviton.game.GameManager;
import graviton.game.action.player.ActionManager;
import graviton.game.client.player.Player;
import graviton.game.client.player.packet.Packets;
import graviton.game.creature.Creature;
import graviton.game.creature.npc.Npc;
import graviton.game.creature.npc.NpcAnswer;
import graviton.game.enums.IdType;
import graviton.game.enums.Rank;
import graviton.game.exchange.job.BreakerExchange;
import graviton.game.maps.Maps;
import graviton.game.object.ObjectTemplate;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.shorts.Short2ObjectOpenHashMap;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.*;


/**
 * Created by Botan on 20/06/2015.
 */
@Slf4j
public class PacketManager {
    private final String[] dictionary;
    private final String[] forbidden;
    private final Calendar calendar;

    @InjectSetting("packet.crypted")
    private boolean crypted;
    @Inject
    GameManager gameManager;
    @Inject
    PlayerFactory playerFactory;
    @Getter
    private Map<Short, PacketParser> packets;

    public PacketManager(String[] dictionary, String[] forbidden) {
        this.calendar = GregorianCalendar.getInstance();
        this.dictionary = dictionary;
        this.forbidden = forbidden;
        load();
    }

    private void addPacket(String packet, PacketParser packetParser) {
        this.packets.put((short)((packet.charAt(0) - packet.charAt(1))*(packet.charAt(1) + packet.charAt(0))), packetParser);
    }

    public void load() {
        this.packets = new Short2ObjectOpenHashMap<>(16, 1);

        addPacket("cC", (client, packet) -> client.send("cC" + packet));

        addPacket("BA", (client, packet) -> client.getAccount().launchCommand(packet));

        addPacket("EK", (client, packet) -> client.getCurrentPlayer().getExchange().toogleOk(client.getCurrentPlayer().getId()));

        addPacket("EM", (client, packet) -> client.getCurrentPlayer().doExchangeAction(packet));

        addPacket("EL", (client, packet) -> ((BreakerExchange)client.getCurrentPlayer().getExchange()).setLastIngredients());

        addPacket("EA", (client, packet) -> client.getCurrentPlayer().startExchange());

        addPacket("EB", (client, packet) -> {
            String[] arguments = packet.split("\\|");

            ObjectTemplate template = gameManager.getObjectTemplate(Integer.parseInt(arguments[0]));
            int quantity = Integer.parseInt(arguments[1]);

            client.getCurrentPlayer().addKamas(-template.getPrice() * quantity);
            client.getCurrentPlayer().addObject(template.createObject(quantity, false), true);

            client.send(client.getCurrentPlayer().getPacket(Packets.As));
            client.send("EBK");
        });

        addPacket("EV", (client, packet) -> {
            if (client.getCurrentPlayer().getExchange() == null) {
                client.send("EV");
                client.getCurrentPlayer().save();
            } else
                client.getCurrentPlayer().getExchange().cancel();
        });

        addPacket("ER", (client, packet) -> client.getCurrentPlayer().askExchange(packet));

        addPacket("Od", (client, packet) -> client.getCurrentPlayer().removeObject(packet));

        addPacket("OM", (client, packet) -> client.getCurrentPlayer().moveObject(packet));

        addPacket("DV", (client, packet) -> {
            client.send("DV");
            client.getCurrentPlayer().setAskedCreature(0);
            client.getCurrentPlayer().getActionManager().setStatus(ActionManager.Status.WAITING);
        });

        addPacket("DC", (client, packet) -> {
            Creature creature = client.getCurrentPlayer().getMap().getCreatures(IdType.NPC).get(Integer.parseInt(packet));
            if (creature != null)
                client.getCurrentPlayer().createDialog((Npc) creature);
            else
                log.error("The NPC can not be found", packet);
        });

        addPacket("DR", (client, packet) -> {
            try {
                String[] informations = packet.split("\\|");

                assert (client.getCurrentPlayer().getActionManager().getStatus() != ActionManager.Status.DIALOG);

                Npc npc = (Npc) client.getCurrentPlayer().getMap().getCreatures(IdType.NPC).get(client.getCurrentPlayer().getAskedCreature());

                assert npc != null;

                NpcAnswer answer = gameManager.getNpcAnswer(Integer.parseInt(informations[1]));

                if (answer == null) {
                    client.send("DV");
                    client.getCurrentPlayer().setAskedCreature(0);
                    client.getCurrentPlayer().setActionState(ActionManager.Status.WAITING);
                    return;
                }

                answer.apply(client.getCurrentPlayer());
            } catch (Exception e) {
                e.printStackTrace();
                client.send("DV");
                client.getCurrentPlayer().setAskedCreature(0);
                client.getCurrentPlayer().setActionState(ActionManager.Status.WAITING);
            }
        });

        addPacket("PV", (client, packet) -> {
            if (packet.isEmpty())
                client.getCurrentPlayer().getGroup().removeMember(client.getCurrentPlayer());
            else
                client.getCurrentPlayer().getGroup().kick(client.getCurrentPlayer(), gameManager.getPlayer(Integer.parseInt(packet)));
        });

        addPacket("FD", (client, packet) -> client.getAccount().removeInList(packet, true));

        addPacket("iD", (client, packet) -> client.getAccount().removeInList(packet, false));

        addPacket("FL", (client, packet) -> client.send(client.getAccount().getListPacket(true)));

        addPacket("iL", (client, packet) -> client.send(client.getAccount().getListPacket(false)));

        addPacket("FA", (client, packet) -> client.getAccount().addInList(client.getCurrentPlayer().getGameManager().getPlayer(packet), true));

        addPacket("iA", (client, packet) -> client.getAccount().addInList(client.getCurrentPlayer().getGameManager().getPlayer(packet), false));

        addPacket("Ba", (client, packet) -> {
            if (client.getAccount().getRank().id == Rank.PLAYER.id)
                return;
            String[] arguments = packet.substring(1).trim().split(",");
            Maps map = gameManager.getMapByPosition(Integer.parseInt(arguments[0]), Integer.parseInt(arguments[1]));
            if (map != null)
                client.getCurrentPlayer().changePosition(map.getRandomCell());
        });

        addPacket("GP", (client, packet) -> client.getCurrentPlayer().togglePvp(packet.charAt(0)));

        addPacket("WU", (client, packet) -> client.getCurrentPlayer().useZaap(Integer.parseInt(packet)));

        addPacket("AB", (client, packet) -> client.getCurrentPlayer().boostStatistics(Integer.parseInt(packet)));

        addPacket("GC", (client, packet) -> client.getCurrentPlayer().createGame());

        addPacket("SB", (client, packet) -> {
            if (!client.getCurrentPlayer().boostSpell(Integer.parseInt(packet)))
                client.send("SUE");
        });

        addPacket("SM", (client, packet) -> {
            String[] arguments = packet.split("\\|");
            Player player = client.getCurrentPlayer();
            int id = Integer.parseInt(arguments[0]);
            if (player.getSpells().containsKey(id))
                player.moveSpell(id, Utils.HASH[Integer.parseInt(arguments[1])]);
            client.send("BN");
        });

        addPacket("PI", (client, packet) -> {
            Player target = client.getCurrentPlayer().getGameManager().getPlayer(packet);

            if (target == null) {
                client.send("PIEn" + packet);
                return;
            }

            if (target.getGroup() != null) {
                client.send("PIEa" + packet);
                return;
            }
            String finalPacket = "PIK" + client.getCurrentPlayer().getName() + "|" + target.getName();
            client.getCurrentPlayer().setAskedCreature(target.getId());
            target.setAskedCreature(client.getCurrentPlayer().getId());
            client.send(finalPacket);
            target.send(finalPacket);
        });

        addPacket("GA", (client, packet) -> client.getCurrentPlayer().createAction(Integer.parseInt(packet.substring(0, 3)), packet.substring(3)));

        addPacket("GK", (client, packet) -> {
            if (client.getCurrentPlayer().getActionManager() == null || client.getCurrentPlayer().getActionManager().getCurrentActions().isEmpty())
                return;
            int gameActionId;
            String[] informations = packet.substring(1).split("\\|");
            try {
                gameActionId = Integer.parseInt(informations[0]);
                client.getCurrentPlayer().getActionManager().endAction(gameActionId, packet.charAt(0) == 'K', informations.length > 1 ? informations[1] : "");
            } catch (Exception e) {
                log.error("Problem at packet {} : {}", packet, e);
            }
        });

        addPacket("gI", (client, packet) -> client.getCurrentPlayer().sendGuildInformations(packet.charAt(0)));

        addPacket("gC", (client, packet) -> client.getCurrentPlayer().createGuild(packet));

        addPacket("eD", (client, packet) -> client.getCurrentPlayer().changeOrientation(Integer.parseInt(packet), true));

        addPacket("eU", (client, packet) -> client.getCurrentPlayer().getMap().send("eUK" + client.getCurrentPlayer().getId() + "|" + packet));

        addPacket("BS", (client, packet) -> client.getCurrentPlayer().getMap().send("cS" + client.getCurrentPlayer().getId() + "|" + packet));

        addPacket("BM", (client, packet) -> client.getCurrentPlayer().speak(packet.substring(0, packet.length() - 1), packet.substring(0, 1)));

        addPacket("AT", (client, packet) -> {
            client.setAccount(gameManager.getAccount(Integer.parseInt(packet)));
            if (client.getAccount() != null) {
                client.getAccount().setClient(client);
                client.getAccount().setNetworkAddress(client.getSession().getLocalAddress().toString().replace("/", "").split(":")[0]);
                client.send("ATK" + (crypted ? client.generateKey() : "0"));
            } else {
                client.send("ATE");
            }
        });

        addPacket("AS", (client, packet) -> {
            try {
                client.getAccount().getPlayer(Integer.parseInt(packet)).joinGame();
            } catch (NullPointerException e) {
                client.send("ASE");
            }
        });

        addPacket("AD", (client, packet) -> {
            if (client.getAccount().getAnswer().equals(packet.substring(2))) {
                client.getAccount().getPlayer(Integer.parseInt(packet.substring(0, 1))).delete();
                return;
            }
            client.send("ADE");
        });

        addPacket("AA", (client, packet) -> {
            if (client.getAccount().getPlayers().size() >= 5) {
                client.send("AAEf");
                return;
            }
            String[] arguments = packet.split("\\|");
            if (playerFactory.checkName(arguments[0]) || arguments[0].length() < 4 || arguments[0].length() > 12) {
                client.send("AAEa");
                return;
            }
            for (String forbiddenWord : forbidden)
                if (arguments[0].toLowerCase().contains(forbiddenWord)) {
                    client.send("AAEa");
                    return;
                }
            int[] colors = {Integer.parseInt(arguments[3]), Integer.parseInt(arguments[4]), Integer.parseInt(arguments[5])};
            client.getAccount().createPlayer(arguments[0], (byte) Integer.parseInt(arguments[1]), (byte) Integer.parseInt(arguments[2]), colors);
        });

        /** Without argument **/
        addPacket("Ai", (client, packet) -> {
            client.send(client.getAccount().getPlayersCachePacket());
        });

        addPacket("AL", (client, packet) -> {
            client.send(client.getAccount().getPlayersPacket());
        });

        addPacket("AV", (client, packet) -> client.send("AV0"));

        addPacket("Af", (client, packet) -> client.send("Af1|1|1|1|1"));

        addPacket("AP", (client, packet) -> {
            String pseudo = dictionary[(int) (Math.random() * dictionary.length - 1)] + dictionary[(int) (Math.random() * dictionary.length - 1)];
            while (pseudo.length() < 5)
                pseudo = dictionary[(int) (Math.random() * dictionary.length - 1)] + dictionary[(int) (Math.random() * dictionary.length - 1)];
            client.send("AP" + pseudo);
        });

        addPacket("BD", (client, packet) -> {
            client.send("BD" + calendar.get(Calendar.YEAR) + "|" + calendar.get(Calendar.MONTH) + "|" + calendar.get(Calendar.DAY_OF_MONTH));
            client.send("BT" + (new Date().getTime() + 3600000));
        });

        addPacket("GI", (client, packet) -> {
            client.send(client.getCurrentPlayer().getMap().getGMs());
            client.getCurrentPlayer().getMap().sendGdf(client.getCurrentPlayer());
            client.send("GDK");
        });

        addPacket("WV", (client, packet) -> client.send("WV"));

        addPacket("PR", (client, packet) -> {
            if (client.getCurrentPlayer().getAskedCreature() == 0)
                return;
            client.send("BN");
            Player player = playerFactory.get(client.getCurrentPlayer().getAskedCreature());
            assert player != null;
            player.send("PR");
            player.setAskedCreature(0);
            client.getCurrentPlayer().setAskedCreature(0);
        });

        addPacket("PA", (client, packet) -> {
            if (client.getCurrentPlayer().getAskedCreature() == 0)
                return;
            client.send("BN");
            Player player2 = playerFactory.get(client.getCurrentPlayer().getAskedCreature());
            assert player2 != null;
            if (player2.getGroup() != null)
                player2.getGroup().addMember(client.getCurrentPlayer());
            else
                player2.createGroup(client.getCurrentPlayer());
            player2.send("PR");
            client.getCurrentPlayer().setAskedCreature(0);
            player2.setAskedCreature(0);
        });

        this.packets = Collections.unmodifiableMap(packets);
        log.info("{} packets loaded", this.packets.size());
    }

}
