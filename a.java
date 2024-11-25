@Service
public class DownloadFiles {

    @Autowired
    private RestTemplate restTemplate;

    public void downloadAndSaveFiles(String baseUrl, int submissionId, List<Integer> targetIds, String destinationDir) throws IOException {
        // Create a timestamped folder
        String timestamp = String.valueOf(System.currentTimeMillis());
        Path folderPath = Paths.get(destinationDir, "submission_" + submissionId + "_" + timestamp);
        Files.createDirectories(folderPath);

        for (int targetId : targetIds) {
            // Dynamically construct URL for each targetId
            String url = String.format("%s/%d/targets/%d/download/deliverable", baseUrl, submissionId, targetId);

            try {
                // Make GET request to download file
                ResponseEntity<byte[]> response = restTemplate.exchange(url, HttpMethod.GET, null, byte[].class);

                if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                    // Save file locally
                    String fileName = "target_" + targetId + ".file"; // Replace ".file" with actual extension if known
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
}


@PostMapping("/downloadProcessedFiles")
public ResponseEntity<?> downloadProcessedFiles(@RequestBody int submissionId) {
    try {
        // Step 1: Get completed targets
        List<RetrieveTargetResponse> completedTargets = pollingWithSubmissionId.getCompletedTargets(submissionId);
        System.out.println("Completed Targets: " + completedTargets);

        // Step 2: Get processed target IDs
        List<Integer> processedTargetIds = processedTargetId.getProcessedTargetIds(completedTargets);
        System.out.println("Processed Target IDs: " + processedTargetIds);

        // Step 3: Define the base URL and destination directory
        String baseUrl = "http://your-pd-url/rest/v0/submissions"; // Replace with actual base URL
        String destinationDir = "/path/to/destination"; // Replace with actual destination directory

        // Step 4: Download files and save locally
        downloadFiles.downloadAndSaveFiles(baseUrl, submissionId, processedTargetIds, destinationDir);

        return ResponseEntity.ok("Files downloaded successfully.");
    } catch (Exception e) {
        e.printStackTrace();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error during file download: " + e.getMessage());
    }
}
