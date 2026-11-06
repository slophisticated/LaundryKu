package laundryku.util;

import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/** Hash password memakai PBKDF2 + salt acak sehingga password asli tidak disimpan di database. */
public final class Password {
    private static final int ITERASI = 120_000;
    private static final int PANJANG_BIT = 256;
    private static final SecureRandom RANDOM = new SecureRandom();

    private Password() { }

    public static String hash(String password) {
        byte[] salt = new byte[16];
        RANDOM.nextBytes(salt);
        Base64.Encoder b64 = Base64.getEncoder();
        return ITERASI + ":" + b64.encodeToString(salt) + ":"
                + b64.encodeToString(pbkdf2(password.toCharArray(), salt, ITERASI));
    }

    public static boolean cocok(String password, String tersimpan) {
        if (password == null || tersimpan == null) { return false; }
        String[] bagian = tersimpan.split(":");
        if (bagian.length != 3) { return false; }
        try {
            int iterasi = Integer.parseInt(bagian[0]);
            byte[] salt = Base64.getDecoder().decode(bagian[1]);
            byte[] hash = Base64.getDecoder().decode(bagian[2]);
            return MessageDigest.isEqual(hash, pbkdf2(password.toCharArray(), salt, iterasi));
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private static byte[] pbkdf2(char[] password, byte[] salt, int iterasi) {
        try {
            PBEKeySpec spec = new PBEKeySpec(password, salt, iterasi, PANJANG_BIT);
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Hash password gagal dibuat.", e);
        }
    }
}
