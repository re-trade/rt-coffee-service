package org.retrade.prover.util;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.InputStream;
import java.security.GeneralSecurityException;

public class AESEncryptUtil {
    private static final String ALGORITHM = "AES";

    public static SecretKey getKeyFromBytes(byte[] keyBytes) {
        return new SecretKeySpec(keyBytes, ALGORITHM);
    }

    public static CipherInputStream encryptStream(InputStream inputStream, SecretKey secretKey) throws GeneralSecurityException {
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        return new CipherInputStream(inputStream, cipher);
    }
}
