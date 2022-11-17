package com.lourdes.inztagram.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString

@Document(collection = "UserLoginMapping")
public class UserLoginMapping {
    @Id
    private String uuid;
    private String username;
}
