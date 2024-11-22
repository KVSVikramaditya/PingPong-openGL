package com.ms.datalink.globalDatalink.service;

import com.ms.datalink.globalDatalink.model.SaveFileRequest;
import com.ms.datalink.globalDatalink.model.SaveFileResponse;
import com.ms.datalink.globalDatalink.model.StartSubmissionResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@RequiredArgsConstructor
@Slf4j
@Component
public class SaveFileService {

    @Autowired
    private AuthService authService;

    @Autowired
    private TokenService tokenService;

    private final String backendApiUrl = "https://gl-pilot1.transperfect.com/PD";
    private final RestTemplate restTemplate = new RestTemplate();

    // Main method to save file, analyze it, and start submission
    public StartSubmissionResponse saveFileAndStartSubmission(int subId) {
        try {
            // Step 1: Save File
            SaveFileResponse saveFileResponse = saveFile(subId);
            if (saveFileResponse != null) {
                log.info("Save File step completed successfully for Submission ID: {}", subId);

                // Step 2: Analyze File
                StartSubmissionResponse analyzeResponse = analyzeFile(subId);
                if (analyzeResponse != null) {
                    log.info("Analyze File step completed successfully for Submission ID: {}", subId);

                    // Step 3: Start Submission
                    StartSubmissionResponse startResponse = startSubmission(subId);
                    if (startResponse != null) {
                        log.info("Start Submission step completed successfully for Submission ID: {}", subId);
                        return startResponse;
                    } else {
                        log.error("Failed to start submission for Submission ID: {}", subId);
                        throw new RuntimeException("Failed to start submission for Submission ID: " + subId);
                    }
                } else {
                    log.error("Failed to analyze file for Submission ID: {}", subId);
                    throw new RuntimeException("Failed to analyze file for Submission ID: " + subId);
                }
            } else {
                log.error("Failed to save file for Submission ID: {}", subId);
                throw new RuntimeException("Failed to save file for Submission ID: " + subId);
            }
        } catch (Exception e) {
            log.error("Error in saveFileAndStartSubmission for Submission ID: {}: {}", subId, e.getMessage(), e);
            throw new RuntimeException("Error in saveFileAndStartSubmission for Submission ID: " + subId, e);
        }
    }

    // Method to save the file
    private SaveFileResponse saveFile(int subId) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(authService.getToken());

            SaveFileRequest saveFileRequest = new SaveFileRequest(false); // autoStart set to false
            HttpEntity<SaveFileRequest> requestBody = new HttpEntity<>(saveFileRequest, headers);

            String url = backendApiUrl + "/rest/v0/submissions/" + subId + "/save";
            ResponseEntity<SaveFileResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    requestBody,
                    SaveFileResponse.class
            );

            return response.getBody();
        } catch (Exception e) {
            log.error("Error in saveFile for Submission ID: {}: {}", subId, e.getMessage(), e);
            throw new RuntimeException("Error in saveFile for Submission ID: " + subId, e);
        }
    }

    // Method to analyze the file
    private StartSubmissionResponse analyzeFile(int subId) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(authService.getToken());

            HttpEntity<Void> requestBody = new HttpEntity<>(headers);

            String url = backendApiUrl + "/rest/v0/submissions/" + subId + "/analyze";
            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    requestBody,
                    String.class
            );

            if (response.getBody() != null) {
                return startSubmission(subId);
            } else {
                return null;
            }
        } catch (Exception e) {
            log.error("Error in analyzeFile for Submission ID: {}: {}", subId, e.getMessage(), e);
            throw new RuntimeException("Error in analyzeFile for Submission ID: " + subId, e);
        }
    }

    // Method to start the submission
    private StartSubmissionResponse startSubmission(int subId) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(authService.getToken());

            HttpEntity<Void> requestBody = new HttpEntity<>(headers);

            String url = backendApiUrl + "/rest/v0/submissions/" + subId + "/start";
            ResponseEntity<StartSubmissionResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    requestBody,
                    StartSubmissionResponse.class
            );

            return response.getBody();
        } catch (Exception e) {
            log.error("Error in startSubmission for Submission ID: {}: {}", subId, e.getMessage(), e);
            throw new RuntimeException("Error in startSubmission for Submission ID: " + subId, e);
        }
    }
}
