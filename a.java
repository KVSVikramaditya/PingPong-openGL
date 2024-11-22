package com.ms.datalink.globalDatalink.model;

import lombok.Data;

import java.util.List;

@Data
public class UploadResourceResponse {
    private String processId;
    private int submissionId;
    private List<DocumentIdEntry> documentIds;

    @Data
    public static class DocumentIdEntry {
        private String name;
        private int documentId;
    }
}
