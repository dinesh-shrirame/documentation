package com.filetask.fileTask.controller;

import com.filetask.fileTask.dto.response.DocumentResponse;
import com.filetask.fileTask.entities.StudentDocument;
import com.filetask.fileTask.exception.FileStorageException;
import com.filetask.fileTask.service.DocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;


    // ================= UPLOAD SINGLE DOCUMENT =================

    @PostMapping(
            value = "/upload/{studentId}",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<DocumentResponse> uploadDocument(
            @PathVariable("studentId") Long studentId,
            @RequestParam(value = "documentName", required = false) String documentName,
            @RequestParam("file") MultipartFile file) {

        DocumentResponse response =
                documentService.uploadDocument(
                        studentId,
                        documentName,
                        file
                );

        return ResponseEntity.ok(response);
    }

    // ================= UPLOAD MULTIPLE DOCUMENTS =================

    @PostMapping(
            value = "/upload-multiple/{studentId}",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<List<DocumentResponse>> uploadMultipleDocuments(
            @PathVariable("studentId") Long studentId,
            @RequestParam("files") MultipartFile[] files) {

        List<DocumentResponse> responses =
                documentService.uploadMultipleDocuments(
                        studentId,
                        files
                );

        return ResponseEntity.ok(responses);
    }


    // ================= GET STUDENT DOCUMENTS =================

    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<DocumentResponse>> getStudentDocuments(
            @PathVariable("studentId") Long studentId) {

        List<DocumentResponse> documents =
                documentService.getDocumentsByStudentId(studentId);

        return ResponseEntity.ok(documents);
    }


    // ================= DOWNLOAD SINGLE DOCUMENT =================

    @GetMapping("/download/{documentId}")
    public ResponseEntity<byte[]> downloadDocument(
            @PathVariable("documentId") Long documentId)
            throws IOException {

        StudentDocument document =
                documentService.getDocument(documentId);

        Path filePath = Path.of(document.getFilePath());

        byte[] fileData =
                Files.readAllBytes(filePath);

        MediaType mediaType =
                MediaType.APPLICATION_OCTET_STREAM;

        if (document.getDocumentType() != null) {

            try {

                mediaType = MediaType.parseMediaType(
                        document.getDocumentType()
                );

            } catch (Exception ignored) {

                mediaType =
                        MediaType.APPLICATION_OCTET_STREAM;
            }
        }

        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" +
                                document.getFileName() +
                                "\""
                )
                .body(fileData);
    }


    // ================= DOWNLOAD MULTIPLE DOCUMENTS AS ZIP =================

    @PostMapping("/download-zip")
    public ResponseEntity<byte[]> downloadMultipleDocuments(
            @RequestBody(required = false) List<Long> documentIds) {

        if (documentIds == null || documentIds.isEmpty()) {
            throw new FileStorageException("Request body must contain a non-empty list of document IDs, e.g. [1,2,3]");
        }

        byte[] zipData =
                documentService.downloadMultipleDocuments(
                        documentIds
                );

        return ResponseEntity.ok()
                .contentType(
                        MediaType.parseMediaType(
                                "application/zip"
                        )
                )
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"student_documents.zip\""
                )
                .contentLength(zipData.length)
                .body(zipData);
    }

    // ================= REPLACE DOCUMENT =================

    @PutMapping(
            value = "/{documentId}",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<DocumentResponse> replaceDocument(
            @PathVariable("documentId") Long documentId,
            @RequestParam("file") MultipartFile file) {

        DocumentResponse response =
                documentService.replaceDocument(
                        documentId,
                        file
                );

        return ResponseEntity.ok(response);
    }


    // ================= DELETE DOCUMENT =================

    @DeleteMapping("/{documentId}")
    public ResponseEntity<String> deleteDocument(
            @PathVariable("documentId") Long documentId) {

        documentService.deleteDocument(documentId);

        return ResponseEntity.ok(
                "Document deleted successfully"
        );
    }
}