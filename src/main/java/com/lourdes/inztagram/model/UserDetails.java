package com.lourdes.inztagram.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString

@Document(collection = "UserDetails")
public class UserDetails {
    @Id
    private String userName;
    private String fullName;
    private String emailId;
    private String password;
    private String uuid;
}
