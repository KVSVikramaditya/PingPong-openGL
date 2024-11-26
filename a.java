package com.ms.datalink.globalDatalink.service;

import com.ms.datalink.globalDatalink.model.MetadataSourceFile;
import com.ms.datalink.globalDatalink.model.RetrieveTargetResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.io.FileWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import com.opencsv.CSVWriter;

@Component
@Slf4j
@RequiredArgsConstructor
public class AppendToCsv {

    @Value("${targetmetadata.outgoing.file.directory}")
    private String destinationDir;

    public void appendToCsv(List<MetadataSourceFile> metadataList, Map<String, List<MetadataSourceFile>> batchFileMap,
                            List<RetrieveTargetResponse> completedTargets, int submissionId) {
        String csvFilePath = destinationDir + "/table.csv"; // Adjust the path as needed
        Path filePath = Paths.get(csvFilePath);

        try (Writer writer = Files.exists(filePath) ?
                new FileWriter(csvFilePath, true) : new FileWriter(csvFilePath);
             CSVWriter csvWriter = new CSVWriter(writer)) {

            // Write header if the file doesn't exist
            if (!Files.exists(filePath)) {
                String[] header = {"Sourcefilename", "Filetype", "TargetfileName", "Source_content_id", "Source_language",
                        "Target_language", "Jobid", "SubmissionId", "Batchname", "DocumentName", "Targetlanguage",
                        "Documentid", "Targetid", "Status"};
                csvWriter.writeNext(header);
            }

            for (RetrieveTargetResponse target : completedTargets) {
                // Determine batch name
                String batchName = batchFileMap.entrySet()
                        .stream()
                        .filter(entry -> entry.getValue().stream()
                                .anyMatch(metadata -> metadata.getFileName().equals(target.getDocumentName())))
                        .findFirst()
                        .map(Map.Entry::getKey)
                        .orElse("Unknown Batch");

                // Construct the row
                String targetFileName = target.getDocumentName().split("\\.")[0]
                        + "_" + target.getTargetLanguage().substring(0, 2);

                String[] row = {
                        target.getDocumentName(),                   // Sourcefilename
                        target.getFileFormatName(),                 // Filetype
                        targetFileName,                             // TargetfileName
                        target.getDocumentId(),                     // Source_content_id
                        target.getSourceLanguage(),                 // Source_language
                        target.getTargetLanguage(),                 // Target_language
                        target.getJobId(),                          // Jobid
                        String.valueOf(submissionId),               // SubmissionId
                        batchName,                                  // Batchname
                        target.getDocumentName(),                   // DocumentName
                        target.getTargetLanguage(),                 // Targetlanguage
                        target.getDocumentId(),                     // Documentid
                        target.getTargetId(),                       // Targetid
                        target.getStatus()                          // Status
                };

                // Append row to CSV
                csvWriter.writeNext(row);
            }

            log.info("CSV file updated successfully: {}", csvFilePath);

        } catch (Exception e) {
            log.error("Error while appending to CSV file: {}", csvFilePath, e);
        }
    }
}
