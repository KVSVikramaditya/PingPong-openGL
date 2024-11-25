public void downloadAndSaveFiles(String baseUrl, int submissionId, List<Integer> targetIds, String destinationDir) throws IOException {
    // Create a timestamped folder
    String timestamp = String.valueOf(System.currentTimeMillis());
    Path folderPath = Paths.get(destinationDir, "submission_" + submissionId + "_" + timestamp);
    Files.createDirectories(folderPath);

    for (int targetId : targetIds) {
        // Dynamically construct URL for each targetId
        String url = String.format("%s/rest/v0/submissions/%d/targets/%d/download/deliverable", baseUrl, submissionId, targetId);

        try {
            // Make GET request to download the file
            ResponseEntity<byte[]> response = restTemplate.exchange(url, HttpMethod.GET, null, byte[].class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                // Extract file name from Content-Disposition header
                String fileName = extractFileNameFromResponse(response);
                if (fileName == null || fileName.isEmpty()) {
                    fileName = "target_" + targetId; // Default to a generic name if file name is missing
                }

                // Save the file locally with the exact name
                Path filePath = folderPath.resolve(fileName);
                Files.write(filePath, response.getBody());
                System.out.println("File saved: " + filePath.toString());
            } else {
                System.out.println("Failed to download file for targetId: " + targetId);
            }
        } catch (Exception e) {
            System.out.println("Error while downloading file for targetId: " + targetId);
            e.printStackTrace();
        }
    }
}

private String extractFileNameFromResponse(ResponseEntity<byte[]> response) {
    HttpHeaders headers = response.getHeaders();
    if (headers.containsKey(HttpHeaders.CONTENT_DISPOSITION)) {
        String contentDisposition = headers.getFirst(HttpHeaders.CONTENT_DISPOSITION);
        if (contentDisposition != null && contentDisposition.contains("filename=")) {
            // Extract file name from Content-Disposition
            return contentDisposition.split("filename=")[1].replace("\"", "").trim();
        }
    }
    return null; // Return null if no file name is provided in the response
}
