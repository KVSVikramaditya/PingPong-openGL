package com.ms.datalink.globalDatalink.controller;

import com.ms.datalink.globalDatalink.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/frontend")
@Tag(name = "Document Submission API", description = "API for processing document submissions and downloading files.")
public class DocumentSubmissionController {

    @Autowired
    private DocumentSubmissionService documentSubmissionService;

    @Autowired
    private DownLoadAndSaveFiles downloadFiles;

    @Operation(summary = "Submit a document for processing", description = "This endpoint processes a document by accepting a folder name.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Submission processed successfully"),
            @ApiResponse(responseCode = "500", description = "Error processing submission")
    })
    @PostMapping("/submitDocument")
    public ResponseEntity<?> submitDocument(@RequestBody String folderName) {
        try {
            documentSubmissionService.processSubmission(folderName);
            return ResponseEntity.ok("Submission processed successfully");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error processing submission: " + e.getMessage());
        }
    }

    @Operation(summary = "Download files and update their status", description = "Downloads files based on their status and updates the table.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Files downloaded successfully"),
            @ApiResponse(responseCode = "500", description = "Error processing the request")
    })
    @GetMapping("/downloadFiles")
    public ResponseEntity<?> downloadFilesAndUpdateStatus() {
        try {
            downloadFiles.processAndDownloadFiles();
            return ResponseEntity.ok("Files Downloaded");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error processing the request: " + e.getMessage());
        }
    }
}



package com.ms.datalink.globalDatalink.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Document Submission API")
                        .version("1.0")
                        .description("API documentation for Document Submission and File Download services.")
                        .contact(new Contact()
                                .name("Support Team")
                                .email("support@datalink.com")
                                .url("http://datalink.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("http://springdoc.org")));
    }
}



import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Request model for submitting a document.")
public class DocumentRequest {
    @Schema(description = "Name of the folder to process.", example = "documents/folder1")
    private String folderName;
}
