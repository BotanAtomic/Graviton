package graviton.game.common;

import com.google.inject.Injector;
import graviton.game.client.player.Player;
import graviton.game.maps.Maps;
import lombok.Data;

/**
 * Created by Botan on 22/06/2015.
 */
@Data
public class Action {
    private int id;
    private String arguments;

    public Action(int id, String arguments) {
        this.id = id;
        this.arguments = arguments;
    }

    public void apply(Player player) {
        String[] split = arguments.split(",");
        switch (this.id) {
            case 0:
                Maps map = player.getGameManager().getMap(Integer.parseInt(split[0]));
                if (map != null)
                    player.changePosition(map.getCell(Integer.parseInt(split[1])));
                break;
        }
    }
}
