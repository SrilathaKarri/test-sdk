package carestack.documentLinking.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;
import carestack.documentLinking.dto.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * Mapper interface for transforming {@link HealthDocumentLinkingDTO} objects into various DTOs
 * used in a health document linking workflows, such as {@link AppointmentDTO}, {@link CreateCareContextDTO},
 * {@link OpConsultationDTO}, and {@link LinkCareContextDTO}.
 *
 * <p>This interface uses MapStruct for automatic code generation of mapping implementations,
 * reducing boilerplate code and ensuring consistency in data transformations.</p>
 *
 * <h3>Key Features:</h3>
 * <ul>
 *   <li><strong>DTO Mappings:</strong> Maps a health document linking data to appointment, care context, consultation, and link care context DTOs.</li>
 *   <li><strong>Date & Time Formatting:</strong> Handles complex date/time formatting, including validation for invalid dates.</li>
 *   <li><strong>Null Handling:</strong> Ensures null-safe mapping for optional fields like mobile numbers and health records.</li>
 * </ul>
 */
@Mapper
public interface HealthDocumentMapper {

    /**
     * Singleton instance of the mapper for use in the application.
     */
    HealthDocumentMapper INSTANCE = Mappers.getMapper(HealthDocumentMapper.class);

    /**
     * Maps {@link HealthDocumentLinkingDTO} to {@link CreateCareContextDTO}.
     *
     * <p>Formats the appointment date range and sets the resendOtp flag to false.</p>
     *
     * @param data                  {@link HealthDocumentLinkingDTO} source object
     * @param appointmentReference  Reference ID for the appointment
     * @param appointmentStartDate  Appointment start date
     * @param appointmentEndDate    Appointment end date
     * @return {@link CreateCareContextDTO} with mapped values
     */
    @Mapping(target = "resendOtp", constant = "false")
    @Mapping(target = "appointmentDate", expression = "java(formatTimeRange(appointmentStartDate, appointmentEndDate))")
    CreateCareContextDTO mapToCareContextDTO(HealthDocumentLinkingDTO data,
                                                               String appointmentReference,
                                                               String appointmentStartDate,
                                                               String appointmentEndDate);

    /**
     * Formats the appointment date range into a human-readable string.
     *
     * @param startDate Start date in ISO format
     * @param endDate   End date in ISO format
     * @return Formatted date range or error message if invalid
     */
    @Named("formatTimeRange")
    default String formatTimeRange(String startDate, String endDate) {
        String formattedStartDate = formatDate(startDate);
        String formattedEndDate = formatDate(endDate);
        boolean startDateValid = !formattedStartDate.equals("Invalid date");
        boolean endDateValid = !formattedEndDate.equals("Invalid date");

        if (startDateValid && endDateValid) {
            return formattedStartDate + " - " + formattedEndDate;
        } else if (startDateValid) {
            return formattedStartDate + " - (Invalid end date)";
        } else if (endDateValid) {
            return "(Invalid start date) - " + formattedEndDate;
        } else {
            return "Invalid date range";
        }
    }

    /**
     * Maps {@link HealthDocumentLinkingDTO} to {@link OpConsultationDTO}.
     *
     * <p>Includes patient reference, practitioner reference, health records, and mobile number.</p>
     *
     * @param data                  {@link HealthDocumentLinkingDTO} source object
     * @param appointmentReference  Reference ID for the appointment
     * @param careContextReference  Reference ID for the care context
     * @param requestId             Request ID for tracking
     * @return {@link OpConsultationDTO} with mapped values
     */
    @Mapping(target = "patientReference", source = "data.patientReference")
    @Mapping(target = "practitionerReference", source = "data.practitionerReference")
    @Mapping(target = "healthRecords", expression = "java(getHealthRecords(data))")
    @Mapping(target = "mobileNumber", expression = "java(getMobileNumber(data))")
    UpdateVisitRecordsDTO mapToConsultationDTO(HealthDocumentLinkingDTO data,
                                               String appointmentReference,
                                               String careContextReference,
                                               String requestId);

    /**
     * Maps {@link HealthDocumentLinkingDTO} to {@link LinkCareContextDTO}.
     *
     * <p>Sets the authentication mode to "DEMOGRAPHICS" and handles optional request IDs.</p>
     *
     * @param data                  {@link HealthDocumentLinkingDTO} source object
     * @param careContextReference  Reference ID for the care context
     * @param appointmentReference  Reference ID for the appointment
     * @param requestId             Optional request ID for tracking
     * @return {@link LinkCareContextDTO} with mapped values
     */
    @Mapping(target = "patientReference", source = "data.patientReference")
    @Mapping(target = "careContextReference", source = "careContextReference")
    @Mapping(target = "appointmentReference", source = "appointmentReference")
    @Mapping(target = "requestId", expression = "java(requestId != null ? requestId : \"\")")
    @Mapping(target = "authMode", expression = "java(carestack.base.enums.DocLinkingEnums.AuthMode.DEMOGRAPHICS)")
    @Mapping(target="patientAddress", source = "data.patientAbhaAddress")
    LinkCareContextDTO mapToLinkCareContextDTO(HealthDocumentLinkingDTO data,
                                                                 String careContextReference,
                                                                 String appointmentReference,
                                                                 String requestId);

    /**
     * Formats a date/time string into 12-hour AM/PM format.
     *
     * @param dateTime Date/time string in ISO format
     * @return Formatted time or "Invalid date" if parsing fails
     */
    @Named("formatDate")
    default String formatDate(String dateTime) {
        try {
            OffsetDateTime date = OffsetDateTime.parse(dateTime);
            return formatTimeToAmPm(date.toLocalTime());
        } catch (DateTimeParseException e) {
            try {
                LocalDate localDate = LocalDate.parse(dateTime, DateTimeFormatter.ISO_LOCAL_DATE);
                OffsetDateTime date = localDate.atStartOfDay(ZoneOffset.UTC).toOffsetDateTime();
                return formatTimeToAmPm(date.toLocalTime());
            } catch (DateTimeParseException e2) {
                return "Invalid date";
            }
        }
    }

    /**
     * Converts a {@link LocalTime} to 12-hour AM/PM format.
     *
     * @param time {@link LocalTime} to format
     * @return Formatted time in "hh:mm AM/PM" format
     */
    default String formatTimeToAmPm(LocalTime time) {
        return time.format(DateTimeFormatter.ofPattern("hh:mm a"));
    }

    /**
     * Retrieves health records from the source data.
     *
     * @param data {@link HealthDocumentLinkingDTO} source object
     * @return List of {@link HealthInformationDTO} or an empty list if none exist
     */
    default List<HealthInformationDTO> getHealthRecords(HealthDocumentLinkingDTO data) {
        return data.getHealthRecords() != null ? data.getHealthRecords() : new ArrayList<>();
    }

    /**
     * Retrieves the mobile number from the source data.
     *
     * @param data {@link HealthDocumentLinkingDTO} source object
     * @return Mobile number if present, or an empty string if null
     */
    default String getMobileNumber(HealthDocumentLinkingDTO data) {
        return data.getMobileNumber() != null ? data.getMobileNumber() : "";
    }
}
