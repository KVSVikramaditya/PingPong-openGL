// Poll the endpoint to check the latest status
String pollUrl = String.format("%s/rest/v0/targets?targetStatus=PROCESSED&submissionIds=%s", backendApiUrl, submissionId);

try {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.setBearerAuth(authService.getToken());

    // Adjusted Response Type
    ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
            pollUrl, HttpMethod.GET, new HttpEntity<>(headers), 
            new ParameterizedTypeReference<List<Map<String, Object>>>() {}
    );

    if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
        List<Map<String, Object>> responseBody = response.getBody();

        // Iterate through the response to update the status
        for (Map<String, Object> target : responseBody) {
            if (target.containsKey("targetId") && target.containsKey("status")) {
                String targetId = target.get("targetId").toString();
                String latestStatus = target.get("status").toString();

                // Update the corresponding row in the CSV
                for (String[] row : readyOrProcessingRows) {
                    if (row[12].equals(targetId)) { // TargetId column
                        row[13] = latestStatus; // Status column
                    }
                }
            }
        }
    }
} catch (Exception e) {
    log.error("Error polling target statuses for submissionId: {}", submissionId, e);
}
