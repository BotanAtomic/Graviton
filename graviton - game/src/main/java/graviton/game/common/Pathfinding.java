package graviton.game.common;

import graviton.common.Utils;
import graviton.game.maps.Maps;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by Botan on 09/10/2015.
 */
public abstract class Pathfinding {

    private final char[] HASH = Utils.HASH;

    protected int isValidPathfinding(Maps map, int cell, AtomicReference<String> pathRef) {
        int newPos = cell;
        int steps = 0;
        String path = pathRef.get();
        String newPath = "";

        for (int i = 0; i < path.length(); i += 3) {
            String smallPath = path.substring(i, i + 3);
            char dir = smallPath.charAt(0);
            String[] aPathInfos = validSinglePath(newPos, smallPath, map);
            newPos = Integer.parseInt(aPathInfos[1]);
            steps += Integer.parseInt(aPathInfos[2]);
            newPath += dir + cellToCode(newPos);
            if (aPathInfos[0].equalsIgnoreCase("stop"))
                break;
        }

        pathRef.set(newPath);
        return steps;
    }

    private String[] validSinglePath(int CurrentPos, String Path, Maps map) {
        char dir = Path.charAt(0);
        int newStep;
        String[] result = new String[3];
        int dirCaseID = codeToCell(Path.substring(1));
        int lastPos = CurrentPos;
        for (newStep = 0; newStep <= 64; newStep++) {
            result[1] = Integer.toString(lastPos);
            if (lastPos == dirCaseID) {
                result[0] = "ok";
                break;
            }
            lastPos = getCellFromDirection(lastPos, dir, map);
            if (map.getCell(lastPos) == null || !map.getCell(lastPos).isWalkable()) {
                result[0] = "stop";
                break;
            }
        }
        result[2] = Integer.toString(newStep);
        return result;
    }

    private int getCellFromDirection(int CaseID, char direction, Maps map) {
        switch (direction) {
            case 'a':
                return CaseID + 1;
            case 'b':
                return CaseID + map.getWidth();
            case 'c':
                return CaseID + (map.getWidth() * 2 - 1);
            case 'd':
                return CaseID + (map.getWidth() - 1);
            case 'e':
                return CaseID - 1;
            case 'f':
                return CaseID - map.getWidth();
            case 'g':
                return CaseID - (map.getWidth() * 2 - 1);
            case 'h':
                return CaseID - map.getWidth() + 1;
        }
        return -1;
    }


    protected String cellToCode(int cellID) {
        int char1 = cellID / 64, char2 = cellID % 64;
        return HASH[char1] + "" + HASH[char2];
    }

    private int codeToCell(String cellCode) {
        char char1 = cellCode.charAt(0), char2 = cellCode.charAt(1);
        int code1 = 0, code2 = 0;
        for (int a = 0; a < HASH.length; a++) {
            if (HASH[a] == char1) {
                code1 = a * 64;
            }
            if (HASH[a] == char2) {
                code2 = a;
            }
        }
        return (code1 + code2);
    }

    protected int getFinalOrientation(String path) {
        return Utils.getIntByHashedValue(path.charAt(path.length() - 3));
    }

    protected int getFinalCell(String path) {
        return codeToCell(path.substring(path.length() - 2));
    }

}
