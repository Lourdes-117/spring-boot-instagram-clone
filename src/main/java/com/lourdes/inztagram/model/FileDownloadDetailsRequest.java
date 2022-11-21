package com.lourdes.inztagram.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FileDownloadDetailsRequest {
    String userId;
    String fileId;
}
