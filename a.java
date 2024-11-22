package com.ms.datalink.globalDatalink.service;

import com.ms.datalink.globalDatalink.model.RetrieveTargetResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@RequiredArgsConstructor
@Slf4j
@Component
public class PollingWithSubmissionId {

    @Autowired
    private AuthService authService;

    private final String backendApiUrl = "https://gl-pilot1.transperfect.com/PD";
    private final RestTemplate restTemplate = new RestTemplate();

    // Method to get the completed targets based on submission ID
    public List<RetrieveTargetResponse> getCompletedTargets(int submissionId) {
        try {
            // Get JWT token
            String jwtToken = authService.getToken();

            // Set request headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(jwtToken);

            HttpEntity<String> requestEntity = new HttpEntity<>(headers);

            // Make the GET request
            String url = backendApiUrl + "/rest/v0/targets?targetStatus=PROCESSED&submissionIds=" + submissionId;

            ResponseEntity<List<RetrieveTargetResponse>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    requestEntity,
                    new ParameterizedTypeReference<List<RetrieveTargetResponse>>() {}
            );

            if (response.getStatusCode() == HttpStatus.OK) {
                log.info("Successfully retrieved targets for submission ID: {}", submissionId);
                return response.getBody();
            } else {
                throw new RuntimeException("Failed to retrieve completed targets. Status code: " + response.getStatusCode());
            }
        } catch (Exception e) {
            log.error("Error while retrieving completed targets: {}", e.getMessage(), e);
            throw new RuntimeException("Error while retrieving completed targets: " + e.getMessage(), e);
        }
    }
}


@RestController
@RequestMapping("/api/frontend")
public class DocumentSubmissionController {

    @Autowired
    private PollingWithSubmissionId pollingWithSubmissionId;

    @GetMapping("/completedTargets/{submissionId}")
    public ResponseEntity<?> getCompletedTargets(@PathVariable int submissionId) {
        try {
            // Fetch completed targets for the given submission ID
            List<RetrieveTargetResponse> completedTargets = pollingWithSubmissionId.getCompletedTargets(submissionId);

            return ResponseEntity.ok(completedTargets);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to retrieve completed targets: " + e.getMessage());
        }
    }
}
