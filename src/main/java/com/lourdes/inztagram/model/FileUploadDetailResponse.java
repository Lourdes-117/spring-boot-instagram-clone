package com.lourdes.inztagram.model;

import java.util.ArrayList;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("FileUploadDetail")
public class FileUploadDetailResponse {
    @Id
    String fileId;
    String uploadedUserId;
    String uploadedUserName;
    String filePath;
    String imageCaption;
    ArrayList<String> likes;
}