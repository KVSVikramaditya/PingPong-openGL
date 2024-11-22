@PostMapping("/submitDocument")
public ResponseEntity submitDocument(@RequestBody String folderName) throws Exception {
    this.folderName = folderName;
    String jwtToken = OAuthService.getToken();
    String sourceFilePath = incomingFilePath + folderName + "/metadata.csv";

    List<MetadataSourceFile> metadataList = metadataSourceFilesReader.processMetadata(sourceFilePath);
    String submissionRequestJson = submissionRequestGenerator.createSubmissionRequest(metadataList);

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.setBearerAuth(jwtToken);
    HttpEntity<String> requestEntity = new HttpEntity<>(submissionRequestJson, headers);

    // Make the POST request to the backend API
    ResponseEntity<CreateSubmissionResponse> response = restTemplate.exchange(
        backendApiUrl + "/rest/vo/submissions/create",
        HttpMethod.POST,
        requestEntity,
        CreateSubmissionResponse.class
    );

    if (response.getStatusCode() == HttpStatus.OK) {
        submissionId = response.getBody().getSubmissionId();

        // Use SaveFileService to save files and get the response
        SaveFileResponse saveFileResponse = saveFileService.saveFiles(submissionId, sourceFilePath);

        // Check if the submissionId is in startedSubmissionIds
        if (!saveFileResponse.getStartedSubmissionIds().contains(submissionId)) {
            System.out.println("Submission ID not found in started submissions. Aborting file transfer.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Submission ID not found in started submissions. File transfer aborted.");
        }

        // Proceed with file transfer
        try {
            Path sourceFolderPath = Paths.get(incomingFilePath + this.folderName);
            Path targetFolderPath = Paths.get(processingFilePath, this.folderName);

            if (!Files.exists(targetFolderPath.getParent())) {
                Files.createDirectories(targetFolderPath.getParent());
            }

            Files.move(sourceFolderPath, targetFolderPath);
            System.out.println("Folder moved to: " + targetFolderPath.toString());

            // Return success response
            return ResponseEntity.ok("Folder moved successfully to: " + targetFolderPath.toString());
        } catch (IOException e) {
            throw new RuntimeException("Failed to move folder: " + folderName, e);
        }
    } else {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Failed to create submission.");
    }
}
