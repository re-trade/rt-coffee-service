package org.retrade.storage.service;

import org.retrade.provider.aws.model.S3FileResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.List;
import java.util.Set;

public interface FileService {
    S3FileResponse upload(MultipartFile file);

    Set<S3FileResponse> uploadBulkFile(List<MultipartFile> files);

    File downloadFile(String fileUrl);
}
