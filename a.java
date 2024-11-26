@GetMapping("/downloadFiles")
public ResponseEntity<?> downloadFilesAndUpdateStatus() {
    try {
        String result = downloadFiles.processAndDownloadFiles();
        return ResponseEntity.ok(result);
    } catch (Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error processing the request: " + e.getMessage());
    }
}


package com.ms.datalink.globalDatalink.service;

import com.ms.datalink.globalDatalink.model.RetrieveTargetResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;

import java.io.*;
import java.nio.file.*;
import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class DownloadAndSaveFiles {

    @Value("${targetmetadata.outgoing.file.directory}")
    private String destinationDir;

    private final String backendApiUrl = "https://backend-api.example.com"; // Replace with actual URL

    public String processAndDownloadFiles() throws Exception {
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
            return "No data found in the file.";
        }

        // Extract header and data rows
        String[] header = allRows.get(0);
        List<String[]> dataRows = allRows.subList(1, allRows.size());

        // Identify PROCESSED rows and collect Target IDs
        List<Integer> processedTargetIds = new ArrayList<>();
        List<RetrieveTargetResponse> completedTargets = new ArrayList<>();
        for (String[] row : dataRows) {
            String status = row[13]; // Status column
            if ("PROCESSED".equalsIgnoreCase(status)) {
                // Collect target ID and mock RetrieveTargetResponse
                int targetId = Integer.parseInt(row[12]); // Targetid column
                processedTargetIds.add(targetId);

                RetrieveTargetResponse targetResponse = new RetrieveTargetResponse();
                targetResponse.setTargetId(row[12]);
                targetResponse.setDocumentName(row[0]); // Sourcefilename
                targetResponse.setTargetLanguage(row[5]); // Target_language
                targetResponse.setStatus("DOWNLOADED"); // Update status for processed targets
                completedTargets.add(targetResponse);
            }
        }

        if (processedTargetIds.isEmpty()) {
            log.info("No PROCESSED rows found in the file.");
            return "No PROCESSED rows found.";
        }

        // Download files for PROCESSED target IDs
        downloadAndSaveFiles(backendApiUrl, processedTargetIds, completedTargets);

        // Update statuses in the CSV file
        for (String[] row : dataRows) {
            String targetId = row[12]; // Targetid column
            if (processedTargetIds.contains(Integer.parseInt(targetId))) {
                row[13] = "DOWNLOADED"; // Update status to DOWNLOADED
            }
        }

        // Write updated data back to the CSV file
        try (Writer writer = Files.newBufferedWriter(filePath);
             CSVWriter csvWriter = new CSVWriter(writer)) {
            csvWriter.writeNext(header);
            csvWriter.writeAll(dataRows);
        }

        log.info("Files downloaded and CSV file updated successfully.");
        return "Files downloaded and statuses updated successfully.";
    }

    public void downloadAndSaveFiles(String baseUrl, List<Integer> targetIds,
                                     List<RetrieveTargetResponse> completedTargets) {
        for (RetrieveTargetResponse target : completedTargets) {
            String targetLanguageCode = target.getTargetLanguage().substring(0, 2);
            String fileName = target.getDocumentName().split("\\.")[0] + "_" + targetLanguageCode + ".file"; // Replace 'file' with actual extension

            try {
                // Construct URL for download
                String url = String.format("%s/targets/%s/download", baseUrl, target.getTargetId());

                // Simulate downloading and saving the file
                // Replace with actual download logic using RestTemplate or HTTP client
                Path downloadPath = Paths.get(destinationDir, fileName);
                Files.createDirectories(downloadPath.getParent());
                Files.write(downloadPath, new byte[0]); // Placeholder for downloaded file content

                log.info("File downloaded and saved: {}", downloadPath);

            } catch (Exception e) {
                log.error("Error downloading file for targetId: {}", target.getTargetId(), e);
            }
        }
    }
}
