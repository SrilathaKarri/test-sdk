package carestack.patient.DTO;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import carestack.base.enums.Gender;
import carestack.base.enums.ResourceType;
import carestack.base.enums.StatesAndUnionTerritories;
import carestack.patient.enums.PatientIdType;
import carestack.patient.enums.PatientType;

/**
 * Data Transfer Object (DTO) class representing a patient.
 * This class holds the information related to a patient, such as their ID, name, gender, contact information, and more.
 * It is used for transferring patient-related data between layers in the application.
 * <p>
 * Validations are applied to the fields to ensure that the provided data meets specific criteria.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PatientDTO {

    /**
     * Patient's ID number.
     * This is a required field.
     */
    @NotBlank(message = "ID Number is required")
    private String idNumber;

    /**
     * The type of patient ID used (e.g., ABHA, AADHAAR).
     * This is a required field.
     */
    @NotNull(message = "ID Type is required")
    private PatientIdType idType;

    /**
     * The ABHA address associated with the patient.
     * This is a required field.
     */
    @NotBlank(message = "ABHA Address is required")
    private String abhaAddress;

    /**
     * The type of patient (e.g., NEW or OLD).
     * This is a required field.
     */
    @NotNull(message = "Patient Type is required")
    private PatientType patientType;

    /**
     * The first name of the patient.
     * It is required and must have at least 3 characters.
     */
    @NotBlank(message = "First Name is required")
    @Size(min = 3, message = "First Name must be at least 3 characters long")
    private String firstName;

    /**
     * The middle name of the patient (optional).
     */
    private String middleName;

    /**
     * The last name of the patient.
     * It is required and must have at least 3 characters.
     */
    @NotBlank(message = "Last Name is required")
    @Size(min = 3, message = "Last Name must be at least 3 characters long")
    private String lastName;

    /**
     * The birthdate of the patient in YYYY-MM-DD format.
     * It is a required field and must match the specified pattern.
     */
    @NotBlank(message = "Birth Date is required")
    @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}", message = "Birth Date must be in YYYY-MM-DD format")
    private String birthDate;

    /**
     * The gender of the patient.
     * It is a required field.
     */
    @NotNull(message = "Gender is required")
    private Gender gender;

    /**
     * The email ID of the patient.
     * It is required and must follow a valid email format.
     */
    @NotBlank(message = "Email ID is required")
    @Email(message = "Invalid email format")
    private String emailId;

    /**
     * The mobile number of the patient.
     * It must start with +91 followed by 9, 8, or 7 and be exactly 10 digits.
     */
    @NotBlank(message = "Mobile Number is required")
    @Pattern(regexp = "^[+]91[987]\\d{9}$", message = "Mobile number must start with +91 followed by 9, 8, or 7 and be exactly 10 digits")
    private String mobileNumber;

    /**
     * The address of the patient.
     * It is a required field and must be at least 5 characters long.
     */
    @NotBlank(message = "Address is required")
    @Size(min = 5, message = "Address must be at least 5 characters long")
    private String address;

    /**
     * The pincode of the patient's address.
     * It is a required field and must consist of exactly 6 digits.
     */
    @NotBlank(message = "Pincode is required")
    @Pattern(regexp = "\\d{6}", message = "Pincode must be 6 digits")
    private String pincode;

    /**
     * The state of the patient.
     * This is a required field.
     */
    @NotNull(message = "State is required")
    private StatesAndUnionTerritories state;

    /**
     * Indicates whether the patient wants to link a WhatsApp number (optional).
     */
    @NotNull(message = "wantsToLinkWhatsapp is required")
    private Boolean wantsToLinkWhatsapp;

    /**
     * The photo of the patient (optional).
     */
    private String photo;

    /**
     * The type of resource (e.g., Patient).
     * This is a required field.
     */
    @NotNull(message = "Resource Type is required")
    private ResourceType resourceType;

    /**
     * The resource ID for the patient (optional).
     */
    private String resourceId;
}
