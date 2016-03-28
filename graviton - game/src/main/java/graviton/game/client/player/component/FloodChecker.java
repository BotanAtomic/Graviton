package graviton.game.client.player.component;

/**
 * Created by Botan on 03/10/2015.
 */

import graviton.common.Pair;
import graviton.game.client.player.Player;
import org.joda.time.Interval;
import org.joda.time.Period;

import java.util.Date;


public class FloodChecker {
    final private Player player;

    private Date alignTime;
    private Date tradeTime;
    private Date incorporationTime;

    private Date basicTime = new Date();

    private int message = 0;
    private int warning;

    public FloodChecker(Player player) {
        this.player = player;
    }

    public boolean canLaunchCommand() {
        return true;
    }

    private boolean autorize(String canal) {
        switch (canal) {
            case "*": /** Canal general **/
                Period period = new Interval(basicTime.getTime(), new Date().getTime()).toPeriod();
                if (period.getSeconds() < 2) {
                    if (message > 2) {
                        if (!addWarning())
                            player.sendText("<b>Anti - Flood :</b> vous devez ralentir votre rythme de message", "FF0000");
                        return false;
                    }
                    message++;
                    return true;
                }
                basicTime = new Date();
                message = 1;
                break;
            case "!": /** Canal alignement **/
                period = new Interval(alignTime.getTime(), new Date().getTime()).toPeriod();
                if (period.getSeconds() < 45) {
                    player.send("Im0115;" + (45 - period.getSeconds()));
                    return false;
                }
                alignTime = new Date();
                break;
            case "?": /** Canal recrutement **/
                if (incorporationTime != null) {
                    period = new Interval(incorporationTime.getTime(), new Date().getTime()).toPeriod();
                    if (period.getSeconds() < 45) {
                        player.send("Im0115;" + (45 - period.getSeconds()));
                        return false;
                    }
                }
                incorporationTime = new Date();
                break;
            case ":": /** Canal commerce **/
                if (tradeTime != null) {
                    period = new Interval(tradeTime.getTime(), new Date().getTime()).toPeriod();
                    if (period.getSeconds() < 45) {
                        player.send("Im0115;" + (45 - period.getSeconds()));
                        return false;
                    }
                }
                tradeTime = new Date();
                break;
        }
        return true;
    }

    public void speak(String packet,String canal) {
        String message = packet.substring(1);
        switch (canal) {
            case "*": /** Canal general **/
                if (autorize(canal))
                    player.getMap().send("cMK|" + player.getId() + "|" + player.getName() + message);
                break;
            case "#": /** Canal equipe **/
                break;
            case "$": /** Canal groupe **/
                if (player.getGroup() != null)
                    player.getGroup().send("cMK$|" + player.getId() + "|" + player.getName() + message);
                break;
            case "%": /** Canal guilde **/
                if (player.getGuild() != null)
                    player.getGuild().send("cMK%|" + player.getId() + "|" + player.getName() + message);
                break;
            case "¤":
                break;
            case "!": /** Canal alignement **/
                break;
            case "?": /** Canal recrutement **/
                if (autorize(canal))
                    player.getFactory().send("cMK?|" + player.getId() + "|" + player.getName() + message);
                break;
            case ":": /** Canal commerce **/
                if (autorize(canal))
                    player.getFactory().send("cMK:|" + player.getId() + "|" + player.getName() + message);
                break;
            case "@": /** Canal admin **/
                if (player.getAccount().getRank().id != 0)
                    player.getGameManager().sendToAdmins("cMK@|" + player.getId() + "|" + player.getName() + message);
                break;
            default: /** Message prive **/
                Player target = player.getFactory().get(packet.split("\\|")[0]);
                if(target != null) {
                    String finalMessage = "|" + packet.split("\\|")[1];
                    target.send("cMKF|" + player.getId() + "|" + player.getName() + finalMessage);
                    player.send("cMKT|" + target.getId() + "|" + target.getName() + finalMessage);
                } else
                    player.send("cMEf" + packet.split("\\|")[0]);
        }
    }

    private boolean addWarning() {
        warning++;
        if (warning == 3) {
            player.sendText("<b>Anti - Flood :</b> c'est votre 3eme avertissement ! Vous etes maintenant muet pour 10 minutes", "FF0000");
            mute();
            warning = 0;
            return true;
        }
        return false;
    }

    private void mute() {
        String message = "<b>[Anti Flood]</b> - Le joueur " + player.getPacketName() + " s'est fait muter <b>10 minutes</b> pour la raison suivante : <b> flood </b>";
        player.getAccount().setMute(new Pair<>(10, new Date()));
        player.getGameManager().send("cs<font color='#000000'>" + message + "</font>");
    }

}
