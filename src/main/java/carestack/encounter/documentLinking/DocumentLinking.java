package carestack.encounter.documentLinking;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import carestack.base.Base;
import carestack.base.errors.EhrApiError;
import carestack.base.errors.ValidationError;
import carestack.base.utils.Constants;
import carestack.base.utils.StringUtils;
import carestack.encounter.documentLinking.dto.*;
import carestack.encounter.documentLinking.mappers.Mapper;

import java.util.Map;
import java.util.regex.Pattern;
/**
 * Provides the multistep, transactional process of linking a patient's health documents.
 * <p>
 * This service manages a complex workflow that involves creating a care context,
 * updating visit records with health information, and finally linking the context.
 * The entire operation is designed to be atomic; if any step fails, the process
 * is halted, and a detailed error is returned. The state of the transaction is
 * tracked using the {@link TransactionState} object to provide clear debugging
 * information upon failure.
 * </p>
 */
@Service
public class DocumentLinking extends Base {

    private static final Logger logger = LoggerFactory.getLogger(DocumentLinking.class);
    private static final Pattern UUID_PATTERN = Pattern.compile("^[a-fA-F0-9-]{36}$");

    private final Mapper mapper;

    public DocumentLinking(ObjectMapper objectMapper, WebClient webClient, Mapper mapper) {
        super(objectMapper, webClient);
        this.mapper = mapper;
    }

    /**
     * <h3>Dispatches to the Correct Validation Method Based on DTO Type</h3>
     * This method acts as a central validator, accepting a generic {@code Object} and routing it to the
     * appropriate type-specific validation logic. It ensures that any DTO used within the
     * {@code DocumentLinking} service passes its required checks before being processed.
     *
     * <h3>Supported DTO Types and Validation Rules:</h3>
     * <ul>
     *     <li><b>{@link HealthDocumentLinkingDTO}:</b>
     *         <ul>
     *             <li>Checks for non-empty {@code patientReference}, {@code practitionerReference}, {@code appointmentStartDate}, {@code appointmentEndDate}, {@code organizationId}, {@code mobileNumber}.</li>
     *             <li>Validates that {@code patientReference} is a 32 or 36-character UUID.</li>
     *         </ul>
     *     </li>
     *     <li><b>{@link CreateCareContextDTO}:</b>
     *         <ul>
     *             <li>Checks for non-empty {@code patientReference}, {@code practitionerReference}, {@code appointmentReference}, {@code appointmentDate}.</li>
     *             <li>Validates that UUID fields are 36-character UUIDs.</li>
     *             <li>Ensures {@code resendOtp} flag is not null.</li>
     *         </ul>
     *     </li>
     *     <li><b>{@link UpdateVisitRecordsDTO}:</b>
     *         <ul>
     *             <li>Checks for non-empty {@code careContextReference}, {@code patientReference}, {@code practitionerReference}, {@code appointmentReference}.</li>
     *         </ul>
     *     </li>
     *     <li><b>{@link LinkCareContextDTO}:</b>
     *         <ul>
     *             <li>Checks for non-empty {@code requestId}, {@code appointmentReference}, {@code patientAddress}, {@code patientReference}, {@code careContextReference}, and the value of {@code authMode}.</li>
     *         </ul>
     *     </li>
     * </ul>
     *
     * <h3>Input and Parameters:</h3>
     * @param data The DTO object to validate.
     *
     * <h3>Output:</h3>
     * This method does not return a value. It completes silently if validation is successful.
     *
     * <h3>Error Handling:</h3>
     * Throws a {@link ValidationError} under the following conditions:
     * <ul>
     *     <li>The input {@code data} object is {@code null}.</li>
     *     <li>The type of the {@code data} object is not one of the supported DTOs.</li>
     *     <li>Any of the type-specific validation rules fail. The error message will detail the specific field and rule that failed.</li>
     * </ul>
     *
     * <h3>Example Usage:</h3>
     * <pre>{@code
     * // Example 1: Valid DTO
     * HealthDocumentLinkingDTO validDto = new HealthDocumentLinkingDTO();
     * // ... populate with valid data
     * try {
     *     documentLinkingService.validateData(validDto);
     *     System.out.println("Validation successful.");
     * } catch (ValidationError e) {
     *     System.err.println("Validation failed: " + e.getMessage());
     * }
     *
     * // Example 2: Invalid DTO
     * HealthDocumentLinkingDTO invalidDto = new HealthDocumentLinkingDTO();
     * invalidDto.setPatientReference(null); // Missing required field
     * try {
     *     documentLinkingService.validateData(invalidDto);
     * } catch (ValidationError e) {
     *     System.err.println("Validation failed as expected: " + e.getMessage());
     * }
     * }</pre>
     *
     * <h3>Expected Output:</h3>
     * <pre>
     * // For Example 1:
     * Validation successful.
     *
     * // For Example 2:
     * Validation failed as expected: Validation failed: patientReference cannot be null or empty
     * </pre>
     *
     * @throws ValidationError if the object is null, the type is unsupported, or validation fails.
     */
    void validateData(Object data) {
        if (data == null) {
            throw new ValidationError("Input data cannot be null");
        }

        try {
            if (data instanceof HealthDocumentLinkingDTO) {
                validateHealthDocumentLinkingDto((HealthDocumentLinkingDTO) data);
            } else if (data instanceof CreateCareContextDTO) {
                validateCreateCareContextDto((CreateCareContextDTO) data);
            } else if (data instanceof UpdateVisitRecordsDTO) {
                validateConsultationDto((UpdateVisitRecordsDTO) data);
            } else if (data instanceof LinkCareContextDTO) {
                validateLinkCareContextDto((LinkCareContextDTO) data);
            } else {
                throw new ValidationError("Unsupported DTO type: " + data.getClass().getSimpleName());
            }
        } catch (Exception e) {
            logger.error("Validation error: {}", e.getMessage(), e);
            throw new ValidationError("Validation failed: " + e.getMessage());
        }
    }

    private void validateHealthDocumentLinkingDto(HealthDocumentLinkingDTO dto) {
        validateNotEmpty(dto.getPatientReference(), "patientReference");
        validateNotEmpty(dto.getPractitionerReference(), "practitionerReference");

        String patientRef = dto.getPatientReference();
        if (patientRef != null) {
            String cleanRef = patientRef.replace("-", "");
            if (!((cleanRef.length() == 32 || cleanRef.length() == 36) && cleanRef.matches("[a-fA-F0-9]+"))) {
                throw new ValidationError("Patient reference must be a valid 32 or 36 character UUID");
            }
        }

        validateNotEmpty(dto.getAppointmentStartDate() != null ? dto.getAppointmentStartDate() : null, "appointmentStartDate");
        validateNotEmpty(dto.getAppointmentEndDate() != null ? dto.getAppointmentEndDate() : null, "appointmentEndDate");

        if (dto.getAppointmentPriority() != null && StringUtils.isNullOrEmpty(dto.getAppointmentPriority().getValue())) {
            throw new ValidationError("Appointment priority cannot be empty");
        }

        validateNotEmpty(dto.getOrganizationId(), "organizationID");
        validateNotEmpty(dto.getMobileNumber(), "mobileNumber");
    }

    private void validateCreateCareContextDto(CreateCareContextDTO dto) {
        validateNotEmpty(dto.getPatientReference() != null ? dto.getPatientReference() : null, "patientReference");
        validateNotEmpty(dto.getPractitionerReference() != null ? dto.getPractitionerReference() : null, "practitionerReference");
        validateNotEmpty(dto.getAppointmentReference(), "appointmentReference");
        validateNotEmpty(dto.getAppointmentDate(), "appointmentDate");

        // Validate UUIDs
        String[] uuids = {
                dto.getPatientReference() != null ? dto.getPatientReference() : null,
                dto.getPractitionerReference() != null ? dto.getPractitionerReference() : null,
                dto.getAppointmentReference()
        };
        String[] fieldNames = {"patientReference", "practitionerReference", "appointmentReference"};

        for (int i = 0; i < uuids.length; i++) {
            if (uuids[i] != null && !UUID_PATTERN.matcher(uuids[i]).matches()) {
                throw new ValidationError(fieldNames[i] + " must be a valid 36-character UUID");
            }
        }

        if (dto.getResendOtp() == null) {
            throw new ValidationError("Resend OTP flag is required");
        }
    }

    private void validateConsultationDto(UpdateVisitRecordsDTO dto) {
        String[][] validations = {
                {dto.getCareContextReference(), "careContextReference"},
                {dto.getPatientReference(), "patientReference"},
                {dto.getPractitionerReference(), "practitionerReference"},
                {dto.getAppointmentReference(), "appointmentReference"}
        };

        for (String[] validation : validations) {
            validateNotEmpty(validation[0], validation[1]);
        }
    }

    private void validateLinkCareContextDto(LinkCareContextDTO dto) {
        String[][] validations = {
                {dto.getRequestId(), "requestId"},
                {dto.getAppointmentReference(), "appointmentReference"},
                {dto.getPatientAddress(), "patientAddress"},
                {dto.getPatientReference(), "patientReference"},
                {dto.getCareContextReference(), "careContextReference"},
                {dto.getAuthMode() != null ? dto.getAuthMode().getValue() : null, "authMode"}
        };

        for (String[] validation : validations) {
            validateNotEmpty(validation[0], validation[1]);
        }
    }

    private void validateNotEmpty(String value, String fieldName) {
        if (StringUtils.isNullOrEmpty(value)) {
            throw new ValidationError(fieldName + " cannot be null or empty");
        }
    }


    /**
     * Step 1 (Preparation): Maps the initial request DTO to a {@link CreateCareContextDTO}.
     *
     * @param dto The initial request data.
     * @return A {@link Mono} emitting the prepared {@link CreateCareContextDTO}.
     * @throws ValidationError if the input DTO is null.
     */
    public Mono<CreateCareContextDTO> createCareContext(HealthDocumentLinkingDTO dto) {
        return Mono.fromCallable(() -> {
            if (dto == null) {
                throw new ValidationError("Input data cannot be null");
            }

            CreateCareContextDTO careContextData = mapper.mapToCareContextDTO(dto,
                    dto.getAppointmentReference(),
                    dto.getAppointmentStartDate(),
                    dto.getAppointmentEndDate());
            validateData(careContextData);
            return careContextData;
        });
    }

    /**
     * Step 1 (Execution): Sends the request to create a care context to the remote API.
     *
     * @param careContextData The DTO containing the data for care context creation.
     * @return A {@link Mono} emitting the {@link CreateCareContextResponse} from the API.
     */
    private Mono<CreateCareContextResponse> sendCareContextRequest(CreateCareContextDTO careContextData) {
        Map<String, Object> dataToSend = serializeModel(careContextData);
        return post(
                Constants.CREATE_CARE_CONTEXT,
                dataToSend,
                new ParameterizedTypeReference<CreateCareContextResponse>() {}
        );
    }

    /**
     * Step 2: Updates the visit records with the health information provided.
     *
     * @param dto                 The initial request DTO containing health records.
     * @param careContextResponse The response from the previous step, containing necessary IDs.
     * @return A {@link Mono} emitting `true` on success.
     */
    public Mono<Boolean> updateVisitRecords(
            HealthDocumentLinkingDTO dto,
            CreateCareContextResponse careContextResponse) {

        return Mono.fromCallable(() -> {
                    UpdateVisitRecordsDTO consultationData = mapper.mapToConsultationDTO(
                            dto,
                            dto.getAppointmentReference(),
                            careContextResponse.getCareContextReference(),
                            careContextResponse.getRequestId()
                    );

                    validateData(consultationData);
                    return consultationData;
                })
                .flatMap(consultationData -> {
                    Map<String, Object> dataToSend = serializeModel(consultationData);
                    return post(
                            Constants.UPDATE_VISIT_RECORDS,
                            dataToSend,
                            new ParameterizedTypeReference<Boolean>() {}
                    );
                });
    }

    /**
     * Step 3: Links the care context after it has been created and updated.
     *
     * @param healthDocumentLinkingDto The original request DTO.
     * @param careContextResponse      The response from the care context creation step.
     * @return A {@link Mono} emitting `true` on success.
     */
    public Mono<Boolean> linkCareContext(
            HealthDocumentLinkingDTO healthDocumentLinkingDto,
            CreateCareContextResponse careContextResponse) {

        return Mono.fromCallable(() -> {

                    LinkCareContextDTO linkData = mapper.mapToLinkCareContextDTO(
                            healthDocumentLinkingDto,
                            careContextResponse.getCareContextReference(),
                            healthDocumentLinkingDto.getAppointmentReference(),
                            careContextResponse.getRequestId()
                    );
                    validateData(linkData);
                    return linkData;
                })
                .flatMap(linkData -> {
                    Map<String, Object> dataToSend = serializeModel(linkData);
                    return post(
                            Constants.LINK_CARE_CONTEXT,
                            dataToSend,
                            new ParameterizedTypeReference<Boolean>() {}
                    );
                });
    }

    /**
     * <h3>Performs the Complete End-to-End Health Document Linking Process</h3>
     * This is the main entry point for the service. It orchestrates a multi-step, transactional
     * workflow to link a patient's health documents. The process is designed to be atomic:
     * if any step fails, the entire transaction is halted, and a detailed error is propagated.
     *
     * <h3>Workflow Steps:</h3>
     * <ol>
     *     <li><b>Validation:</b> The initial {@link HealthDocumentLinkingDTO} is validated.</li>
     *     <li><b>Care Context Creation:</b> A request is sent to the EHR to create a new care context.</li>
     *     <li><b>Visit Record Update (Conditional):</b> If the input DTO contains health records, they are added to the newly created context.</li>
     *     <li><b>Care Context Linking (Conditional):</b> If the records are updated successfully, the care context is formally linked.</li>
     * </ol>
     *
     * <h3>Input and Validation:</h3>
     * The method requires a {@link HealthDocumentLinkingDTO} object containing all necessary information.
     * The following fields are validated internally:
     * <ul>
     *     <li>{@code patientReference}: Must not be null/empty and must be a valid 32 or 36-character UUID.</li>
     *     <li>{@code practitionerReference}: Must not be null/empty.</li>
     *     <li>{@code appointmentStartDate}: Must not be null/empty.</li>
     *     <li>{@code appointmentEndDate}: Must not be null/empty.</li>
     *     <li>{@code organizationId}: Must not be null/empty.</li>
     *     <li>{@code mobileNumber}: Must not be null/empty.</li>
     *     <li>{@code healthRecords}: If this list is empty or null, the update and linking steps are skipped.</li>
     * </ul>
     *
     * <h3>Output:</h3>
     * @return A {@link Mono<Boolean>} that emits:
     * <ul>
     *     <li>{@code true}: If the entire process (create, update, and link) completes successfully.</li>
     *     <li>{@code false}: If no health records are provided (skipping update/link) or if the update/link steps fail without throwing a fatal error.</li>
     * </ul>
     *
     * <h3>Error Handling:</h3>
     * The reactive stream is designed to handle errors gracefully:
     * <ul>
     *     <li><b>Validation Errors:</b> If the initial validation or any subsequent validation fails, the stream emits a {@link Mono#error(Throwable)} with a {@link ValidationError}.</li>
     *     <li><b>API Errors:</b> If any of the remote API calls fail (e.g., network issue, server error), the stream emits a {@link Mono#error(Throwable)} with an {@link EhrApiError}. This error includes the current state of the transaction (e.g., "Care context created, but visit record update failed") for easier debugging.</li>
     *     <li><b>Unexpected Errors:</b> Any other exceptions are caught and wrapped in a generic {@link EhrApiError} with an HTTP 500 status code.</li>
     * </ul>
     * <h3>Example Usage:</h3>
     * <pre>{@code
     * HealthDocumentLinkingDTO linkingRequest = new HealthDocumentLinkingDTO();
     * // ... populate the DTO with all required patient, practitioner, and health record data
     *
     * documentLinkingService.linkHealthDocument(linkingRequest)
     *     .subscribe(
     *         success -> {
     *             if (success) {
     *                 System.out.println("Health document linked successfully.");
     *             } else {
     *                 System.out.println("Linking process finished, but some steps were skipped or did not complete (e.g., no health records to link).");
     *             }
     *         },
     *         error -> {
     *             if (error instanceof EhrApiError) {
     *                 System.err.println("API Error during linking: " + error.getMessage());
     *             } else if (error instanceof ValidationError) {
     *                 System.err.println("Validation Error: " + error.getMessage());
     *             } else {
     *                 System.err.println("An unexpected error occurred: " + error.getMessage());
     *             }
     *         }
     *     );
     * }</pre>
     *
     * <h3>Expected Output:</h3>
     * <pre>
     * // For a successful full transaction:
     * Health document linked successfully.
     *
     * // If no health records were provided:
     * Linking process finished, but some steps were skipped or did not complete (e.g., no health records to link).
     *
     * // If an API error occurs after creating the care context:
     * API Error during linking: Transaction failed due to API error: [API specific message]. Current state: TransactionState[careContextReference=cc-ref-***, ..., careContextCreated=true, visitRecordsUpdated=false, careContextLinked=false]
     * </pre>
     *
     * @param healthDocumentLinkingDto The DTO containing all necessary information for the transaction.
     */
    public Mono<Boolean> linkHealthDocument(HealthDocumentLinkingDTO healthDocumentLinkingDto) {
        TransactionState transactionState = new TransactionState();

        return Mono.fromRunnable(() -> validateData(healthDocumentLinkingDto))
                .then(createCareContext(healthDocumentLinkingDto))
                .flatMap(this::sendCareContextRequest)
                .doOnNext(careContextResponse -> {
                    transactionState.setCareContextReference(careContextResponse.getCareContextReference());
                    transactionState.setRequestId(careContextResponse.getRequestId());
                    transactionState.setCareContextCreated(true);
                    logger.info("Care context created with reference: {}", transactionState.getCareContextReference());
                })
                .flatMap(careContextResponse -> {
                    if (healthDocumentLinkingDto.getHealthRecords() != null && !healthDocumentLinkingDto.getHealthRecords().isEmpty()) {
                        return updateVisitRecords(healthDocumentLinkingDto, careContextResponse)
                                .flatMap(updateSuccess -> {
                                    if (Boolean.TRUE.equals(updateSuccess)) {
                                        transactionState.setVisitRecordsUpdated(true);
                                        return linkCareContext(healthDocumentLinkingDto, careContextResponse)
                                                .doOnNext(linkSuccess -> {
                                                    if (Boolean.TRUE.equals(linkSuccess)) {
                                                        transactionState.setCareContextLinked(true);
                                                        logger.info("Care context linked successfully");
                                                    } else {
                                                        logger.warn("Failed to link care context");
                                                    }
                                                });
                                    } else {
                                        logger.warn("Visit records update failed or returned false, aborting linking.");
                                        return Mono.just(false);
                                    }
                                });
                    } else {
                        logger.info("No health records provided. Skipping visit record update and linking.");
                        return Mono.just(false);
                    }
                })
                .onErrorMap(ValidationError.class, e ->
                        new ValidationError("Transaction failed due to data validation error: " + e.getMessage())
                )
                .onErrorMap(
                        e -> e instanceof EhrApiError && !(e instanceof ValidationError),
                        e -> {
                            EhrApiError apiError = (EhrApiError) e;
                            return new EhrApiError(
                                    "Transaction failed due to API error: " + apiError.getMessage() + ". Current state: " + transactionState,
                                    HttpStatusCode.valueOf(apiError.getStatusCode())
                            );
                        }
                )
                .onErrorMap(e -> !(e instanceof EhrApiError),
                        e -> new EhrApiError(
                                "Transaction failed due to unexpected error : " + e.getMessage() + ". Current state:" + transactionState,
                                HttpStatusCode.valueOf(500)
                        )
                );
    }
}