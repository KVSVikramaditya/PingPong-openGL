package com.ms.datalink.globalDatalink.service;

import com.ms.datalink.globalDatalink.model.*;
import com.opencsv.exceptions.CsvException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class DocumentSubmissionService {

    @Value("${sourcemetadata.incoming.file.directory}")
    private String incomingFilePath;

    @Value("${targetmetadata.processing.file.directory}")
    private String processingFilePath;

    @Value("${targetmetadata.outgoing.file.directory}")
    private String destinationDir;

    @Value("${backend.api.url}")
    private String backendApiUrl;

    @Autowired
    private MetadataSourceFilesReader metadataSourceFilesReader;

    @Autowired
    private SubmissionRequestGenerator submissionRequestGenerator;

    @Autowired
    private FileUploadandVerifyService fileUploadandVerifyService;

    @Autowired
    private SaveFileService saveFileService;

    @Autowired
    private PollingWithSubmissionId pollingWithSubmissionId;

    @Autowired
    private AppendToCsv appendToCsv;

    @Autowired
    private GetProcessedTargetIds processedTargetId;

    @Autowired
    private DownloadAndSaveFiles downloadFiles;

    @Autowired
    private AuthService authService;

    @Autowired
    private RestTemplate restTemplate;

    public void processSubmission(String folderName) throws IOException, CsvException, InterruptedException {
        // Step 1: Generate paths
        String metadataFilePath = incomingFilePath + folderName + "/metadata.csv";
        String sourceFilePath = incomingFilePath + folderName;

        // Step 2: Process metadata
        List<MetadataSourceFile> metadataList = metadataSourceFilesReader.processMetadata(metadataFilePath);

        // Step 3: Generate submission request
        String submissionRequestJson = submissionRequestGenerator.createSubmissionRequest(metadataList);
        Map<String, List<MetadataSourceFile>> batchFileMap = submissionRequestGenerator.getBatchFileMap();

        // Step 4: Get JWT Token
        String jwtToken = authService.getToken();

        // Step 5: Make the POST request to create submission
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(jwtToken); // Attach JWT Token

        HttpEntity<String> requestEntity = new HttpEntity<>(submissionRequestJson, headers);

        ResponseEntity<CreateSubmissionResponse> response = restTemplate.exchange(
                backendApiUrl + "/rest/v0/submissions/create",
                HttpMethod.POST, requestEntity, CreateSubmissionResponse.class);

        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            log.info("Submission created: {}", response.getBody());
        } else {
            throw new RuntimeException("Failed to create submission");
        }

        int submissionId = response.getBody().getSubmissionId();

        // Step 6: Upload files
        fileUploadandVerifyService.uploadAllFiles(submissionId, sourceFilePath);

        // Step 7: Save file response
        SaveFileResponse saveFileResponse = saveFileService.saveFile(submissionId);

        if (!saveFileResponse.getStartedSubmissionIds().contains(submissionId)) {
            throw new RuntimeException("Submission ID not found in started submissions. Aborting file transfer.");
        }

        // Step 8: Move folder from incoming to processing
        Path sourceFolderPath = Paths.get(incomingFilePath, folderName);
        Path targetFolderPath = Paths.get(processingFilePath, folderName);

        if (!Files.exists(targetFolderPath.getParent())) {
            Files.createDirectories(targetFolderPath.getParent());
        }

        Files.move(sourceFolderPath, targetFolderPath);

        log.info("Folder moved to: {}", targetFolderPath);

        // Step 9: Poll for completed targets
        List<RetrieveTargetResponse> completedTargets = pollingWithSubmissionId.getCompletedTargets(submissionId);

        // Step 10: Append to CSV
        appendToCsv.appendToCsv(metadataList, batchFileMap, completedTargets, submissionId);

        // Step 11: Get processed target IDs
        List<Integer> processedTargetIds = processedTargetId.getProcessedTargetIds(completedTargets);

        log.info("Processed Target IDs: {}", processedTargetIds);

        // Step 12: Download and save files
        downloadFiles.processAndDownloadFiles();
    }
}
