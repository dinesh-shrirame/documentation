package com.filetask.fileTask.repository;

import com.filetask.fileTask.entities.StudentDocument;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DocumentRepository
        extends JpaRepository<StudentDocument, Long> {

    List<StudentDocument> findByStudentId(Long studentId);

    List<StudentDocument> findAllByIdIn(List<Long> ids);
}