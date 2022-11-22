package com.lourdes.inztagram.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GetDetailsOfUserRequest {
    String requestingUserId;
    String userNameToGetDetails;
}
