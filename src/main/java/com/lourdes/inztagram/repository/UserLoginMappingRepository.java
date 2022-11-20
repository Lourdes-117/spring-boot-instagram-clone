package com.lourdes.inztagram.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Component;

import com.lourdes.inztagram.model.UserLoginMapping;

public interface UserLoginMappingRepository extends MongoRepository<UserLoginMapping, String> {
    
}
