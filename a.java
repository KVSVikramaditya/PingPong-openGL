public void downloadAndSaveFiles(String baseUrl, int submissionId, List<Integer> targetIds, String destinationDir) throws IOException {
    // Create a timestamped folder for saving the downloaded files
    String timestamp = String.valueOf(System.currentTimeMillis());
    Path folderPath = Paths.get(destinationDir, "submission_" + submissionId + "_" + timestamp);
    Files.createDirectories(folderPath);
    System.out.println("Files will be saved in: " + folderPath.toString());

    for (int targetId : targetIds) {
        // Dynamically construct URL for each targetId
        String url = String.format("%s/rest/v0/submissions/%d/targets/%d/download/deliverable", baseUrl, submissionId, targetId);
        System.out.println("Downloading from URL: " + url);

        try {
            // Set up headers for authentication
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(authService.getToken());

            // Make GET request to download the file
            ResponseEntity<byte[]> response = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), byte[].class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                // Extract file name from Content-Disposition header
                String fileName = extractFileNameFromResponse(response);
                if (fileName == null || fileName.isEmpty()) {
                    fileName = "target_" + targetId; // Default to a generic name if file name is missing
                }

                // Save the file locally with the extracted or default name
                Path filePath = folderPath.resolve(fileName);
                Files.write(filePath, response.getBody());
                System.out.println("File saved: " + filePath.toString());
            } else {
                System.out.println("Failed to download file for targetId: " + targetId + ". HTTP Status: " + response.getStatusCode());
            }
        } catch (Exception e) {
            System.out.println("Error while downloading file for targetId: " + targetId);
            e.printStackTrace();
        }
    }
}
