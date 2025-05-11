package org.retrade.provider.aws.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class S3FileRequest {
    private byte[] file;
    private String fileName;
}
