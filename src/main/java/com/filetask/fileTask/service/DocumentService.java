package com.filetask.fileTask.service;

import com.filetask.fileTask.dto.response.DocumentResponse;
import com.filetask.fileTask.entities.StudentDocument;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface DocumentService {

    DocumentResponse uploadDocument(
            Long studentId,
            String documentName,
            MultipartFile file
    );

    List<DocumentResponse> uploadMultipleDocuments(
            Long studentId,
            MultipartFile[] files
    );

    List<DocumentResponse> getDocumentsByStudentId(Long studentId);

    StudentDocument getDocument(Long documentId);

    byte[] downloadMultipleDocuments(List<Long> documentIds);

    DocumentResponse replaceDocument(
            Long documentId,
            MultipartFile file
    );

    void deleteDocument(Long documentId);
}