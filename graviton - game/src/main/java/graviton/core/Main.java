package graviton.core;

import com.google.inject.Guice;
import graviton.core.injector.DefaultModule;
import sun.misc.BASE64Decoder;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;


/**
 * Created by Botan on 16/06/2015.
 */
public class Main {

    public static void main(String[] args) {
        Guice.createInjector(new DefaultModule()).getInstance(Manager.class).start();
    }

    private static void decrypt(String value) {
        try {
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(2, new SecretKeySpec("1Hbfh667adfDEJ78".getBytes(), "AES"));
            byte[] decryptedValue64 = new BASE64Decoder().decodeBuffer(value);
            byte[] decryptedByteValue = cipher.doFinal(decryptedValue64);
            System.err.println(value + " = " + new String(decryptedByteValue, "utf-8"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
