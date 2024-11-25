public void downloadAndSaveFiles(String baseUrl, int submissionId, List<Integer> targetIds, String destinationDir) throws IOException {
    // Create a timestamped folder
    String timestamp = String.valueOf(System.currentTimeMillis());
    Path folderPath = Paths.get(destinationDir, "submission_" + submissionId + "_" + timestamp);
    Files.createDirectories(folderPath);

    RestTemplate restTemplate = new RestTemplate();

    for (int targetId : targetIds) {
        String url = String.format("%s/rest/v0/submissions/%d/targets/%d/download/deliverable", baseUrl, submissionId, targetId);

        try {
            // Make GET request to download file
            ResponseEntity<byte[]> response = restTemplate.exchange(url, HttpMethod.GET, null, byte[].class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                // Save file locally
                Path filePath = folderPath.resolve("target_" + targetId + ".file"); // Replace ".file" with actual file extension if known
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
