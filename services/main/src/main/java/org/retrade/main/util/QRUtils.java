package org.retrade.main.util;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;

import java.awt.image.BufferedImage;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class QRUtils {
    public static BufferedImage generateQRCode(String data, int width, int height) {
        try {
            BitMatrix bitMatrix = new MultiFormatWriter().encode(data, BarcodeFormat.QR_CODE, width, height);
            return MatrixToImageWriter.toBufferedImage(bitMatrix);
        } catch(Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public static String generateSKU() {
        var prefix = "SKU";
        var datePart = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        var randomPart = String.format("%s%s", generateRandomAlphanumeric(), generateRandomDigits()) ;
        return String.format("%s-%s-%s", prefix, datePart, randomPart);
    }

    private static String generateRandomAlphanumeric() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        StringBuilder sb = new StringBuilder();
        SecureRandom rand = new SecureRandom();
        for(int i = 0; i < 3; i++) {
            sb.append(chars.charAt(rand.nextInt(chars.length())));
        }
        return sb.toString();
    }

    private static String generateRandomDigits() {
        StringBuilder sb = new StringBuilder();
        SecureRandom rand = new SecureRandom();
        for(int i = 0; i < 3; i++) {
            sb.append(rand.nextInt(10));
        }
        return sb.toString();
    }
}
