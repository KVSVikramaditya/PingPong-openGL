package com.ms.datalink.globalDatalink.service;

import com.ms.datalink.globalDatalink.model.UploadResourceResponse;
import com.ms.datalink.globalDatalink.model.MetadataSourceFile;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
@RequiredArgsConstructor
public class FileUploadAndVerifyService {

    @Value("${sourcemetadata.input.file.pptfiles}")
    private String sourceFilePath;

    @Autowired
    private AuthService authService;

    @Autowired
    private SubmissionRequestGenerator submissionRequestGenerator;

    private final String backendApiUrl = "https://gl-pilot1.transperfect.com/PD";
    private final RestTemplate restTemplate = new RestTemplate();

    public List<UploadResourceResponse> uploadAllFiles(int submissionId) {
        List<UploadResourceResponse> uploadResponses = new ArrayList<>();
        try {
            // Get the JWT token
            String jwtToken = authService.getToken();

            // Get the batch-file map that was stored during the request generation
            Map<String, List<MetadataSourceFile>> batchFileMap = submissionRequestGenerator.getBatchFileMap();

            // Iterate over each batch and its files
            for (Map.Entry<String, List<MetadataSourceFile>> entry : batchFileMap.entrySet()) {
                String batchName = entry.getKey();
                List<MetadataSourceFile> files = entry.getValue();

                for (MetadataSourceFile metadataSourceFile : files) {
                    // Get the file path
                    String filePath = sourceFilePath + File.separator + metadataSourceFile.getFilename();

                    // Upload the file to the backend API and store the response
                    UploadResourceResponse uploadResponse = uploadFile(filePath, batchName, submissionId, jwtToken);
                    if (uploadResponse != null) {
                        uploadResponses.add(uploadResponse);
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error while uploading files: {}", e.getMessage(), e);
            throw new RuntimeException("Error while uploading files: " + e.getMessage(), e);
        }
        return uploadResponses;
    }

    private UploadResourceResponse uploadFile(String filePath, String batchName, int submissionId, String jwtToken) {
        try {
            // Create a File object for the file to upload
            File file = new File(filePath);
            if (!file.exists()) {
                throw new RuntimeException("File not found: " + filePath);
            }

            // Create a FileSystemResource from the file
            FileSystemResource fileSystemResource = new FileSystemResource(file);

            // Prepare request headers and body
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            headers.setBearerAuth(jwtToken); // Add Bearer token

            MultiValueMap<String, Object> reqBody = new LinkedMultiValueMap<>();
            reqBody.add("file", fileSystemResource);
            reqBody.add("batchName", batchName);
            reqBody.add("fileFormatName", "Default_MS-PowerPoint");

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(reqBody, headers);

            // Send the POST request to upload the file
            String url = backendApiUrl + "/rest/v0/submissions/" + submissionId + "/upload/source";
            ResponseEntity<UploadResourceResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    requestEntity,
                    UploadResourceResponse.class
            );

            if (response.getStatusCode() == HttpStatus.BAD_REQUEST) {
                throw new RuntimeException("BAD REQUEST AT Upload Source for file: " + filePath);
            } else if (response.getStatusCode() == HttpStatus.INTERNAL_SERVER_ERROR) {
                throw new RuntimeException("INTERNAL SERVER ERROR AT Upload Source for file: " + filePath);
            }

            System.out.println("File uploaded successfully: " + filePath);
            return response.getBody();
        } catch (Exception e) {
            log.error("Error uploading file: {}", e.getMessage(), e);
            throw new RuntimeException("Error uploading file: " + e.getMessage(), e);
        }
    }
}



package com.ms.datalink.globalDatalink.controller;

import com.ms.datalink.globalDatalink.model.UploadResourceResponse;
import com.ms.datalink.globalDatalink.service.FileUploadAndVerifyService;
import com.ms.datalink.globalDatalink.service.SubmissionRequestGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/frontend")
public class DocumentSubmissionController {

    @Autowired
    private FileUploadAndVerifyService fileUploadAndVerifyService;

    @Autowired
    private SubmissionRequestGenerator submissionRequestGenerator;

    @PostMapping("/submitDocument")
    public ResponseEntity<?> submitDocument() {
        try {
            // Assuming the submission ID is obtained after creating the submission
            int submissionId = submissionRequestGenerator.createSubmission();  // Mocked or implemented

            // Upload all files and get their upload responses
            List<UploadResourceResponse> uploadResponses = fileUploadAndVerifyService.uploadAllFiles(submissionId);

            // Return the responses
            return ResponseEntity.ok(uploadResponses);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to submit and upload files: " + e.getMessage());
        }
    }
}
