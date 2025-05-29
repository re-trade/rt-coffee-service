package org.retrade.storage.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.retrade.storage.model.constant.FileType;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileUploadResponse {
    
    private String fileName;
    private String originalName;
    private String fileUrl;
    private String mimeType;
    private Long fileSize;
    private String checksum;
    private FileType fileType;
}
