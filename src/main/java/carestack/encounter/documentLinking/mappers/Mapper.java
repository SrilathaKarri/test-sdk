package carestack.encounter.documentLinking.mappers;

import org.springframework.stereotype.Component;
import carestack.encounter.documentLinking.dto.*;

/**
 * A utility class responsible for mapping {@link HealthDocumentLinkingDTO} objects
 * to various Data Transfer Objects (DTOs) used in the health document linking a process.
 *
 * <p>This class acts as a wrapper around the MapStruct-generated {@link HealthDocumentMapper}
 * to provide clean, static access to mapping functionalities across the application.</p>
 *
 * <h3>Key Features:</h3>
 * <ul>
 *   <li><strong>Delegation to MapStruct:</strong> Leverages the MapStruct-generated mapper for efficient DTO transformations.</li>
 *   <li><strong>Static Access:</strong> Provides static methods for common mapping operations, reducing the need for object instantiation.</li>
 *   <li><strong>Spring Integration:</strong> Annotated with {@link Component} for dependency injection.</li>
 * </ul>
 */
@Component
public class Mapper {

    /**
     * Singleton instance of the MapStruct-generated {@link HealthDocumentMapper}.
     */
    private static final HealthDocumentMapper mapper = HealthDocumentMapper.INSTANCE;

    /**
     * Maps {@link HealthDocumentLinkingDTO} to {@link CreateCareContextDTO}.
     *
     * <p>Includes appointment reference and formatted appointment date range.</p>
     *
     * @param data                  {@link HealthDocumentLinkingDTO} source object
     * @param appointmentReference  Reference ID for the appointment
     * @param appointmentStartDate  Appointment start date
     * @param appointmentEndDate    Appointment end date
     * @return {@link CreateCareContextDTO} with mapped values
     */
    public CreateCareContextDTO mapToCareContextDTO(HealthDocumentLinkingDTO data,
                                                    String appointmentReference,
                                                    String appointmentStartDate,
                                                    String appointmentEndDate) {
        return mapper.mapToCareContextDTO(data, appointmentReference, appointmentStartDate, appointmentEndDate);
    }

    /**
     * Maps {@link HealthDocumentLinkingDTO} to {@link OpConsultationDTO}.
     *
     * <p>Includes patient reference, practitioner reference, health records, mobile number,
     * and request ID for tracking.</p>
     *
     * @param data                  {@link HealthDocumentLinkingDTO} source object
     * @param appointmentReference  Reference ID for the appointment
     * @param careContextReference  Reference ID for the care context
     * @param requestId             Request ID for tracking
     * @return {@link OpConsultationDTO} with mapped values
     */
    public UpdateVisitRecordsDTO mapToConsultationDTO(HealthDocumentLinkingDTO data,
                                                      String appointmentReference,
                                                      String careContextReference,
                                                      String requestId) {
        return mapper.mapToConsultationDTO(data, appointmentReference, careContextReference, requestId);
    }

    /**
     * Maps {@link HealthDocumentLinkingDTO} to {@link LinkCareContextDTO}.
     *
     * <p>Handles optional request IDs and sets the authentication mode to "DEMOGRAPHICS".</p>
     *
     * @param data                  {@link HealthDocumentLinkingDTO} source object
     * @param careContextReference  Reference ID for the care context
     * @param appointmentReference  Reference ID for the appointment
     * @param requestId             Optional request ID for tracking
     * @return {@link LinkCareContextDTO} with mapped values
     */
    public LinkCareContextDTO mapToLinkCareContextDTO(HealthDocumentLinkingDTO data,
                                                      String careContextReference,
                                                      String appointmentReference,
                                                      String requestId) {
        return mapper.mapToLinkCareContextDTO(data, careContextReference, appointmentReference, requestId);
    }
}
