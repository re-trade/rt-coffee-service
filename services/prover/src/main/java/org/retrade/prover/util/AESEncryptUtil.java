package org.retrade.prover.util;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;

public class AESEncryptUtil {
    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/CBC/PKCS5Padding";
    private static final int IV_SIZE = 16;

    public static SecretKey getKeyFromBytes(byte[] keyBytes) {
        return new SecretKeySpec(keyBytes, ALGORITHM);
    }

    public static InputStream encryptStream(InputStream inputStream, SecretKey secretKey) throws GeneralSecurityException, IOException {
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        byte[] iv = new byte[IV_SIZE];
        new SecureRandom().nextBytes(iv);
        IvParameterSpec ivSpec = new IvParameterSpec(iv);

        cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec);
        ByteArrayOutputStream headerStream = new ByteArrayOutputStream();
        headerStream.write(iv);

        return new SequenceInputStream(
                new ByteArrayInputStream(headerStream.toByteArray()),
                new CipherInputStream(inputStream, cipher)
        );
    }

    public static InputStream decryptStream(InputStream encryptedInputStream, SecretKey secretKey) throws GeneralSecurityException, IOException {
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        byte[] iv = new byte[IV_SIZE];
        if (encryptedInputStream.read(iv) != IV_SIZE) {
            throw new IOException("Failed to read IV from encrypted stream.");
        }
        IvParameterSpec ivSpec = new IvParameterSpec(iv);
        cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec);

        return new CipherInputStream(encryptedInputStream, cipher);
    }
}
