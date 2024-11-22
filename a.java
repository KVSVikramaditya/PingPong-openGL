package com.ms.datalink.globalDatalink.model;

import java.util.List;

public class UploadResourceResponse {
    private String processId;
    private int submissionId;
    private List<DocumentIdEntry> documentIds;

    // Getters and Setters
    public String getProcessId() {
        return processId;
    }

    public void setProcessId(String processId) {
        this.processId = processId;
    }

    public int getSubmissionId() {
        return submissionId;
    }

    public void setSubmissionId(int submissionId) {
        this.submissionId = submissionId;
    }

    public List<DocumentIdEntry> getDocumentIds() {
        return documentIds;
    }

    public void setDocumentIds(List<DocumentIdEntry> documentIds) {
        this.documentIds = documentIds;
    }

    // Inner class to represent each document ID entry
    public static class DocumentIdEntry {
        private String name;
        private int documentId;

        // Getters and Setters
        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getDocumentId() {
            return documentId;
        }

        public void setDocumentId(int documentId) {
            this.documentId = documentId;
        }
    }
}
