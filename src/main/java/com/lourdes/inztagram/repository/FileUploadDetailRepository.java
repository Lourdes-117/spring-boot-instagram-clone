package com.lourdes.inztagram.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.lourdes.inztagram.model.FileUploadDetailRequest;

@Repository
public interface FileUploadDetailRepository extends MongoRepository<FileUploadDetailRequest, String> {
    
}
