package com.ms.datalink.globalDatalink.controller;

import com.ms.datalink.globalDatalink.service.DocumentSubmissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/frontend")
public class DocumentSubmissionController {

    @Autowired
    private DocumentSubmissionService documentSubmissionService;

    @PostMapping("/submitDocument")
    public ResponseEntity<?> submitDocument(@RequestBody String folderName) {
        try {
            documentSubmissionService.processSubmission(folderName);
            return ResponseEntity.ok("Submission processed successfully");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error processing submission: " + e.getMessage());
        }
    }
}




package com.ms.datalink.globalDatalink.service;

import com.ms.datalink.globalDatalink.model.MetadataSourceFile;
import com.ms.datalink.globalDatalink.model.RetrieveTargetResponse;
import com.ms.datalink.globalDatalink.model.SaveFileResponse;
import com.ms.datalink.globalDatalink.service.*;
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

    private final RestTemplate restTemplate = new RestTemplate();

    public void processSubmission(String folderName) throws IOException, CsvException, InterruptedException {
        // Step 1: Generate paths
        String metadataFilePath = incomingFilePath + folderName + "/metadata.csv";
        String sourceFilePath = incomingFilePath + folderName;

        // Step 2: Process metadata
        List<MetadataSourceFile> metadataList = metadataSourceFilesReader.processMetadata(metadataFilePath);

        // Step 3: Generate submission request
        String submissionRequestJson = submissionRequestGenerator.createSubmissionRequest(metadataList);
        Map<String, List<MetadataSourceFile>> batchFileMap = submissionRequestGenerator.getBatchFileMap();

        // Step 4: Make the POST request to create submission
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
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

        // Step 5: Upload files
        fileUploadandVerifyService.uploadAllFiles(submissionId, sourceFilePath);

        // Step 6: Save file response
        SaveFileResponse saveFileResponse = saveFileService.saveFile(submissionId);

        if (!saveFileResponse.getStartedSubmissionIds().contains(submissionId)) {
            throw new RuntimeException("Submission ID not found in started submissions. Aborting file transfer.");
        }

        // Step 7: Move folder from incoming to processing
        Path sourceFolderPath = Paths.get(incomingFilePath, folderName);
        Path targetFolderPath = Paths.get(processingFilePath, folderName);

        if (!Files.exists(targetFolderPath.getParent())) {
            Files.createDirectories(targetFolderPath.getParent());
        }

        Files.move(sourceFolderPath, targetFolderPath);

        log.info("Folder moved to: {}", targetFolderPath);

        // Step 8: Poll for completed targets
        List<RetrieveTargetResponse> completedTargets = pollingWithSubmissionId.getCompletedTargets(submissionId);

        // Step 9: Append to CSV
        appendToCsv.appendToCsv(metadataList, batchFileMap, completedTargets, submissionId);

        // Step 10: Get processed target IDs
        List<Integer> processedTargetIds = processedTargetId.getProcessedTargetIds(completedTargets);

        log.info("Processed Target IDs: {}", processedTargetIds);

        // Step 11: Download and save files
        downloadFiles.processAndDownloadFiles();
    }
}
