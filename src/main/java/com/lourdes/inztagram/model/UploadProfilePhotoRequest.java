package com.lourdes.inztagram.model;

import org.springframework.web.multipart.MultipartFile;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UploadProfilePhotoRequest {
    String userId;
    MultipartFile imageFile;
}
