package graviton.game.common;

/**
 * Created by Botan on 03/10/2015.
 */

import graviton.common.Pair;
import graviton.game.client.player.Player;

import java.util.Date;
import java.util.HashMap;


public class FloodChecker {
    final private Player player;

    private HashMap<Channel, Long> channels;

    private int message = 0;
    private int warning;

    public FloodChecker(Player player) {
        this.player = player;
        this.channels = new HashMap<Channel, Long>() {
            {
                put(Channel.BASIC, 0L);
                put(Channel.TRADE, 0L);
                put(Channel.RECRUITMENT, 0L);
                put(Channel.ALIGNEMENT, 0L);
            }
        };
    }

    private boolean checkChannel(Channel channel) {
        long difference = System.currentTimeMillis() - channels.get(channel);

        if (channel == Channel.BASIC) {
            if (difference < 3000) {
                if (message > 4) {
                    player.send("M10");
                    return false;
                }
                message++;
            } else {
                warning = 0;
                message = 0;
            }
        } else {
            if (difference < 4500) {
                player.send("Im0115;" + (45 - difference / 1000));
                return false;
            }
        }
        channels.put(channel, System.currentTimeMillis());
        return true;
    }

    public void speak(String packet, String canal) {
        Channel channel = Channel.get(canal.charAt(0));

        if (channel == Channel.PRIVATE) {
            Player target = player.getFactory().get(packet.split("\\|")[0]);
            if (target != null) {
                String finalMessage = "|" + packet.split("\\|")[1];
                target.send("cMKF|" + player.getId() + "|" + player.getName() + finalMessage);
                player.send("cMKT|" + target.getId() + "|" + target.getName() + finalMessage);
            } else
                player.send("cMEf" + packet.split("\\|")[0]);
            return;
        }

        if (channel.limited && !checkChannel(channel))
            return;
        if (player.checkAttribut(channel.attribute))
            player.getMap().send(channel.generatePacket(player, packet.substring(1)));
    }

    enum Channel {
        BASIC('*', true, null),
        TRADE('!', true, null),
        ALIGNEMENT(':', true, null),
        RECRUITMENT('?', true, null),
        GROUP('$', false, "group"),
        GUILD('%', false, "guild"),
        RANK('@', false, "rank"),
        PRIVATE('i', false, null);

        private final char channel;
        private final boolean limited;
        private final String attribute;

        Channel(char channel, boolean limited, String attribute) {
            this.channel = channel;
            this.limited = limited;
            this.attribute = attribute;
        }

        public String generatePacket(Player player, String message) {
            return ("cMK" + channel + "|" + player.getId() + "|" + player.getName() + message);
        }

        public static Channel get(char argument) {
            for (Channel channel : Channel.values())
                if (channel.channel == argument)
                    return channel;
            return null;
        }
    }

}
