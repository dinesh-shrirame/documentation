package com.filetask.fileTask.service.impl;

import com.filetask.fileTask.dto.response.DocumentResponse;
import com.filetask.fileTask.entities.Student;
import com.filetask.fileTask.entities.StudentDocument;
import com.filetask.fileTask.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.filetask.fileTask.repository.DocumentRepository;
import com.filetask.fileTask.repository.StudentRepository;
import com.filetask.fileTask.service.DocumentService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import com.filetask.fileTask.exception.FileStorageException;

@Service
@RequiredArgsConstructor
public class DocumentServiceImpl implements DocumentService {

    private final DocumentRepository documentRepository;

    private final StudentRepository studentRepository;

    @Value("${file.upload-dir}")
    private String uploadDirectory;

    @Override
    public DocumentResponse uploadDocument(
            Long studentId,
            String documentName,
            MultipartFile file) {

        if (file == null || file.isEmpty()) {
            throw new FileStorageException("Uploaded file must not be empty");
        }

        Student student = studentRepository
                .findById(studentId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Student not found: " + studentId
                        )
                );

        try {

            Path uploadPath = Path.of(uploadDirectory)
                    .toAbsolutePath()
                    .normalize();

            Files.createDirectories(uploadPath);

            String originalFileName =
                    Path.of(file.getOriginalFilename())
                            .getFileName()
                            .toString();

            String storedFileName =
                    UUID.randomUUID() + "_" + originalFileName;

            Path targetPath = uploadPath
                    .resolve(storedFileName)
                    .normalize();

            Files.copy(
                    file.getInputStream(),
                    targetPath,
                    StandardCopyOption.REPLACE_EXISTING
            );

            // Fallback: if the client didn't send documentName, derive it from the file name
            String resolvedDocumentName = resolveDocumentName(documentName, originalFileName);

            StudentDocument document =
                    StudentDocument.builder()
                            .documentName(resolvedDocumentName)
                            .documentType(file.getContentType())
                            .fileName(originalFileName)
                            .filePath(targetPath.toString())
                            .fileSize(file.getSize())
                            .uploadedAt(LocalDateTime.now())
                            .student(student)
                            .build();

            StudentDocument savedDocument =
                    documentRepository.save(document);

            return mapToResponse(savedDocument);

        } catch (IOException exception) {

            throw new FileStorageException(
                    "Unable to upload document",
                    exception
            );
        }
    }

    @Override
    public List<DocumentResponse> uploadMultipleDocuments(
            Long studentId,
            MultipartFile[] files) {

        if (files == null || files.length == 0) {
            throw new FileStorageException("At least one file must be uploaded");
        }

        List<DocumentResponse> responses =
                new ArrayList<>();

        for (MultipartFile file : files) {

            // Pass null (not file.getOriginalFilename()) so the same fallback
            // logic in uploadDocument() strips the extension consistently.
            responses.add(
                    uploadDocument(
                            studentId,
                            null,
                            file
                    )
            );
        }

        return responses;
    }

    @Override
    public List<DocumentResponse> getDocumentsByStudentId(
            Long studentId) {

        if (!studentRepository.existsById(studentId)) {

            throw new ResourceNotFoundException(
                    "Student not found: " + studentId
            );
        }

        return documentRepository
                .findByStudentId(studentId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public StudentDocument getDocument(Long documentId) {

        return documentRepository
                .findById(documentId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Document not found: " + documentId
                        )
                );
    }

    @Override
    public byte[] downloadMultipleDocuments(
            List<Long> documentIds) {

        if (documentIds == null || documentIds.isEmpty()) {

            throw new FileStorageException(
                    "Document IDs cannot be empty"
            );
        }

        List<StudentDocument> documents =
                documentRepository.findAllByIdIn(documentIds);

        if (documents.size() != documentIds.size()) {

            throw new ResourceNotFoundException(
                    "One or more documents not found"
            );
        }

        return ZipUtility.createZip(documents);
    }

    @Override
    public DocumentResponse replaceDocument(
            Long documentId,
            MultipartFile file) {

        if (file == null || file.isEmpty()) {
            throw new FileStorageException("Replacement file must not be empty");
        }

        StudentDocument document =
                getDocument(documentId);

        try {

            Path oldFilePath =
                    Path.of(document.getFilePath());

            Files.deleteIfExists(oldFilePath);

            String originalFileName =
                    Path.of(file.getOriginalFilename())
                            .getFileName()
                            .toString();

            String storedFileName =
                    UUID.randomUUID() + "_" + originalFileName;

            Path uploadPath = Path.of(uploadDirectory)
                    .toAbsolutePath()
                    .normalize();

            Files.createDirectories(uploadPath);

            Path newFilePath = uploadPath
                    .resolve(storedFileName)
                    .normalize();

            Files.copy(
                    file.getInputStream(),
                    newFilePath,
                    StandardCopyOption.REPLACE_EXISTING
            );

            document.setFileName(originalFileName);
            document.setFilePath(newFilePath.toString());
            document.setDocumentType(file.getContentType());
            document.setFileSize(file.getSize());
            document.setUploadedAt(LocalDateTime.now());

            StudentDocument updatedDocument =
                    documentRepository.save(document);

            return mapToResponse(updatedDocument);

        } catch (IOException exception) {

            throw new FileStorageException(
                    "Unable to replace document",
                    exception
            );
        }
    }

    @Override
    public void deleteDocument(Long documentId) {

        StudentDocument document =
                getDocument(documentId);

        try {

            Files.deleteIfExists(
                    Path.of(document.getFilePath())
            );

            documentRepository.delete(document);

        } catch (IOException exception) {

            throw new FileStorageException(
                    "Unable to delete document",
                    exception
            );
        }
    }

    private String resolveDocumentName(String documentName, String originalFileName) {
        if (documentName != null && !documentName.isBlank()) {
            return documentName;
        }
        int dotIndex = originalFileName.lastIndexOf('.');
        return dotIndex > 0 ? originalFileName.substring(0, dotIndex) : originalFileName;
    }

    private DocumentResponse mapToResponse(
            StudentDocument document) {

        return DocumentResponse.builder()
                .id(document.getId())
                .documentName(document.getDocumentName())
                .documentType(document.getDocumentType())
                .fileName(document.getFileName())
                .fileSize(document.getFileSize())
                .uploadedAt(document.getUploadedAt())
                .build();
    }
}