package com.ms.datalink.globalDatalink.service;

import com.ms.datalink.globalDatalink.model.RetrieveTargetResponse;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

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

    @Value("${metadata.table.file.directory}")
    private String destinationDir1;

    @Value("${backend.api.url}")
    private String backendApiUrl;

    public void processAndDownloadFiles() throws IOException {
        String csvFilePath = destinationDir1 + "/table.csv";
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

        // Set to track processed submission IDs to avoid duplicates
        Set<Integer> processedSubmissionIds = new HashSet<>();

        // Poll READY or PROCESSING stages
        List<String[]> readyOrProcessingRows = dataRows.stream()
                .filter(row -> ("READY".equalsIgnoreCase(row[13]) || "PROCESSING".equalsIgnoreCase(row[13]))
                        && !processedSubmissionIds.contains(Integer.parseInt(row[7]))) // Skip already processed submissions
                .collect(Collectors.toList());

        for (String[] row : readyOrProcessingRows) {
            int submissionId = Integer.parseInt(row[7]);
            processedSubmissionIds.add(submissionId); // Add to processed set

            String targetId = row[12];

            // Poll the endpoint to check the latest status
            String pollUrl = String.format("%s/rest/v0/submissions/%d/targets/%s/status", backendApiUrl, submissionId, targetId);
            try {
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.setBearerAuth(authService.getToken());
                ResponseEntity<Map> response = restTemplate.exchange(pollUrl, HttpMethod.GET, new HttpEntity<>(headers), Map.class);

                if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                    String latestStatus = (String) response.getBody().get("status");
                    row[13] = latestStatus; // Update status in the table
                }
            } catch (Exception e) {
                log.error("Error polling target status for Target ID: {}", targetId, e);
            }
        }

        // Write updated statuses back to the CSV file
        try (Writer writer = Files.newBufferedWriter(filePath);
             CSVWriter csvWriter = new CSVWriter(writer)) {
            csvWriter.writeNext(header);
            csvWriter.writeAll(dataRows);
        }
        log.info("CSV file updated with latest statuses: {}", csvFilePath);

        // Process PROCESSED status
        List<String[]> processedRows = dataRows.stream()
                .filter(row -> "PROCESSED".equalsIgnoreCase(row[13]))
                .collect(Collectors.toList());

        // Group rows by submissionId
        Map<Integer, List<String[]>> submissionTargetMap = new HashMap<>();
        for (String[] row : processedRows) {
            int submissionId = Integer.parseInt(row[7]); // SubmissionId column
            submissionTargetMap.computeIfAbsent(submissionId, k -> new ArrayList<>()).add(row);
        }

        // Process each submissionId and its target IDs
        for (Map.Entry<Integer, List<String[]>> entry : submissionTargetMap.entrySet()) {
            int submissionId = entry.getKey();
            List<String[]> targetRows = entry.getValue();

            // Skip duplicate processing
            if (!processedSubmissionIds.add(submissionId)) {
                log.info("Skipping already processed submissionId: {}", submissionId);
                continue;
            }

            // Create a timestamped folder for saving the downloaded files
            String timestamp = String.valueOf(System.currentTimeMillis());
            Path folderPath = Paths.get(destinationDir, "submission_" + submissionId + "_" + timestamp);
            Files.createDirectories(folderPath);
            log.info("Files will be saved in: {}", folderPath);

            List<String[]> metadataEntries = new ArrayList<>();
            metadataEntries.add(new String[]{"Sourcefilename", "TargetfileName", "SubmissionId", "TargetID",
                    "TargetLanguage", "SourceLanguage", "SourceAccountId", "Status"}); // Metadata header

            for (String[] row : targetRows) {
                String targetId = row[12]; // TargetId
                String sourceAccountId = row[3]; // SourceAccountId

                String targetLanguageCode = row[5].substring(0, 2); // Target_language
                String originalFileName = row[0]; // Sourcefilename
                String baseFileName = originalFileName.contains(".")
                        ? originalFileName.substring(0, originalFileName.lastIndexOf("."))
                        : originalFileName;
                String fileExtension = originalFileName.contains(".")
                        ? originalFileName.substring(originalFileName.lastIndexOf("."))
                        : "";
                String targetFileName = baseFileName + "_" + targetLanguageCode + fileExtension;

                String url = String.format("%s/rest/v0/submissions/%d/targets/%s/download/deliverable",
                        backendApiUrl, submissionId, targetId);
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

                        // Add metadata entry for the file
                        metadataEntries.add(new String[]{
                                originalFileName,       // Sourcefilename
                                targetFileName,         // TargetfileName
                                String.valueOf(submissionId), // SubmissionId
                                targetId,               // TargetID
                                row[5],                 // TargetLanguage
                                row[4],                 // SourceLanguage
                                sourceAccountId,        // SourceAccountId
                                "DOWNLOADED"            // Status
                        });

                        // Update the row status
                        row[13] = "DOWNLOADED";
                    } else {
                        log.warn("Failed to download file for targetId: {}. HTTP Status: {}", targetId, response.getStatusCode());
                    }
                } catch (Exception e) {
                    log.error("Error while downloading file for targetId: {}", targetId, e);
                }
            }

            // Save metadata.csv in the same folder
            saveMetadataCsv(folderPath.resolve("metadata.csv"), metadataEntries);
        }

        // Write updated data back to the CSV file
        try (Writer writer = Files.newBufferedWriter(filePath);
             CSVWriter csvWriter = new CSVWriter(writer)) {
            csvWriter.writeNext(header);
            csvWriter.writeAll(dataRows);
        }
        log.info("CSV file updated successfully: {}", csvFilePath);
    }

    private void saveMetadataCsv(Path csvFilePath, List<String[]> metadataEntries) {
        try (Writer writer = Files.newBufferedWriter(csvFilePath);
             CSVWriter csvWriter = new CSVWriter(writer)) {
            csvWriter.writeAll(metadataEntries);
            log.info("Metadata saved as CSV: {}", csvFilePath);
        } catch (IOException e) {
            log.error("Error while saving metadata.csv: {}", csvFilePath, e);
        }
    }
}
