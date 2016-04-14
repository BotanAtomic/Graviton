package graviton.game.creature.npc;

import graviton.game.client.player.Player;
import lombok.Data;

/**
 * Created by Botan on 09/02/2016.
 */

@Data
public class NpcQuestion {
    private int id;
    private String answer;
    private String argument;
    private String condition;
    private int falseQuestion;

    public NpcQuestion(int id, String answer, String argument, String condition, int falseQuestion) {
        this.id = id;
        this.answer = answer;
        this.argument = argument;
        this.condition = condition;
        this.falseQuestion = falseQuestion;
    }

    public String getDQPacket(Player player) {
        /**if(!ConditionParser.validConditions(player, this.getCondition()))
            return World.data.getNpcQuestion(falseQuestion).parseToDQPacket(player);**/

        String packet = this.getId() + "";

        if(!this.getArgument().equals(""))
            packet += ";" + parseArguments(player);

        packet += "|" + this.getAnswer();

        return packet;
    }

    private String parseArguments(Player player) {
        String argument = this.argument;
        argument = argument.replace("[name]", player.getValue("name"));
        argument = argument.replace("[bankCost]", player.getValue("bankCost"));
        return argument;
    }
}
