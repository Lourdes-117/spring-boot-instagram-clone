package com.lourdes.inztagram.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GetPostsOfUserRequest {
    String userId;
    String userNameNeeded;
    Integer pagination;
}