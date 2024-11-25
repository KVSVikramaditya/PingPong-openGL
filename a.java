public void downloadAndSaveFiles(String baseUrl, int submissionId, List<Integer> targetIds, List<RetrieveTargetResponse> completedTargets, String destinationDir) throws IOException {
    // Create a timestamped folder for saving the downloaded files
    String timestamp = String.valueOf(System.currentTimeMillis());
    Path folderPath = Paths.get(destinationDir, "submission_" + submissionId + "-" + timestamp);
    Files.createDirectories(folderPath);
    System.out.println("Files will be saved in: " + folderPath.toString());

    List<String[]> metadataEntries = new ArrayList<>();
    metadataEntries.add(new String[] {"Source File Name", "Target File Name", "Target ID", "Target Language", "Source Language", "Status"}); // CSV Header

    for (RetrieveTargetResponse target : completedTargets) {
        int targetId = Integer.parseInt(target.getTargetid());

        // Skip targets not in the provided targetIds list
        if (!targetIds.contains(targetId)) {
            continue;
        }

        // Prepare file names
        String targetLanguageCode = target.getTargetLanguage().substring(0, 2); // First two letters of targetLanguage
        String originalFileName = target.getDocumentName(); // e.g., "example.pptx"

        // Extract the file name and extension
        String baseFileName = originalFileName.contains(".") ? originalFileName.substring(0, originalFileName.lastIndexOf(".")) : originalFileName;
        String fileExtension = originalFileName.contains(".") ? originalFileName.substring(originalFileName.lastIndexOf(".")) : ""; // Includes the dot (e.g., ".pptx")

        String targetFileName = baseFileName + "_" + targetLanguageCode + fileExtension; // e.g., "example_fr.pptx"

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
                // Save the file locally
                Path filePath = folderPath.resolve(targetFileName);
                Files.write(filePath, response.getBody());
                System.out.println("File saved: " + filePath.toString());

                // Add metadata entry for the file
                metadataEntries.add(new String[] {
                        originalFileName,       // Source File Name
                        targetFileName,         // Target File Name
                        target.getTargetid(),   // Target ID
                        target.getTargetLanguage(),
                        target.getSourceLanguage(),
                        "Downloaded"            // Status
                });
            } else {
                System.out.println("Failed to download file for targetId: " + targetId + ". HTTP Status: " + response.getStatusCode());
            }
        } catch (Exception e) {
            System.out.println("Error while downloading file for targetId: " + targetId);
            e.printStackTrace();
        }
    }

    // Save metadata.csv file in the same folder
    saveMetadataCsv(folderPath.resolve("metadata.csv"), metadataEntries);
}

private void saveMetadataCsv(Path csvFilePath, List<String[]> metadataEntries) {
    try (BufferedWriter writer = Files.newBufferedWriter(csvFilePath);
         CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT)) {
        for (String[] entry : metadataEntries) {
            csvPrinter.printRecord((Object[]) entry);
        }
        System.out.println("Metadata saved as CSV: " + csvFilePath.toString());
    } catch (IOException e) {
        System.out.println("Error while saving metadata.csv");
        e.printStackTrace();
    }
}
