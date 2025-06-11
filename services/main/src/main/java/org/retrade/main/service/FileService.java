package org.retrade.main.service;

import org.springframework.web.multipart.MultipartFile;

public interface FileService {
    String uploadEncrypted(MultipartFile file);
}
