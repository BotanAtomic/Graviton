package graviton.utils;


import sun.misc.BASE64Decoder;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.security.Key;

/**
 * Created by Botan on 13/09/2015.
 */
public class CryptManager {
    private static final char[] HASH = {'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l',
            'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', 'A',
            'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P',
            'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '0', '1', '2', '3', '4',
            '5', '6', '7', '8', '9', '-', '_'};

    private static final String ALGORITHM = "AES";
    private static final String KEY = "1Hbfh667adfDEJ78";

    public final static String decrypt(String value) {
        try {
            Key key = generateKey();
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, key);
            byte[] decryptedValue64 = new BASE64Decoder().decodeBuffer(value);
            byte[] decryptedByteValue = cipher.doFinal(decryptedValue64);
            String decryptedValue = new String(decryptedByteValue, "utf-8");
            return decryptedValue;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private final static Key generateKey() throws Exception {
        Key key = new SecretKeySpec(KEY.getBytes(), ALGORITHM);
        return key;
    }

    public final static String encrypt(String pass, String key) {
        int i = HASH.length;
        StringBuilder crypted = new StringBuilder("#1");
        for (int y = 0; y < pass.length(); y++) {
            char c1 = pass.charAt(y);
            char c2 = key.charAt(y);
            double d = Math.floor(c1 / 16);
            int j = c1 % 16;
            crypted.append(HASH[(int) ((d + c2 % i) % i)]).append(HASH[(j + c2 % i) % i]);
        }
        return crypted.toString();
    }
}

