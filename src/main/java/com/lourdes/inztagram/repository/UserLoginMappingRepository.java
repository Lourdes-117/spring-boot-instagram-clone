package com.lourdes.inztagram.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.lourdes.inztagram.model.UserLoginMapping;


@Repository
public interface UserLoginMappingRepository extends MongoRepository<UserLoginMapping, String> {
    
}
