package org.retrade.storage.controller;

import lombok.RequiredArgsConstructor;
import org.retrade.common.model.dto.response.ResponseObject;
import org.retrade.provider.aws.model.S3FileResponse;
import org.retrade.storage.service.FileService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("files")
public class FileController {
    private final FileService fileService;

    @PostMapping(path = "upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ResponseObject<String>> uploadFile(
            @RequestPart("file") MultipartFile file) {
        var result = fileService.upload(file);
        return ResponseEntity.ok(new ResponseObject.Builder<String>()
                .success(true)
                .code("SUCCESS")
                .content(result.getFileUrl())
                .messages("File uploaded successfully")
                .build());
    }

    @PostMapping(path = "upload/bulk", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ResponseObject<Set<String>>> uploadBulkFiles(
            @RequestPart("files") List<MultipartFile> files) {
        var result = fileService.uploadBulkFile(files);
        var links = result.stream().map(S3FileResponse::getFileUrl).collect(Collectors.toSet());
        return ResponseEntity.ok(new ResponseObject.Builder<Set<String>>()
                .success(true)
                .code("SUCCESS")
                .content(links)
                .messages("Files uploaded successfully")
                .build());
    }
}
