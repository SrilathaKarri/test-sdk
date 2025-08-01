package carestack.encounter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import carestack.ai.AiService;
import carestack.ai.EncryptionUtilities;
import carestack.ai.dto.FhirBundleDTO;
import carestack.ai.dto.ProcessDSDto;
import carestack.base.Base;
import carestack.base.enums.CaseType;
import carestack.base.utils.Constants;
import carestack.base.utils.LogUtil;
import carestack.encounter.dtos.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service class for orchestrating the generation of FHIR bundles from various health data sources.
 * <p>
 * This service acts as a central hub for converting health information into the FHIR standard.
 * It provides a unified entry point, {@link #createEncounter(EncounterRequestDTO)}, which intelligently
 * routes requests based on the provided data format. This allows for seamless integration of both
 * unstructured documents and structured data payloads.
 *
 * <h3>Core Capabilities:</h3>
 * <ol>
 *   <li><b>Unstructured Document Processing:</b> When provided with URLs to clinical documents (e.g., case sheets, lab reports),
 *   the service leverages an AI backend to perform Optical Character Recognition (OCR) and Natural Language Processing (NLP).
 *   It extracts structured information from the documents and then assembles it into a FHIR bundle. This is ideal for
 *   digitizing legacy records or processing scanned files.</li>
 *
 *   <li><b>Structured Data Processing:</b> When provided with a direct JSON payload that conforms to one of the
 *   pre-defined DTO structures (e.g., {@link OPConsultationDTO}), the service validates the data and directly
 *   generates a corresponding FHIR bundle. This pathway is highly efficient for data originating from modern,
 *   structured systems.</li>
 * </ol>
 *
 * By handling these two distinct workflows, the {@code Encounter} service provides a powerful and flexible tool
 * for achieving interoperability in healthcare data exchange.
 */
@Service
public class Encounter extends Base {

    private final Validator validator;

    @Autowired
    EncryptionUtilities encryptionUtilities;

    @Autowired
    private AiService aiService;

    public Encounter(ObjectMapper objectMapper,
                     WebClient webClient,
                     Validator validator) {
        super(objectMapper, webClient);
        this.validator = validator;
    }

    /**
     * Validates a given DTO object using the configured JSR-380 validator.
     *
     * @param dto The DTO to validate.
     * @param <T> The type of the DTO.
     * @throws IllegalArgumentException if validation fails.
     */
    private <T> void validateInput(T dto) {
        Set<ConstraintViolation<T>> violations = validator.validate(dto);
        if (!violations.isEmpty()) {
            String errorMessage = violations.stream()
                    .map(violation -> violation.getPropertyPath() + " " + violation.getMessage())
                    .collect(Collectors.joining("; "));

            LogUtil.logger.error("Validation errors for {}: {}", dto.getClass().getSimpleName(), errorMessage);
            throw new IllegalArgumentException("Validation failed: " + errorMessage);
        }
    }


    /**
     * <h3>Generate a FHIR Bundle from Health Data</h3>
     * This is the primary entry point for creating a FHIR bundle. It functions as a dispatcher,
     * analyzing the {@link EncounterRequestDTO} to determine whether to process unstructured files
     * or a structured JSON payload.
     *
     * <h3>Input and Parameters:</h3>
     * The method accepts a single {@link EncounterRequestDTO} object, which contains all the necessary
     * information for the operation.
     *
     * <ul>
     *     <li><b>{@code request} (EncounterRequestDTO):</b> The main request object. It must not be null.
     *         <ul>
     *             <li><b>{@code caseType} (CaseType):</b> Mandatory. Specifies the clinical context (e.g., {@code OP_CONSULTATION}, {@code DISCHARGE_SUMMARY}). This determines how the data is interpreted and which internal logic is used.</li>
     *             <li><b>{@code dto} (Object):</b> Mandatory. This flexible field must be a {@code Map<String, Object>}. It acts as a container for the actual health data and must contain one of the following keys:
     *                 <ul>
     *                     <li><b>"caseSheets" (List&lt;String&gt;):</b> A list of URLs pointing to unstructured documents. Use this for the file-based workflow.</li>
     *                     <li><b>"Payload" (Object):</b> A structured JSON object (can be a {@code Map} or {@code JsonNode}) that conforms to the expected DTO for the given {@code caseType}. Use this for the payload-based workflow.</li>
     *                 </ul>
     *             </li>
     *             <li><b>{@code labReports} (List&lt;String&gt;):</b> Optional. A list of URLs for supplementary lab reports. This can be provided in both workflows.</li>
     *         </ul>
     *     </li>
     * </ul>
     *
     * <h3>Validation Rules:</h3>
     * <ul>
     *     <li>The {@code EncounterRequestDTO} object itself is validated for JSR-380 constraints (e.g., {@code @NotNull}).</li>
     *     <li>The request must contain either a 'payload' key or a 'caseSheets' key within the {@code dto} map. Providing both or neither will result in an error.</li>
     *     <li>The value for 'payload' must be a JSON object (e.g., a Map).</li>
     *     <li>The value for 'caseSheets' must be a list of strings.</li>
     * </ul>
     *
     * <h3>Output:</h3>
     * @return A {@link Mono} that emits the generated FHIR bundle. On success, the emitted object is typically a
     *         {@code Map<String, Object>} representing the complete FHIR bundle JSON structure.
     *
     * <h3>Error Handling:</h3>
     * The method returns a {@link Mono#error(Throwable)} under several conditions:
     * <ul>
     *     <li>{@link IllegalArgumentException}: If validation fails (e.g., missing required fields, invalid payload structure).</li>
     *     <li>Other runtime exceptions if downstream API calls fail or if the AI service cannot process the documents.</li>
     * </ul>
     *
     * <h3>Example 1: Generating FHIR from Unstructured Files</h3>
     * <pre>{@code
     * EncounterRequestDTO request = new EncounterRequestDTO();
     * request.setCaseType(CaseType.DISCHARGE_SUMMARY);
     *
     * // The 'dto' field holds a map containing the file URLs
     * Map<String, Object> dataMap = new HashMap<>();
     * dataMap.put("caseSheets", List.of("https://example.com/docs/discharge-summary-123"));
     * request.setDto(dataMap);
     *
     * // Optionally add lab reports
     * request.setLabReports(List.of("https://example.com/labs/report-456"));
     *
     * encounterService.createEncounter(request)
     *     .subscribe(
     *         fhirBundle -> System.out.println("FHIR Bundle generated: " + fhirBundle),
     *         error -> System.err.println("Error generating bundle: " + error.getMessage())
     *     );
     * }</pre>
     *
     * <h3>Example 2: Generating FHIR from a Structured Payload</h3>
     * <pre>{@code
     * EncounterRequestDTO request = new EncounterRequestDTO();
     * request.setCaseType(CaseType.OP_CONSULTATION);
     *
     * // The 'dto' field holds a map containing the structured payload
     * Map<String, Object> payload = new HashMap<>();
    // ... populate the payload map with data conforming to OPConsultationSections ...
     * payload.put("chiefComplaints", "Fever and cough for 3 days.");
     * // ... other fields like patientDetails, doctorDetails, etc.
     *
     * Map<String, Object> dataMap = new HashMap<>();
     * dataMap.put("payload", payload);
     * request.setDto(dataMap);
     *
     * encounterService.createEncounter(request)
     *     .subscribe(
     *         fhirBundle -> System.out.println("FHIR Bundle generated: " + fhirBundle),
     *         error -> System.err.println("Error generating bundle: " + error.getMessage())
     *     );
     * }</pre>
     *
     * <h3>Expected Output (Conceptual):</h3>
     * <pre>{@code
     * // A map representing a FHIR Bundle resource
     * {
     *   "resourceType": "Bundle",
     *   "id": "bundle-transaction-123",
     *   "type": "transaction",
     *   "entry": [
     *     {
     *       "fullUrl": "urn:uuid:...",
     *       "resource": { "resourceType": "Patient", ... },
     *       "request": { "method": "POST", "url": "Patient" }
     *     },
     *     {
     *       "fullUrl": "urn:uuid:...",
     *       "resource": { "resourceType": "Encounter", ... },
     *       "request": { "method": "POST", "url": "Encounter" }
     *     },
     *     // ... other FHIR resources
     *   ]
     * }
     * }</pre>
     *
     * @param request The request DTO containing either files or a structured payload.
     */
    public Mono<?> createEncounter(EncounterRequestDTO request) {
        validateInput(request);

        Optional<Object> payloadOpt = request.getPayload();
        if (payloadOpt.isPresent()) {
            Object actualPayload = payloadOpt.get();

            if (actualPayload instanceof Map<?, ?> || actualPayload instanceof JsonNode) {
                LogUtil.logger.info("Payload is a JSON object for CaseType: {}", request.getCaseType());
                return generateFhirFromPayload(request.getCaseType(), request.getDto(), Optional.ofNullable(request.getLabReports()));
            } else {
                LogUtil.logger.error("Unexpected payload type: {} inside dto.", actualPayload.getClass().getName());
                return Mono.error(new IllegalArgumentException(
                        "Invalid payload type: expected JSON object for payload."
                ));
            }
        }

        Optional<List<String>> filesOpt = request.getCaseSheets();
        if (filesOpt.isPresent()) {
            List<String> files = filesOpt.get();

            if (files.size() == 1) {
                LogUtil.logger.info("Payload is a single String (likely a file URL) for CaseType: {}", request.getCaseType());
                return generateFhirFromFiles(request.getCaseType(), List.of(files.get(0)), Optional.ofNullable(request.getLabReports()));
            } else {
                LogUtil.logger.info("Payload is a List of Strings (likely multiple file URLs) for CaseType: {}", request.getCaseType());
                return generateFhirFromFiles(request.getCaseType(), files, Optional.ofNullable(request.getLabReports()));
            }
        }

        LogUtil.logger.error("Neither payload nor files found in request DTO.");
        return Mono.error(new IllegalArgumentException("Invalid request: Provide either 'payload' or 'files' in dto."));
    }


    /**
     * <h3>Generate FHIR Bundle from Unstructured Files</h3>
     * This internal method orchestrates the FHIR bundle generation from a list of file URLs. It implements the
     * unstructured data processing workflow by interacting with a backend AI service.
     *
     * <h3>Workflow:</h3>
     * <ol>
     *   <li>Encrypts the file URLs for secure transmission.</li>
     *   <li>Calls an AI service endpoint ({@code /generate-case-sheet-summary}) to extract structured data from the documents.</li>
     *   <li>Receives the extracted data and validates its presence.</li>
     *   <li>Calls a second service endpoint ({@code /generate-fhir-bundle}), passing the extracted data and any optional lab reports to produce the final FHIR bundle.</li>
     * </ol>
     * This method is intended for internal use by {@link #createEncounter(EncounterRequestDTO)}.
     *
     * <h3>Input and Parameters:</h3>
     * <ul>
     *     <li><b>{@code caseType} (CaseType):</b> Mandatory. The clinical context for the documents (e.g., {@code DISCHARGE_SUMMARY}).</li>
     *     <li><b>{@code fileStrings} (List&lt;String&gt;):</b> Mandatory. A list of URLs pointing to the primary clinical documents (case sheets).</li>
     *     <li><b>{@code labReports} (Optional&lt;List&lt;String&gt;&gt;):</b> Optional. A list of URLs for supplementary lab reports.</li>
     * </ul>
     *
     * <h3>Output:</h3>
     * @return A {@link Mono<Map<String, Object>>} that emits the generated FHIR bundle as a {@code Map<String, Object>}.
     *
     * <h3>Error Handling:</h3>
     * The method returns a {@link Mono#error(Throwable)} if any step in the process fails, including:
     * <ul>
     *     <li>Failure to encrypt file URLs.</li>
     *     <li>Validation errors on the internal DTOs.</li>
     *     <li>Network or server errors from the AI service calls.</li>
     *     <li>The AI service fails to return extracted data from the documents.</li>
     * </ul>
     *
     * @param caseType    The clinical context for the documents.
     * @param fileStrings A list of URLs pointing to the primary clinical documents.
     * @param labReports  An optional list of URLs for supporting lab reports.
     */
    public Mono<Map<String, Object>> generateFhirFromFiles(CaseType caseType, List<String> fileStrings, Optional<List<String>> labReports) {
        return Mono.fromCallable(() -> {
                    String finalEncryptedData = encryptionUtilities.encryption(fileStrings);
                    ProcessDSDto payload = new ProcessDSDto();
                    payload.setFiles(fileStrings);
                    payload.setEncryptedData(finalEncryptedData);
                    payload.setCaseType(caseType);

                    validateInput(payload);

                    return payload;
                })
                // Step 1: Call /generate-discharge-summary
                .flatMap(payload -> {
                    LogUtil.logger.info("Calling /generate-discharge-summary for files: {}", fileStrings);
                    return post(Constants.GENERATE_CASE_SHEET_SUMMARY_URL, payload,
                            new ParameterizedTypeReference<DischargeSummaryResponse>() {});
                })
                // Step 2: Extract `extractedData` and pass to /generate-fhir-bundle
                .flatMap(dischargeResponse -> {
                    Map<String, Object> extractedData = dischargeResponse.getExtractedData();
                    if (extractedData == null || extractedData.isEmpty()) {
                        return Mono.error(new IllegalArgumentException("Discharge summary response did not contain extracted data"));
                    }

                    FhirBundleDTO fhirBundleDTO = new FhirBundleDTO();
                    fhirBundleDTO.setCaseType(caseType);
                    fhirBundleDTO.setExtractedData(extractedData);

                    if (labReports.isPresent() && !labReports.get().isEmpty()) {
                        String encryptedLabReports = encryptionUtilities.encryption(labReports.get());
                        fhirBundleDTO.setFiles(List.of(encryptedLabReports));
                    }

                    validateInput(fhirBundleDTO);

                    LogUtil.logger.info("Calling /generate-fhir-bundle with extracted data for caseType {}", caseType);
                    return post(Constants.GENERATE_FHIR_BUNDLE_URL, fhirBundleDTO,
                            new ParameterizedTypeReference<Map<String, Object>>() {});
                })
                .doOnSuccess(response -> LogUtil.logger.info("FHIR bundle generated successfully from files"))
                .doOnError(error -> LogUtil.logger.error("Error generating FHIR bundle from files: {}", error.getMessage(), error));
    }

    /**
     * <h3>Generate FHIR Bundle from Structured Payload</h3>
     * This internal method generates a FHIR bundle directly from a structured JSON payload. It implements the
     * structured data processing workflow.
     *
     * <h3>Workflow:</h3>
     * <ol>
     *   <li>Maps the raw JSON payload to a specific, strongly typed DTO (e.g., {@link OPConsultationDTO}) based on the {@code caseType}.</li>
     *   <li>Validates the resulting DTO against its defined constraints.</li>
     *   <li>Constructs a {@link FhirBundleDTO} containing the structured data.</li>
     *   <li>If lab reports are provided, they are encrypted and added to the request.</li>
     *   <li>Calls the {@code /generate-fhir-bundle} service endpoint to produce the final FHIR bundle.</li>
     * </ol>
     * This method is intended for internal use by {@link #createEncounter(EncounterRequestDTO)}.
     *
     * <h3>Input and Parameters:</h3>
     * <ul>
     *     <li><b>{@code caseType} (CaseType):</b> Mandatory. The clinical context, which dictates how the payload is interpreted.</li>
     *     <li><b>{@code rawJsonPayload} (Object):</b> Mandatory. The structured data, typically a {@code Map} or {@code JsonNode}, conforming to the expected schema for the given {@code caseType}.</li>
     *     <li><b>{@code labReports} (Optional&lt;List&lt;String&gt;&gt;):</b> Optional. A list of URLs for supplementary lab reports.</li>
     * </ul>
     *
     * <h3>Output:</h3>
     * @return A {@link Mono<Map<String, Object>>} that emits the generated FHIR bundle as a {@code Map<String, Object>}.
     *
     * <h3>Error Handling:</h3>
     * The method returns a {@link Mono#error(Throwable)} if any step in the process fails, including:
     * <ul>
     *     <li>Failure to map the raw JSON to a specific DTO (e.g., invalid structure, unsupported case type).</li>
     *     <li>Validation errors on the mapped DTO.</li>
     *     <li>Network or server errors from the {@code /generate-fhir-bundle} API call.</li>
     * </ul>
     *
     * @param caseType       The clinical context for the data.
     * @param rawJsonPayload The structured data, typically a Map or JsonNode.
     * @param labReports     An optional list of URLs for supporting lab reports.
     */
    public Mono<Map<String, Object>> generateFhirFromPayload(CaseType caseType, Object rawJsonPayload, Optional<List<String>> labReports) {
        return Mono.fromCallable(() -> {
                    HealthInformationDTO specificDto = mapJsonToSpecificDto(caseType, rawJsonPayload);
                    validateInput(specificDto);

                    FhirBundleDTO fhirBundleDTO = new FhirBundleDTO();
                    fhirBundleDTO.setExtractedData(objectMapper.convertValue(specificDto.getPayload(), Map.class));
                    fhirBundleDTO.setCaseType(caseType);

                    if (labReports.isPresent() && !labReports.get().isEmpty()) {
                        String encryptedLabReports = encryptionUtilities.encryption(labReports.get());
                        fhirBundleDTO.setFiles(List.of(encryptedLabReports));
                    }

                    validateInput(fhirBundleDTO);

                    return fhirBundleDTO;
                })
                .flatMap(dto -> {
                    LogUtil.logger.info("Calling external service for JSON-data-based FHIR bundle generation at {}", Constants.GENERATE_FHIR_BUNDLE_URL);
                    return post(Constants.GENERATE_FHIR_BUNDLE_URL, dto, new ParameterizedTypeReference<Map<String, Object>>() {});
                })
                .doOnSuccess(response -> LogUtil.logger.info("FHIR bundle successfully generated from raw JSON data"))
                .doOnError(error -> LogUtil.logger.error("Error generating FHIR bundle from raw JSON data: {}", error.getMessage(), error));
    }

    /**
     * <h3>Map Raw JSON to a Specific Health DTO</h3>
     * A private utility method that converts a raw JSON payload (represented as a {@code Map} or {@code JsonNode})
     * into a specific, strongly-typed {@link HealthInformationDTO} implementation based on the provided {@link CaseType}.
     *
     * <h3>Input and Parameters:</h3>
     * <ul>
     *     <li><b>{@code caseType} (CaseType):</b> Mandatory. This enum value determines the target DTO class for deserialization (e.g., {@code OP_CONSULTATION} maps to {@link OPConsultationDTO}).</li>
     *     <li><b>{@code rawJsonPayload} (Object):</b> Mandatory. The raw payload object, which must be an instance of {@code Map} or {@code JsonNode}.</li>
     * </ul>
     *
     * <h3>Output:</h3>
     * @return An instance of the specific {@link HealthInformationDTO} subclass (e.g., {@link OPConsultationDTO}, {@link DischargeSummaryDTO}) populated with data from the payload.
     *
     * <h3>Validation and Error Handling:</h3>
     * This method is strict and will throw an {@link IllegalArgumentException} under the following conditions:
     * <ul>
     *     <li>If the {@code rawJsonPayload} is {@code null}.</li>
     *     <li>If the provided {@code caseType} is not supported in the internal {@code switch} statement.</li>
     *     <li>If the {@code rawJsonPayload} is not a {@code Map} or {@code JsonNode}.</li>
     *     <li>If the Jackson {@code ObjectMapper} fails to map the payload to the target DTO class, which can happen if the JSON structure is invalid or missing required fields.</li>
     * </ul>
     *
     * @param caseType       The case type that determines the target DTO class.
     * @param rawJsonPayload The raw payload (Map or JsonNode).
     */
    private HealthInformationDTO mapJsonToSpecificDto(CaseType caseType, Object rawJsonPayload) {
        if (rawJsonPayload == null) {
            throw new IllegalArgumentException("JSON payload cannot be null for case type: " + caseType);
        }

        Class<? extends HealthInformationDTO> targetDtoClass = switch (caseType) {
            case OP_CONSULTATION -> OPConsultationDTO.class;
            case DISCHARGE_SUMMARY -> DischargeSummaryDTO.class;
            case PRESCRIPTION -> PrescriptionRecordDTO.class;
            case WELLNESS_RECORD -> WellnessRecordDTO.class;
            case IMMUNIZATION_RECORD -> ImmunizationRecordDTO.class;
            case DIAGNOSTIC_REPORT -> DiagnosticReportDTO.class;
            case HEALTH_DOCUMENT_RECORD -> HealthDocumentRecordDTO.class;
            default -> throw new IllegalArgumentException("Unsupported CaseType for JSON payload mapping: " + caseType);
        };

        try {
            if (rawJsonPayload instanceof JsonNode) {
                return objectMapper.treeToValue((JsonNode) rawJsonPayload, targetDtoClass);
            } else if (rawJsonPayload instanceof Map) {
                return objectMapper.convertValue(rawJsonPayload, targetDtoClass);
            } else {
                throw new IllegalArgumentException("Raw JSON payload must be a JsonNode or Map, but was: " + rawJsonPayload.getClass().getName());
            }
        } catch (Exception e) {
            LogUtil.logger.error("Failed to map JSON payload for case type {} to DTO {}: {}", caseType, targetDtoClass.getSimpleName(), e.getMessage(), e);
            throw new IllegalArgumentException(String.format("Invalid JSON payload for case type %s: %s", caseType, e.getMessage()), e);
        }
    }
}
