package graviton.game.client.player.component;

/**
 * Created by Botan on 03/10/2015.
 */

import graviton.common.Pair;
import graviton.game.client.player.Player;

import java.util.Date;


public class FloodCheck {
    final private Player player;
    private final long FLOOD_TIME = 60000;
    private long alignTime;
    private long tradeTime;
    private long incorporationTime;
    private long basicTime;
    private int message = 0;
    private int warning;

    public FloodCheck(Player player) {
        this.player = player;
    }

    public boolean canLaunchCommand() {
        return true;
    }

    public boolean autorize(String canal) {
        int difference;
        switch (canal) {
            case "*": /** Canal general **/
                if (getDifference(basicTime, System.currentTimeMillis()) < 1500) {
                    if (message > 2) {
                        if (!addWarning())
                            player.sendText("<b>Anti - Flood :</b> vous devez ralentir votre rythme de message", "FF0000");
                        return false;
                    }
                    message++;
                    return true;
                }
                basicTime = System.currentTimeMillis();
                message = 1;
                break;
            case "!": /** Canal alignement **/
                difference = getDifference(alignTime, System.currentTimeMillis());
                if (difference < FLOOD_TIME) {
                    player.send("Im0115;" + getRemainingTime(difference));
                    return false;
                }
                alignTime = System.currentTimeMillis();
                break;
            case "?": /** Canal recrutement **/
                difference = getDifference(incorporationTime, System.currentTimeMillis());
                if (difference < FLOOD_TIME) {
                    player.send("Im0115;" + getRemainingTime(difference));
                    return false;
                }
                incorporationTime = System.currentTimeMillis();
                break;
            case ":": /** Canal commerce **/
                difference = getDifference(tradeTime, System.currentTimeMillis());
                if (difference < FLOOD_TIME) {
                    player.send("Im0115;" + getRemainingTime(difference));
                    return false;
                }
                tradeTime = System.currentTimeMillis();
                break;
        }
        return true;
    }

    private boolean addWarning() {
        warning++;
        if (warning == 2) {
            player.sendText("<b>Anti - Flood :</b> c'est votre 2eme avertissement ! Vous etes maintenant muet pour 10 minutes", "FF0000");
            mute();
            warning = 0;
            return true;
        }
        return false;
    }

    private void mute() {
        String message = "[AntiFlood] - Le joueur <b>" + player.getName() + "</b> s'est fait mute <b>10 minutes</b> pour la raison suivante : <b> flood </b>";
        player.getAccount().setMute(new Pair<>(10, new Date()));
        player.getGameManager().sendToPlayers("cs<font color='#000000'>" + message + "</font>");
        return;
    }

    private int getDifference(long lastTime, long actualTime) {
        return (int) ((actualTime - lastTime));
    }

    private int getRemainingTime(long difference) {
        return (int) (FLOOD_TIME - difference) / 1000;
    }
}
