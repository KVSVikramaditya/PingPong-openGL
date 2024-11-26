package com.ms.datalink.globalDatalink.service;

import com.ms.datalink.globalDatalink.model.RetrieveTargetResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;

@Component
@Slf4j
@RequiredArgsConstructor
public class DownloadAndSaveFiles {

    @Autowired
    private AuthService authService;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${targetmetadata.outgoing.file.directory}")
    private String destinationDir;

    @Value("${backend.api.url}")
    private String backendApiUrl;

    public void processAndDownloadFiles() throws IOException {
        String csvFilePath = destinationDir + "/table.csv";
        Path filePath = Paths.get(csvFilePath);

        if (!Files.exists(filePath)) {
            log.error("File not found: {}", csvFilePath);
            throw new FileNotFoundException("File not found: " + csvFilePath);
        }

        // Read the CSV file
        List<String[]> allRows;
        try (Reader reader = Files.newBufferedReader(filePath);
             CSVReader csvReader = new CSVReader(reader)) {
            allRows = csvReader.readAll();
        }

        if (allRows.isEmpty()) {
            log.warn("No data found in the file: {}", csvFilePath);
            return;
        }

        // Extract header and data rows
        String[] header = allRows.get(0);
        List<String[]> dataRows = allRows.subList(1, allRows.size());

        // Group rows by submissionId and collect target IDs
        Map<Integer, List<Integer>> submissionTargetMap = new HashMap<>();
        List<RetrieveTargetResponse> completedTargets = new ArrayList<>();
        for (String[] row : dataRows) {
            String status = row[13]; // Assuming 'Status' is the 14th column (index 13)
            if ("PROCESSED".equalsIgnoreCase(status)) {
                int submissionId = Integer.parseInt(row[7]); // SubmissionId column
                int targetId = Integer.parseInt(row[12]); // TargetId column
                submissionTargetMap.computeIfAbsent(submissionId, k -> new ArrayList<>()).add(targetId);

                RetrieveTargetResponse targetResponse = new RetrieveTargetResponse();
                targetResponse.setTargetId(row[12]);
                targetResponse.setDocumentName(row[0]); // Sourcefilename
                targetResponse.setTargetLanguage(row[5]); // Target_language
                targetResponse.setSourceLanguage(row[4]); // Source_language
                targetResponse.setStatus("DOWNLOADED");
                completedTargets.add(targetResponse);
            }
        }

        if (submissionTargetMap.isEmpty()) {
            log.info("No PROCESSED rows found in the file.");
            return;
        }

        // Process each submissionId and its target IDs
        for (Map.Entry<Integer, List<Integer>> entry : submissionTargetMap.entrySet()) {
            int submissionId = entry.getKey();
            List<Integer> targetIds = entry.getValue();

            // Create a timestamped folder for saving the downloaded files
            String timestamp = String.valueOf(System.currentTimeMillis());
            Path folderPath = Paths.get(destinationDir, "submission_" + submissionId + "_" + timestamp);
            Files.createDirectories(folderPath);
            log.info("Files will be saved in: {}", folderPath);

            // Download files for the current submissionId and target IDs
            for (RetrieveTargetResponse target : completedTargets) {
                if (!targetIds.contains(Integer.parseInt(target.getTargetId()))) {
                    continue;
                }

                String targetLanguageCode = target.getTargetLanguage().substring(0, 2);
                String originalFileName = target.getDocumentName();
                String baseFileName = originalFileName.contains(".")
                        ? originalFileName.substring(0, originalFileName.lastIndexOf("."))
                        : originalFileName;
                String fileExtension = originalFileName.contains(".")
                        ? originalFileName.substring(originalFileName.lastIndexOf("."))
                        : "";
                String targetFileName = baseFileName + "_" + targetLanguageCode + fileExtension;

                String url = String.format("%s/rest/v0/submissions/%d/targets/%s/download/deliverable",
                        backendApiUrl, submissionId, target.getTargetId());
                log.info("Downloading from URL: {}", url);

                try {
                    HttpHeaders headers = new HttpHeaders();
                    headers.setContentType(MediaType.APPLICATION_JSON);
                    headers.setBearerAuth(authService.getToken());
                    ResponseEntity<byte[]> response = restTemplate.exchange(url, HttpMethod.GET,
                            new HttpEntity<>(headers), byte[].class);

                    if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                        Path filePathToSave = folderPath.resolve(targetFileName);
                        Files.write(filePathToSave, response.getBody());
                        log.info("File saved: {}", filePathToSave);
                    } else {
                        log.warn("Failed to download file for targetId: {}. HTTP Status: {}",
                                target.getTargetId(), response.getStatusCode());
                    }
                } catch (Exception e) {
                    log.error("Error while downloading file for targetId: {}", target.getTargetId(), e);
                }
            }
        }

        // Update statuses in the CSV file
        for (String[] row : dataRows) {
            String targetId = row[12]; // TargetId column
            if (completedTargets.stream()
                    .anyMatch(target -> target.getTargetId().equals(targetId))) {
                row[13] = "DOWNLOADED"; // Update status to DOWNLOADED
            }
        }

        // Write updated data back to the CSV file
        try (Writer writer = Files.newBufferedWriter(filePath);
             CSVWriter csvWriter = new CSVWriter(writer)) {
            csvWriter.writeNext(header);
            csvWriter.writeAll(dataRows);
        }

        log.info("CSV file updated successfully: {}", csvFilePath);
    }
}
