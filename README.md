# PING-PONG-GAME
Two Dimentional game using FreeGlut or using OpenGL


package com.ms.datalink.globalDatalink.service;

import com.ms.datalink.globalDatalink.model.MetadataSourceFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.core.io.FileSystemResource;

import java.io.File;
import java.util.List;
import java.util.Map;

@Component
public class FileUploadAndVerifyService {

    @Autowired
    private OAuthService oAuthService;

    @Autowired
    private SubmissionRequestGenerator submissionRequestGenerator;

    private final String backendApiUrl = "https://gl-pilot1.transperfect.com/PD";
    private final RestTemplate restTemplate = new RestTemplate();

    public void uploadAllFiles(int submissionId) {
        try {
            // Get the JWT token
            String jwtToken = oAuthService.getToken();

            // Get the batch-file map that was stored during the request generation
            Map<String, List<MetadataSourceFile>> batchFileMap = submissionRequestGenerator.getBatchFileMap();

            // Iterate over each batch and its files
            for (Map.Entry<String, List<MetadataSourceFile>> entry : batchFileMap.entrySet()) {
                String batchName = entry.getKey();
                List<MetadataSourceFile> files = entry.getValue();

                for (MetadataSourceFile metadataSourceFile : files) {
                    // Get the file path
                    String filePath = metadataSourceFile.getFilename();

                    // Upload the file to the backend API
                    uploadFile(filePath, batchName, submissionId, jwtToken);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error while uploading files: " + e.getMessage());
        }
    }

    private String uploadFile(String filePath, String batchName, int submissionId, String jwtToken) {
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
            headers.setBearerAuth(jwtToken);  // Add Bearer token

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
            return response.getBody().getProcessId();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error uploading file: " + e.getMessage());
        }
    }
}
