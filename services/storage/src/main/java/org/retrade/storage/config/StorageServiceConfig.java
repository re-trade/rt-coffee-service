package org.retrade.storage.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "storage")
public class StorageServiceConfig {

    private VideoConfig video = new VideoConfig();
    private MediaConfig media = new MediaConfig();

    @Data
    public static class VideoConfig {
        private String maxFileSize = "500MB";
        private String supportedFormats = "mp4,avi,mov,mkv,webm";
        private ProcessingConfig processing = new ProcessingConfig();

        @Data
        public static class ProcessingConfig {
            private String chunkSize = "1MB";
            private String timeout = "300s";
        }
    }

    @Data
    public static class MediaConfig {
        private String maxFileSize = "50MB";
        private String supportedImageFormats = "jpg,jpeg,png,gif,webp,bmp";
        private String supportedVideoFormats = "mp4,avi,mov,mkv,webm";
        private String supportedAudioFormats = "mp3,wav,aac,ogg";
        private ThumbnailConfig thumbnail = new ThumbnailConfig();

        @Data
        public static class ThumbnailConfig {
            private int width = 300;
            private int height = 300;
            private int quality = 85;
        }
    }
}
