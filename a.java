package com.ms.datalink.globalDatalink.service;

import com.ms.datalink.globalDatalink.model.SaveFileResponse;
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

    private final String backendApiUrl = "https://gl-pilot1.transperfect.com/PD";
    private final RestTemplate restTemplate = new RestTemplate();

    // Method to save the file with autostart set to true
    public SaveFileResponse saveFile(int subId) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(authService.getToken());

            // Create request body with autostart set to true
            String requestBodyJson = "{\"autoStart\": true}";
            HttpEntity<String> requestBody = new HttpEntity<>(requestBodyJson, headers);

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
}
