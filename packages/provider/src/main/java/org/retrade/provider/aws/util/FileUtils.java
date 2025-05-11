package org.retrade.provider.aws.util;

import org.apache.tika.Tika;

import java.util.Set;
import java.util.UUID;

public class FileUtils {
    public static boolean verifyFile (byte [] file) {
        var tika = new Tika();
        try {
            String type = tika.detect(file);
            return isAllowFileType(type);
        } catch (Exception ex) {
            return false;
        }
    }

    public static boolean isAllowFileType (String type) {
        Set<String> typeAllow = Set.of(
                "image/webp",
                "image/jpeg",
                "image/png",
                "application/pdf"
        );
        return typeAllow.contains(type);
    }
    
    public static String generateFileName(String originalName) {
        return String.format("%s-%s", UUID.randomUUID(), originalName);
    }
}
