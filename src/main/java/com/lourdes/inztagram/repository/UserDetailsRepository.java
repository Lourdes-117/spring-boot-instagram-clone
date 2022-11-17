package com.lourdes.inztagram.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import com.lourdes.inztagram.model.UserDetails;

public interface UserDetailsRepository extends MongoRepository<UserDetails, String> {
    
}
