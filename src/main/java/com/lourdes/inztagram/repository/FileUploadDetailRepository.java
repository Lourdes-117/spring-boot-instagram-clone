package com.lourdes.inztagram.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.lourdes.inztagram.model.FileUploadDetailRequest;

public interface FileUploadDetailRepository extends MongoRepository<FileUploadDetailRequest, String> {
    
}
