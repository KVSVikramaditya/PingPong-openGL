@PostMapping("/submitDocument")
public ResponseEntity submitDocument(@RequestBody String folderName) throws Exception {
    this.folderName = folderName;
    String jwtToken = AuthService.getToken();
    String metadataFilePath = incomingFilePath + folderName + "/metadata.csv";
    String sourceFilePath = incomingFilePath + folderName;
    List<MetadataSourceFile> metadataList = metadataSourceFilesReader.processMetadata(metadataFilePath);
    
    String submissionRequestJson = submissionRequestGenerator.createSubmissionRequest(metadataList);
    Map<String, List<MetadataSourceFile>> batchFileMap = submissionRequestGenerator.getBatchFileMap();
    System.out.println("Generated request body: " + submissionRequestJson);
    System.out.println("Batch File Map: " + batchFileMap);

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.setBearerAuth(jwtToken);
    HttpEntity<String> requestEntity = new HttpEntity<>(submissionRequestJson, headers);

    // Make the POST request to the backend API
    ResponseEntity<CreateSubmissionResponse> response = restTemplate.exchange(
            backendApiUrl + "/rest/v0/submissions/create",
            HttpMethod.POST,
            requestEntity,
            CreateSubmissionResponse.class
    );

    if (response.getStatusCode() == HttpStatus.OK) {
        System.out.println(response);
        submissionId = response.getBody().getSubmissionId();

        try {
            List<UploadResourceResponse> uploadResponses = fileUploadandVerifyService.uploadAllFiles(submissionId, sourceFilePath);
            System.out.println(uploadResponses);

            SaveFileResponse saveFileResponse = saveFileService.saveFile(submissionId);
            Path sourceFolderPath = Paths.get(incomingFilePath, this.folderName);

            if (!saveFileResponse.getStartedSubmissionIds().contains(submissionId)) {
                System.out.println("Submission ID not found in started submissions. Aborting file transfer.");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Submission ID not found in started submissions. File transfer aborted.");
            }

            Thread.sleep(10000);

            List<RetrieveTargetResponse> completedTargets = pollingWithSubmissionId.getCompletedTargets(submissionId);
            System.out.println(completedTargets);

            List<Integer> processedTargetIds = processedTargetId.getProcessedTargetIds(completedTargets);
            System.out.println("Processed Target IDs: " + processedTargetIds);

            downloadFiles.downloadAndSaveFiles(backendApiUrl, submissionId, processedTargetIds, completedTargets, destinationDir);

            // Append data to the existing Excel file
            appendToExcel(metadataList, batchFileMap, completedTargets, submissionId);

            return ResponseEntity.ok(uploadResponses);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to submit and upload files: " + e.getMessage());
        }
    }

    return new ResponseEntity(HttpStatus.BAD_GATEWAY);
}

private void appendToExcel(List<MetadataSourceFile> metadataList, Map<String, List<MetadataSourceFile>> batchFileMap, 
                           List<RetrieveTargetResponse> completedTargets, int submissionId) {
    String excelFilePath = destinationDir + "/table.xlsx"; // Adjust the path as needed
    try (FileInputStream fis = new FileInputStream(excelFilePath);
         Workbook workbook = new XSSFWorkbook(fis)) {

        Sheet sheet = workbook.getSheetAt(0); // Assuming data is in the first sheet
        int rowCount = sheet.getLastRowNum() + 1;

        for (RetrieveTargetResponse target : completedTargets) {
            Row row = sheet.createRow(rowCount++);
            String batchName = batchFileMap.entrySet()
                                           .stream()
                                           .filter(entry -> entry.getValue().contains(metadataList.get(0)))
                                           .findFirst()
                                           .map(Map.Entry::getKey)
                                           .orElse("Unknown Batch");

            // Populate the row
            row.createCell(0).setCellValue(target.getDocumentName()); // Sourcefilename
            row.createCell(1).setCellValue(target.getFileFormatName()); // Filetype
            row.createCell(2).setCellValue(target.getDocumentName().split("\\.")[0] + "_" + target.getTargetLanguage().substring(0, 2)); // TargetfileName
            row.createCell(3).setCellValue(target.getDocumentId()); // Source_content_id
            row.createCell(4).setCellValue(target.getSourceLanguage()); // Source_language
            row.createCell(5).setCellValue(target.getTargetLanguage()); // Target_language
            row.createCell(6).setCellValue(target.getJobId()); // Jobid
            row.createCell(7).setCellValue(submissionId); // SubmissionId
            row.createCell(8).setCellValue(batchName); // Batchname
            row.createCell(9).setCellValue(target.getDocumentName()); // DocumentName
            row.createCell(10).setCellValue(target.getTargetLanguage()); // Targetlanguage
            row.createCell(11).setCellValue(target.getDocumentId()); // Documentid
            row.createCell(12).setCellValue(target.getTargetId()); // Targetid
            row.createCell(13).setCellValue(target.getStatus()); // Status
        }

        // Write back to the file
        try (FileOutputStream fos = new FileOutputStream(excelFilePath)) {
            workbook.write(fos);
        }
        System.out.println("Excel file updated: " + excelFilePath);

    } catch (Exception e) {
        System.out.println("Error updating Excel file");
        e.printStackTrace();
    }
}
