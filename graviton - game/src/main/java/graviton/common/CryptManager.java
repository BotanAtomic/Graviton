package graviton.common;


import sun.misc.BASE64Decoder;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.security.Key;

/**
 * Created by Botan on 13/09/2015.
 */
public class CryptManager {

    private static final String ALGORITHM = "AES";
    private static final String KEY = "1Hbfh667adfDEJ78";
    private static final Key key = new SecretKeySpec(KEY.getBytes(), ALGORITHM);

    public static String decrypt(String value) {
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, key);
            byte[] decryptedValue64 = new BASE64Decoder().decodeBuffer(value);
            byte[] decryptedByteValue = cipher.doFinal(decryptedValue64);
            return new String(decryptedByteValue, "utf-8");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}

