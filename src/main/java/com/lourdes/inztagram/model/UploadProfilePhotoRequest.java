package com.lourdes.inztagram.model;

import org.springframework.web.multipart.MultipartFile;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class UploadProfilePhotoRequest {
    String userId;
    MultipartFile imageFile;
}
