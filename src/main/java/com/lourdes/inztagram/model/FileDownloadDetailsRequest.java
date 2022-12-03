package com.lourdes.inztagram.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class FileDownloadDetailsRequest {
    String userId;
    String fileId;
}
