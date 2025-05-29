package org.retrade.storage.service;

import org.retrade.provider.aws.model.S3FileResponse;
import org.retrade.storage.model.constant.FileType;
import org.retrade.storage.model.dto.FileUploadResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Set;

public interface FileService {

    S3FileResponse upload(MultipartFile file);

    Set<S3FileResponse> uploadBulkFile(List<MultipartFile> files);

    FileUploadResponse uploadFile(MultipartFile file, FileType fileType);

    FileUploadResponse uploadFile(byte[] fileData, String originalName, String mimeType, FileType fileType);

    FileUploadResponse uploadAvatar(MultipartFile file);

    FileUploadResponse uploadImage(MultipartFile file);

    FileUploadResponse uploadDocument(MultipartFile file);
}
