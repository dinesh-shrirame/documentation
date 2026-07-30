package com.filetask.fileTask.service.impl;

import com.filetask.fileTask.dto.request.StudentRequest;
import com.filetask.fileTask.dto.response.StudentResponse;
import com.filetask.fileTask.entities.Student;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.filetask.fileTask.repository.StudentRepository;
import com.filetask.fileTask.service.StudentService;

@Service
@RequiredArgsConstructor
public class StudentServiceImpl implements StudentService {

    private final StudentRepository studentRepository;


    @Override
    public StudentResponse createStudent(StudentRequest request) {
        System.out.println("Document Upload..");
        Student student = Student.builder()
                .name(request.getName())
                .email(request.getEmail())
                .mobileNumber(request.getMobileNumber())
                .build();

        Student savedStudent = studentRepository.save(student);

        return StudentResponse.builder()
                .id(savedStudent.getId())
                .name(savedStudent.getName())
                .email(savedStudent.getEmail())
                .mobileNumber(savedStudent.getMobileNumber())
                .build();

    }
}