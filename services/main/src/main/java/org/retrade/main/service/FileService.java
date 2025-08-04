package org.retrade.main.service;

import org.retrade.main.model.constant.IdentityCardTypeEnum;
import org.retrade.main.model.dto.response.DecodedFile;
import org.springframework.web.multipart.MultipartFile;

public interface FileService {
    String uploadEncrypted(MultipartFile file);

    DecodedFile getSellerIdentityCard(String sellerId, IdentityCardTypeEnum type);
}
