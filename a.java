public List<RetrieveTargetResponse> getCompletedTargets(int submissionId) {
    try {
        // Get JWT token
        String jwtToken = authService.getToken();

        // Set request headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        headers.setBearerAuth(jwtToken);

        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        // Construct the URL with query parameters
        String url = backendApiUrl + "/rest/v0/targets?targetStatus=PROCESSED&submissionIds=" + submissionId;

        // Make the GET request
        ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                requestEntity,
                String.class // Use String to capture the raw response
        );

        if (response.getStatusCode() == HttpStatus.OK) {
            String rawResponseBody = response.getBody();
            log.info("Raw Response Body: {}", rawResponseBody);

            // Deserialize the response manually if needed
            ObjectMapper objectMapper = new ObjectMapper();
            List<RetrieveTargetResponse> targetResponses = objectMapper.readValue(
                    rawResponseBody,
                    new TypeReference<List<RetrieveTargetResponse>>() {}
            );

            return targetResponses;
        } else {
            throw new RuntimeException("Failed to retrieve completed targets. Status code: " + response.getStatusCode());
        }
    } catch (Exception e) {
        log.error("Error while retrieving completed targets: {}", e.getMessage(), e);
        throw new RuntimeException("Error while retrieving completed targets: " + e.getMessage(), e);
    }
}
