package com.lourdes.inztagram.model;

import org.springframework.data.annotation.Id;
import org.springframework.web.multipart.MultipartFile;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FileUploadDetailRequest {
    @Id
    String fileId;
    String userId;
    String uploadedUserId;
    String uploadedUserName;
    String imageCaption;
    MultipartFile imageFile;
}
