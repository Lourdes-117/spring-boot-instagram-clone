package com.lourdes.inztagram.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class GetPostsOfUserRequest {
    String userId;
    String userNameNeeded;
    Integer pagination;
}