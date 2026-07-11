package com.filetask.fileTask.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class DocumentResponse {

    private Long id;

    private String documentName;

    private String documentType;

    private String fileName;

    private Long fileSize;

    private LocalDateTime uploadedAt;
}