package com.lourdes.inztagram.model;

import java.util.ArrayList;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.web.multipart.MultipartFile;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString

@Document("FileUploadMapping")
public class FileUploadDetailRequest {
    @Id
    String fileId;
    
    String userId;
    String filePath;
    String userName;
    String imageCaption;
    ArrayList<String> likes;
    MultipartFile imageFile;
}
