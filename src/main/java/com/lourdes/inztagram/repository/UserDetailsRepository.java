package com.lourdes.inztagram.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.lourdes.inztagram.model.UserDetails;

@Repository
public interface UserDetailsRepository extends MongoRepository<UserDetails, String> {
    
}
