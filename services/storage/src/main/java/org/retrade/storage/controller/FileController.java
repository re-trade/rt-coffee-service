package org.retrade.storage.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "File Management", description = "APIs for file upload, download, and management without database storage")
public class FileController {
    private final FileService fileService;

    @PostMapping(path = "upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload a file")
    public ResponseEntity<ResponseObject<String>> uploadFile(
            @Parameter(description = "File to upload") @RequestPart("file") MultipartFile file) {
        var result = fileService.upload(file);
        return ResponseEntity.ok(new ResponseObject.Builder<String>()
                .success(true)
                .code("SUCCESS")
                .content(result.getFileUrl())
                .messages("File uploaded successfully")
                .build());
    }

    @PostMapping(path = "upload/bulk", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload multiple files")
    public ResponseEntity<ResponseObject<Set<String>>> uploadBulkFiles(
            @Parameter(description = "Files to upload") @RequestPart("files") List<MultipartFile> files) {
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
