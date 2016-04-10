package graviton.game.creature.monster;

import graviton.game.creature.Creature;
import graviton.game.creature.Position;
import graviton.game.enums.IdType;
import graviton.game.fight.fighter.Fightable;
import graviton.game.fight.fighter.Fighter;
import graviton.game.maps.Maps;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Botan on 23/10/2015 [Game]
 */
public class MonsterGroup implements Creature,Fightable {
    final private int id;

    final private Position position;

    @Getter
    final private int agressionDistance;

    private List<MonsterGrade> monsters;

    public MonsterGroup(List<MonsterGrade> monsters, Maps maps) {
        this.monsters = tries(monsters);
        this.position = new Position(maps, maps.getRandomCell(), -1);
        this.id = IdType.MONSTER_GROUP.MAXIMAL_ID - maps.getId() * 1000 - position.getCell().getId();
        this.agressionDistance = generateAgressionDistance();
    }

    private List<MonsterGrade> tries(List<MonsterGrade> monsters) {
        List<MonsterGrade> monsterGrades = new ArrayList<>();
        int maxGroupSize = (int) (Math.random() * 7 + 1);
        for (int i = 0; i < monsters.size(); i++) {
            try {
                if (monsters.size() > 8 && i == maxGroupSize)
                    break;
                monsterGrades.add(monsters.get((int)(Math.random() * monsters.size()-1 + 1)));
            } catch (Exception e) {
                break;
            }
        }

        return monsterGrades;
    }

    private int generateAgressionDistance(){
        int maxLevel = 0;
        for(MonsterGrade mob : monsters)
            if(mob.getLevel() > maxLevel)
                maxLevel = mob.getLevel();
        return maxLevel > 500 ? 3 : maxLevel / 50;
    }

    @Override
    public int getId() {
        return this.id;
    }

    @Override
    public String getGm() {
        StringBuilder id = new StringBuilder();
        StringBuilder gfx = new StringBuilder();
        StringBuilder level = new StringBuilder();
        StringBuilder color = new StringBuilder();
        StringBuilder toReturn = new StringBuilder();

        boolean isFirst = true;

        if (this.monsters.isEmpty()) {
            return "";
        }
        for (MonsterGrade monster : this.monsters) {
            if (!isFirst) {
                id.append(",");
                gfx.append(",");
                level.append(",");
            }
            id.append(monster.getTemplate().getId());
            gfx.append(monster.getTemplate().getGxf()).append("^100");
            level.append(monster.getLevel());
            color.append(monster.getTemplate().getColors()).append(";0,0,0,0;");
            isFirst = false;
        }

        toReturn.append(this.position.getCell().getId()).append(";").append(this.position.getOrientation()).append(";0;").append(this.getId())
                .append(";").append(id).append(";-3;").append(gfx).append(";").append(level).append(";").append(color);
        return toReturn.toString();
    }

    @Override
    public void send(String packet) {

    }

    @Override
    public void setFighter(Fighter fighter) {

    }

    @Override
    public String getFightGm() {
        return "";
    }

    @Override
    public void speak(String message) {

    }

    @Override
    public Position getPosition() {
        return this.position;
    }

    @Override
    public IdType getType() {
        return IdType.MONSTER_GROUP;
    }
}
