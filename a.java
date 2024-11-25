@Service
public class SubmissionRequestGenerator {
    // This map will hold the association between batch names and the files associated with each batch
    private final Map<String, List<MetadataSourceFile>> batchFileMap = new HashMap<>();

    public String createSubmissionRequest(List<MetadataSourceFile> metadataList) {
        try {
            // Initialize ObjectMapper for creating JSON
            ObjectMapper mapper = new ObjectMapper();

            // Create the root object for submission request
            ObjectNode submissionRequest = mapper.createObjectNode();
            submissionRequest.put("name", "Test Submission With Tech Tracking 1");
            submissionRequest.put("dueDate", 1767178799000L); // Replace with appropriate due date
            submissionRequest.put("projectId", 136); // Replace with your project ID
            submissionRequest.put("sourceLanguage", metadataList.get(0).getSourceLanguage());
            submissionRequest.put("instructions", "The instructions contain general indications for linguists.");
            submissionRequest.put("background", "The background contains additional context information.");

            // TechTracking section
            ObjectNode techTracking = mapper.createObjectNode();
            techTracking.put("adaptorName", "MYADPTR");
            techTracking.put("adaptorVersion", "1.0.0");
            techTracking.put("clientVersion", "ClientCMS 2.0");
            techTracking.put("technologyProduct", "GL_PD");
            submissionRequest.set("techTracking", techTracking);

            // Create batchInfos array
            ArrayNode batchInfos = mapper.createArrayNode();
            int batchCounter = 1;

            for (MetadataSourceFile file : metadataList) {
                for (String targetLanguage : file.getTargetLanguage().split(",")) {
                    // Generate a unique batch for each target language
                    ObjectNode batchInfo = mapper.createObjectNode();
                    String batchName = "Batch" + batchCounter++;
                    batchInfo.put("workflowId", 2); // Replace with appropriate workflow ID
                    batchInfo.put("targetFormat", "TXLF"); // Replace with appropriate target format
                    batchInfo.put("name", batchName);

                    // Add the file to batchFileMap under the batch name
                    batchFileMap.putIfAbsent(batchName, new ArrayList<>());
                    batchFileMap.get(batchName).add(file);

                    // Create targetLanguageInfos array with a single target language
                    ArrayNode targetLanguageInfos = mapper.createArrayNode();
                    ObjectNode targetLanguageInfo = mapper.createObjectNode();
                    targetLanguageInfo.put("targetLanguage", targetLanguage.trim());
                    targetLanguageInfos.add(targetLanguageInfo);

                    batchInfo.set("targetLanguageInfos", targetLanguageInfos);
                    batchInfos.add(batchInfo);
                }
            }

            submissionRequest.set("batchInfos", batchInfos);
            submissionRequest.put("claimScope", "LANGUAGE");

            // Convert submissionRequest to JSON string
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(submissionRequest);

        } catch (Exception e) {
            throw new RuntimeException("Error creating submission request", e);
        }
    }

    // Method to get the batchFileMap for further usage like uploading files
    public Map<String, List<MetadataSourceFile>> getBatchFileMap() {
        return batchFileMap;
    }
}
