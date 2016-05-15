package graviton.common;


/**
 * Created by Botan on 26/09/2015.
 */

/**
 * Fucking static class
 **/
public class Utils {
    public static final char[] HASH = {'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's',
            't', 'u', 'v', 'w', 'x', 'y', 'z', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U',
            'V', 'W', 'X', 'Y', 'Z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '-', '_'};

    public static char getHashedValueByInteger(int c) {
        return HASH[c];
    }

    public static String generateKey() {
        String key = "";
        for(int i = 0; i <32;i++)
            key = key.concat(String.valueOf(HASH[(int)Math.random()*HASH.length]));
        return key;
    }


    public static int getIntByHashedValue(char c) {
        for (int a = 0; a <= HASH.length; a++)
            if (HASH[a] == c)
                return a;
        return -1;
    }

 }
