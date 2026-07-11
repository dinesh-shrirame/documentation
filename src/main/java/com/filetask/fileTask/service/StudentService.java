package com.filetask.fileTask.service;


import com.filetask.fileTask.dto.request.StudentRequest;
import com.filetask.fileTask.dto.response.StudentResponse;

public interface StudentService {

    StudentResponse createStudent(StudentRequest request);
}