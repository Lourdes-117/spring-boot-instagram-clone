package com.lourdes.inztagram.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class LikePostRequest {
    String userId;
    String postId;
}
