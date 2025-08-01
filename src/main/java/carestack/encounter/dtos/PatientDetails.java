package carestack.encounter.dtos;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents the demographic and administrative details of a patient.
 * <p>
 * This DTO is used within various health record DTOs (e.g., {@link DischargeSummarySections})
 * to provide context about the patient involved in the encounter.
 * </p>
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PatientDetails {
    /**
     * The full name of the patient.
     */
    @JsonProperty("Name")
    private String name;

    /**
     * The age of the patient (e.g., "35Y").
     */
    @JsonProperty("Age")
    private String age;

    /**
     * The gender of the patient (e.g., "Male", "Female").
     */
    @JsonProperty("Sex")
    private String sex;

    /**
     * The patient's date of birth, formatted as MM/dd/yyyy.
     */
    @JsonProperty("Date of Birth")
    @JsonFormat(pattern = "MM/dd/yyyy")
    private String dateOfBirth;

    /**
     * The date of admission for an in-patient encounter, formatted as MM/dd/yyyy.
     */
    @JsonProperty("Date of Admission")
    @JsonFormat(pattern = "MM/dd/yyyy")
    private String dateOfAdmission;

    /**
     * The patient's residential address.
     */
    @JsonProperty("Address")
    private String address;

    /**
     * The patient's contact phone number.
     */
    @JsonProperty("Contact Number")
    private String contactNumber;

    /**
     * The patient's Unique Health Identifier (UHID).
     */
    @JsonProperty("UHID")
    private String uhid;

    /**
     * The In-Patient (IP) number for an admission.
     */
    @JsonProperty("IP Number")
    private String ipNumber;

    /**
     * The marital status of the patient (e.g., "Married", "Single").
     */
    @JsonProperty("Marital Status")
    private String maritalStatus;
}