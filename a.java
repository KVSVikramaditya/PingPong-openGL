dependencies {
    implementation 'org.springdoc:springdoc-openapi-ui:1.7.0' // Use the latest version
    implementation 'org.springdoc:springdoc-openapi-data-rest:1.7.0'
    implementation 'org.springdoc:springdoc-openapi-security:1.7.0' // If using security
}



package com.translations.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Translations API")
                        .version("1.0")
                        .description("API documentation for the Translations Project")
                        .contact(new Contact()
                                .name("Support Team")
                                .email("support@translations.com")
                                .url("http://translations.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("http://springdoc.org")));
    }
}




package com.translations.controller;

import com.translations.model.TranslationRequest;
import com.translations.model.TranslationResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/translations")
@Tag(name = "Translations", description = "Manage translations and related resources")
public class TranslationController {

    @Operation(summary = "Submit a new translation request", description = "Creates a new translation job")
    @PostMapping
    public ResponseEntity<TranslationResponse> submitTranslation(@RequestBody TranslationRequest request) {
        // Logic to handle the translation request
        return ResponseEntity.ok(new TranslationResponse());
    }

    @Operation(summary = "Get translation status", description = "Fetches the status of an existing translation job")
    @GetMapping("/{id}")
    public ResponseEntity<TranslationResponse> getTranslationStatus(@PathVariable String id) {
        // Logic to fetch the translation status
        return ResponseEntity.ok(new TranslationResponse());
    }
}





springdoc.api-docs.enabled=true
springdoc.swagger-ui.enabled=true
springdoc.swagger-ui.path=/swagger-ui.html
springdoc.api-docs.path=/v3/api-docs




    package com.translations.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Request model for translation submissions")
public class TranslationRequest {
    @Schema(description = "Source language of the text", example = "en")
    private String sourceLanguage;

    @Schema(description = "Target language for the translation", example = "fr")
    private String targetLanguage;

    @Schema(description = "Text to be translated", example = "Hello, world!")
    private String text;
}

@Data
@Schema(description = "Response model for translation status")
public class TranslationResponse {
    @Schema(description = "Unique ID of the translation job", example = "12345")
    private String jobId;

    @Schema(description = "Current status of the translation job", example = "COMPLETED")
    private String status;

    @Schema(description = "Translated text", example = "Bonjour, le monde!")
    private String translatedText;
}
