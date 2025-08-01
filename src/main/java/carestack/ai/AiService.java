package carestack.ai;

import carestack.ai.dto.ExtractionRequestDTO;
import carestack.ai.dto.FhirBundleDTO;
import carestack.ai.dto.ProcessDSDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import carestack.base.Base;
import carestack.base.enums.CaseType;
import carestack.base.errors.ValidationError;
import carestack.base.utils.Constants;
import carestack.base.utils.LogUtil;

import java.util.Map;
import java.util.Set;

/**
 * <h3>AI Service for Healthcare Document Processing</h3>
 *
 * This service provides a suite of AI-powered functionalities for processing and analyzing healthcare documents.
 * It acts as a client to an external AI API, handling tasks such as data encryption, document summarization,
 * entity extraction, and FHIR bundle generation.
 *
 * <h3>Core Responsibilities:</h3>
 * <ul>
 *   <li><b>Document Summarization:</b> Generates concise summaries from clinical documents like Discharge Summaries or OP Consultations.</li>
 *   <li><b>Entity Extraction:</b> Identifies and extracts structured data (e.g., patient demographics, diagnoses, medications) from an unstructured text.</li>
 *   <li><b>FHIR Conversion:</b> Transforms structured clinical data into HL7 FHIR-compliant bundles.</li>
 *   <li><b>Security:</b> Encrypts sensitive data before transmission to ensure patient privacy and compliance.</li>
 * </ul>
 *
 * The service leverages Spring's reactive {@link WebClient} for non-blocking API communication and Jakarta Bean Validation
 * for robust input validation.
 */
@Service
public class AiService extends Base {

    @Autowired
    protected EncryptionUtilities encryptionUtilities;

    private final Validator validator;

    /**
     * Constructs the AiService.
     *
     * @param objectMapper Jackson ObjectMapper for serialization
     * @param webClient    Spring WebClient for HTTP calls
     * @param validator    Validator for DTO validation
     */
    public AiService(ObjectMapper objectMapper,
                     WebClient webClient,
                     Validator validator) {
        super(objectMapper, webClient);
        this.validator = validator;

    }


    /**
     * Validates the provided DTO using Bean Validation.
     *
     * @param dto  DTO to be validated
     * @param <T>  Generic type of the DTO
     * @throws IllegalArgumentException if validation fails
     */
    private <T> void validateAiData(T dto) {
        Set<ConstraintViolation<T>> violations = validator.validate(dto);
        if (!violations.isEmpty()) {
            StringBuilder errorMessage = new StringBuilder("Validation failed: ");
            violations.forEach(violation ->
                    errorMessage.append(violation.getPropertyPath())
                            .append(" ")
                            .append(violation.getMessage())
                            .append("; "));

            LogUtil.logger.error("Validation errors: {}", errorMessage);
            throw new IllegalArgumentException(errorMessage.toString());
        }
    }

    /**
     * <h3>Generate Discharge Summary</h3>
     * Processes one or more discharge summary documents from provided URLs, encrypts their content, and submits them to the AI service for summarization.
     *
     * <h3>Input and Validation:</h3>
     * The method accepts a {@link ProcessDSDto} object, which is automatically validated.
     * <ul>
     *     <li><b>files (List&lt;String&gt;):</b> A list of publicly accessible URLs pointing to the document files (e.g., PDFs, images). This is required if {@code encryptedData} is not provided.</li>
     *     <li><b>publicKey (String, Optional):</b> A public key in PEM format used by the API to encrypt the response.</li>
     *     <li><b>encryptedData (String, Optional):</b> Pre-encrypted data. If provided, the {@code files} list is ignored for encryption.</li>
     * </ul>
     *
     * <h3>Workflow:</h3>
     * <ol>
     *     <li>Validates the input {@link ProcessDSDto}.</li>
     *     <li>If the {@code files} list is provided, it encrypts the list of URLs.</li>
     *     <li>Constructs a payload, setting the {@code caseType} to {@code DISCHARGE_SUMMARY}.</li>
     *     <li>Makes a POST request to the summarization endpoint.</li>
     * </ol>
     *
     * <h3>Output:</h3>
     * @return A {@link Mono<String>} that emits the raw string response from the AI service. This response may contain a job ID for polling or the direct summary result.
     *
     * <h3>Error Handling:</h3>
     * Returns a {@link Mono#error(Throwable)} with a {@link RuntimeException} if input validation, encryption, or the API call fails.
     *
     * <h3>Example Usage:</h3>
     * <pre>{@code
     * ProcessDSDto requestDto = new ProcessDSDto();
     * requestDto.setFiles(Arrays.asList(
     *     "https://example-bucket.s3.amazonaws.com/discharge1.pdf",
     *     "https://example-bucket.s3.amazonaws.com/discharge2.pdf"
     * ));
     * requestDto.setPublicKey("-----BEGIN PUBLIC KEY-----...-----END PUBLIC KEY-----");
     *
     * aiService.generateDischargeSummary(requestDto)
     *     .subscribe(
     *         response -> System.out.println("API Response: " + response),
     *         error -> System.err.println("Error generating summary: " + error.getMessage())
     *     );
     * }</pre>
     *
     * <h3>Expected Output (Example):</h3>
     * <pre>{@code
     * // API Response: {"jobId":"a1b2c3d4-e5f6-7890-1234-fedcba987654","status":"IN_PROGRESS"}
     * }</pre>
     *
     * @param processDSDto The DTO containing the file URLs and optional encryption key.
     */
    public Mono<String> generateDischargeSummary(ProcessDSDto processDSDto) {
        return Mono.fromCallable(() -> {
                    try {
                        // Validate the input DTO
                        validateAiData(processDSDto);

                        // Encrypt the files if provided
                        String encryptedData = null;
                        if (processDSDto.getFiles() != null && !processDSDto.getFiles().isEmpty()) {
                            encryptedData = encryptionUtilities.encryption(processDSDto.getFiles());
                        }

                        // Create the payload
                        ProcessDSDto payload = new ProcessDSDto();
                        payload.setFiles(processDSDto.getFiles());
                        payload.setPublicKey(processDSDto.getPublicKey());
                        payload.setEncryptedData(encryptedData != null ? encryptedData : processDSDto.getEncryptedData());
                        payload.setCaseType(CaseType.DISCHARGE_SUMMARY);

                        return payload;

                    } catch (Exception e) {
                        LogUtil.logger.error("Error preparing discharge summary request: {}", e.getMessage(), e);
                        throw new RuntimeException("Error preparing discharge summary request", e);
                    }
                })
                .flatMap(payload -> {
                    try {
                        return post(Constants.GENERATE_CASE_SHEET_SUMMARY_URL, payload, new ParameterizedTypeReference<>() {});
                    } catch (Exception e) {
                        LogUtil.logger.error("Error in generating DischargeSummary: {}", e.getMessage(), e);
                        return Mono.error(new RuntimeException("Error in generating DischargeSummary", e));
                    }
                });
    }

    /**
     * <h3>Generate Outpatient (OP) Consultation Summary</h3>
     * Processes one or more OP consultation documents from provided URLs, encrypts their content, and submits them to the AI service for summarization.
     *
     * <h3>Input and Validation:</h3>
     * The method accepts a {@link ProcessDSDto} object, which is automatically validated.
     * <ul>
     *     <li><b>files (List&lt;String&gt;):</b> A list of publicly accessible URLs pointing to the document files. Required if {@code encryptedData} is not provided.</li>
     *     <li><b>publicKey (String, Optional):</b> A public key in PEM format used by the API to encrypt the response.</li>
     *     <li><b>encryptedData (String, Optional):</b> Pre-encrypted data. If provided, the {@code files} list is ignored for encryption.</li>
     * </ul>
     *
     * <h3>Workflow:</h3>
     * <ol>
     *     <li>Validates the input {@link ProcessDSDto}.</li>
     *     <li>If the {@code files} list is provided, it encrypts the list of URLs.</li>
     *     <li>Constructs a payload, setting the {@code caseType} to {@code OP_CONSULTATION}.</li>
     *     <li>Makes a POST request to the summarization endpoint.</li>
     * </ol>
     *
     * <h3>Output:</h3>
     * @return A {@link Mono<String>} that emits the raw string response from the AI service.
     *
     * <h3>Error Handling:</h3>
     * Returns a {@link Mono#error(Throwable)} with a {@link RuntimeException} if input validation, encryption, or the API call fails.
     *
     * <h3>Example Usage:</h3>
     * <pre>{@code
     * ProcessDSDto requestDto = new ProcessDSDto();
     * requestDto.setFiles(Arrays.asList("https://example-bucket.s3.amazonaws.com/op_consultation.pdf"));
     *
     * aiService.generateOpSummary(requestDto)
     *     .subscribe(
     *         response -> System.out.println("API Response: " + response),
     *         error -> System.err.println("Error generating OP summary: " + error.getMessage())
     *     );
     * }</pre>
     *
     * <h3>Expected Output (Example):</h3>
     * <pre>{@code
     * // API Response: {"jobId":"b2c3d4e5-f6a7-8901-2345-edcba9876543","status":"IN_PROGRESS"}
     * }</pre>
     *
     * @param processDSDto The DTO containing the file URLs and optional encryption key.
     */
    public Mono<String> generateOpSummary(ProcessDSDto processDSDto) {
        return Mono.fromCallable(() -> {
                    try {
                        validateAiData(processDSDto);

                        String encryptedData = null;
                        if (processDSDto.getFiles() != null && !processDSDto.getFiles().isEmpty()) {
                            encryptedData = encryptionUtilities.encryption(processDSDto.getFiles());
                        }

                        ProcessDSDto payload = new ProcessDSDto();
                        payload.setFiles(processDSDto.getFiles());
                        payload.setPublicKey(processDSDto.getPublicKey());
                        payload.setEncryptedData(encryptedData != null ? encryptedData : processDSDto.getEncryptedData());
                        payload.setCaseType(carestack.base.enums.CaseType.OP_CONSULTATION);

                        return payload;

                    } catch (Exception e) {
                        LogUtil.logger.error("Error preparing OP case data extraction request: {}", e.getMessage(), e);
                        throw new RuntimeException("Error preparing OP case data extraction request", e);
                    }
                })
                .flatMap(payload -> {
                    try {
                        return post(Constants.GENERATE_CASE_SHEET_SUMMARY_URL, payload, new ParameterizedTypeReference<String>() {});
                    } catch (Exception e) {
                        LogUtil.logger.error("Error in extracting text from OP case sheet: {}", e.getMessage(), e);
                        return Mono.error(new RuntimeException("Error in extracting text from OP case sheet", e));
                    }
                });
    }


    /**
     * <h3>Generate FHIR Bundle</h3>
     * Converts structured clinical data into a standard FHIR-compliant bundle.
     *
     * <h3>Input and Validation:</h3>
     * The method accepts a {@link FhirBundleDTO} object, which is automatically validated.
     * <ul>
     *     <li><b>extractedData (Map&lt;String, Object&gt;, Optional):</b> Raw, structured data (e.g., from a previous extraction step). If provided, this data will be encrypted by the service.</li>
     *     <li><b>encryptedData (String, Optional):</b> Pre-encrypted structured data. Use this if you are encrypting the data externally.</li>
     *     <li><b>caseType (CaseType):</b> The type of clinical case (e.g., {@code DISCHARGE_SUMMARY}). This is a required field.</li>
     *     <li><b>files (List&lt;String&gt;, Optional):</b> A list of encrypted file references relevant to the clinical context.</li>
     * </ul>
     *
     * <h3>Workflow:</h3>
     * <ol>
     *     <li>Validates the input {@link FhirBundleDTO}.</li>
     *     <li>If {@code extractedData} is present and {@code encryptedData} is not, it encrypts {@code extractedData} and sets it as the payload.</li>
     *     <li>Makes a POST request to the FHIR bundle generation endpoint.</li>
     * </ol>
     *
     * <h3>Output:</h3>
     * @return A {@link Mono<Map<String, Object>>} that emits a map representing the generated FHIR bundle in JSON format.
     *
     * <h3>Error Handling:</h3>
     * Returns a {@link Mono#error(Throwable)} with an {@link IllegalArgumentException} on validation failure or a {@link RuntimeException} on other processing errors.
     *
     * <h3>Example Usage:</h3>
     * <pre>{@code
     * FhirBundleDTO fhirRequest = new FhirBundleDTO();
     * fhirRequest.setCaseType(CaseType.DISCHARGE_SUMMARY);
     * fhirRequest.setExtractedData(Map.of(
     *     "patientName", "John Doe",
     *     "diagnosis", "Viral Fever",
     *     "admissionDate", "2023-10-26"
     * ));
     *
     * aiService.generateFhirBundle(fhirRequest)
     *     .subscribe(
     *         fhirBundle -> System.out.println("Generated FHIR Bundle: " + fhirBundle),
     *         error -> System.err.println("Error generating FHIR bundle: " + error.getMessage())
     *     );
     * }</pre>
     *
     * <h3>Expected Output (Example):</h3>
     * <pre>{@code
     * // Generated FHIR Bundle: { "resourceType": "Bundle", "id": "...", "entry": [ { "resource": { "resourceType": "Patient", "name": [ { "text": "John Doe" } ] } }, ... ] }
     * }</pre>
     *
     * @param fhirBundleDTO The DTO containing the clinical data to be converted.
     */
    public Mono<Map<String, Object>> generateFhirBundle(FhirBundleDTO fhirBundleDTO) {
        return Mono.fromCallable(() -> {
                    validateFhirBundleDTO(fhirBundleDTO);
                    encryptIfNeeded(fhirBundleDTO);
                    return fhirBundleDTO;
                })
                .doOnNext(dto -> LogUtil.logger.info("Sending request to FHIR bundle endpoint"))
                .flatMap(dto -> post(Constants.GENERATE_FHIR_BUNDLE_URL, dto, new ParameterizedTypeReference<Map<String, Object>>() {}))
                .doOnSuccess(response -> LogUtil.logger.info("FHIR bundle successfully generated"))
                .doOnError(error -> LogUtil.logger.error("Error generating FHIR bundle: {}", error.getMessage(), error));
    }

    /**
     * <h3>Extract Structured Entities from Text</h3>
     * Submits a block of text (as a JSON string) to the AI service to extract structured clinical entities.
     *
     * <h3>Input and Validation:</h3>
     * The method accepts a {@code Map<String, Object>} payload with the following keys:
     * <ul>
     *     <li><b>extractedData (Object):</b> The raw data to be processed. This object is serialized into a JSON string and sent as {@code inputText}. This field is <b>required</b>.</li>
     *     <li><b>caseType (String):</b> A string identifying the case type (e.g., "Discharge Summary", "RADIOLOGY_REPORT"). This field is <b>required</b>.</li>
     * </ul>
     *
     * <h3>Workflow:</h3>
     * <ol>
     *     <li>Validates the presence of {@code extractedData} and {@code caseType} in the payload.</li>
     *     <li>Serializes the {@code extractedData} object into a JSON string.</li>
     *     <li>Constructs an {@link ExtractionRequestDTO}.</li>
     *     <li>Makes a POST request to the entity extraction endpoint.</li>
     * </ol>
     *
     * <h3>Output:</h3>
     * @return A {@link Mono<Object>} that emits the extracted entities, typically as a Map or other structured object.
     *
     * <h3>Error Handling:</h3>
     * Returns a {@link Mono#error(Throwable)} with a {@link ValidationError} if required fields are missing, or a {@link RuntimeException} for other processing failures.
     *
     * <h3>Example Usage:</h3>
     * <pre>{@code
     * Map<String, Object> unstructuredData = Map.of(
     *     "notes", "Patient John Doe, 45Y M, complains of chest pain. Diagnosis: Unstable Angina."
     * );
     *
     * Map<String, Object> requestPayload = Map.of(
     *     "extractedData", unstructuredData,
     *     "caseType", "OP_CONSULTATION"
     * );
     *
     * aiService.extractEntity(requestPayload)
     *     .subscribe(
     *         entities -> System.out.println("Extracted Entities: " + entities),
     *         error -> System.err.println("Error extracting entities: " + error.getMessage())
     *     );
     * }</pre>
     *
     * <h3>Expected Output (Example):</h3>
     * <pre>{@code
     * // Extracted Entities: { "patient": { "name": "John Doe", "age": "45", "gender": "M" }, "symptoms": [ "chest pain" ], "diagnoses": [ "Unstable Angina" ] }
     * }</pre>
     *
     * @param payload The input map containing the data and case type.
     */

    Mono<Object> extractEntity(Map<String, Object> payload) {
        try {
            // 1. Extract `extractedData` and convert to JSON string
            Object extractedData = payload.get("extractedData");
            if (extractedData == null) {
                throw new ValidationError("Missing 'extractedData' field in payload");
            }

            String inputTextJson = objectMapper.writeValueAsString(extractedData);

            String caseTypeStr = (String) payload.get("caseType");
            if (caseTypeStr == null || caseTypeStr.isBlank()) {
                throw new ValidationError("Missing 'caseType' field in payload");
            }

            ExtractionRequestDTO request = new ExtractionRequestDTO(inputTextJson, caseTypeStr);

            return post(Constants.HEALTH_INFORMATION_EXTRACTION_URL, request, new ParameterizedTypeReference<>() {})
                    .doOnSuccess(response -> LogUtil.logger.info("Entity Extraction done successfully"))
                    .doOnError(error -> LogUtil.logger.error("Error extracting Entity: {}", error.getMessage(), error));

        } catch (Exception e) {
            LogUtil.logger.error("Error in extractEntity service: {}", e.getMessage(), e);
            return Mono.error(new RuntimeException("Failed to process extraction request", e));
        }
    }




    /**
     * Validates the FhirBundleDTO.
     *
     * @param dto FhirBundleDTO to validate
     * @throws IllegalArgumentException if validation fails
     */
    private void validateFhirBundleDTO(FhirBundleDTO dto) {
        Set<ConstraintViolation<FhirBundleDTO>> violations = validator.validate(dto);
        if (!violations.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (ConstraintViolation<FhirBundleDTO> v : violations) {
                sb.append(v.getPropertyPath()).append(": ").append(v.getMessage()).append("; ");
            }
            throw new IllegalArgumentException("Validation failed: " + sb);
        }
    }

    /**
     * Encrypts the extracted data in-place if encryptedData is not already provided.
     *
     * @param dto DTO with either extractedData or encryptedData
     */
    private void encryptIfNeeded(FhirBundleDTO dto) {
        if (dto.getEncryptedData() == null && dto.getExtractedData() != null) {
            LogUtil.logger.info("Encrypting extractedData for FHIR bundle...");
            String encrypted = encryptionUtilities.encryption(dto.getExtractedData());
            dto.setEncryptedData(encrypted);
            dto.setExtractedData(null); // discard plaintext
        }
    }
}
