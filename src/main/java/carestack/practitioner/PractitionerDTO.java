package carestack.practitioner;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import carestack.base.enums.Gender;
import carestack.base.enums.ResourceType;
import carestack.base.enums.StatesAndUnionTerritories;


/**
 * Data Transfer Object (DTO) representing a Practitioner.
 * <p>
 * This class encapsulates all the necessary information required to create or update a practitioner's
 * details in the system. It includes personal details, professional information, and validation
 * constraints to ensure data integrity.
 * </p>
 *
 * <p>Uses Lombok annotations to generate boilerplate code like getters, setters, constructors, and builders.</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PractitionerDTO {

    /**
     * Unique registration ID of the practitioner.
     * Must not be blank.
     */
    @NotBlank(message = "Registration ID is required.")
    private String registrationId;

    /**
     * Department where the practitioner is assigned.
     * Must not be blank.
     */
    @NotBlank(message = "Department is required.")
    private String department;

    /**
     * Designation of the practitioner within the department.
     * Must not be blank.
     */
    @NotBlank(message = "Designation is required.")
    private String designation;

    /**
     * Current status of the practitioner (e.g., active, inactive).
     * Must not be blank.
     */
    @NotBlank(message = "Status is required.")
    private String status;

    /**
     * Date when the practitioner joined the organization.
     * Must not be null.
     */
    @NotNull(message = "Joining Date is required.")
    private String joiningDate;

    /**
     * Staff type of the practitioner (e.g., full-time, part-time).
     * Must not be blank.
     */
    @NotBlank(message = "Staff Type is required.")
    private String staffType;

    /**
     * First name of the practitioner.
     * Must not be blank and at least 3 characters long.
     */
    @NotBlank(message = "First Name is required.")
    @Size(min = 3, message = "First Name must be at least 3 characters long")
    private String firstName;

    /**
     * Middle name of the practitioner.
     * This field is optional.
     */
    private String middleName;

    /**
     * Last name of the practitioner.
     * Must not be blank and at least 3 characters long.
     */
    @NotBlank(message = "Last Name is required.")
    @Size(min = 3, message = "Last Name must be at least 3 characters long")
    private String lastName;

    /**
     * Birthdate of the practitioner in YYYY-MM-DD format.
     * Must not be null and match the specified date pattern.
     */
    @NotNull(message = "Birth Date is required.")
    @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}", message = "Birth Date must be in YYYY-MM-DD format")
    private String birthDate;

    /**
     * Gender of the practitioner.
     * Must not be null. Defaults to MALE if not specified.
     */
    @NotNull(message = "Gender is required.")
    private Gender gender = Gender.MALE;

    /**
     * Mobile number of the practitioner.
     * Must not be blank and match the pattern for valid Indian mobile numbers.
     */
    @NotBlank(message = "Mobile Number is required.")
    @Pattern(regexp = "^[+]91[987]\\d{9}$", message = "Mobile number must start with +91 followed by 9, 8, or 7 and be exactly 10 digits")
    private String mobileNumber;

    /**
     * Email ID of the practitioner.
     * Must not be blank, follow a valid email format, and match additional email validation rules.
     */
    @NotBlank(message = "Email ID is required.")
    @Email(message = "Invalid email format.")
    private String emailId;

    /**
     * Address of the practitioner.
     * Must not be blank and at least 5 characters long.
     */
    @NotBlank(message = "Address is required.")
    @Size(min = 5, message = "Address must be at least 5 characters long")
    private String address;

    /**
     * Pincode of the practitioner's location.
     * Must not be blank and exactly 6 digits.
     */
    @NotBlank(message = "Pincode is required.")
    @Pattern(regexp = "\\d{6}", message = "Pincode must be 6 digits")
    private String pincode;

    /**
     * State or Union Territory where the practitioner resides.
     * Must not be null.
     */
    @NotNull(message = "State is required.")
    private StatesAndUnionTerritories state;

    /**
     * Indicates if the practitioner wants to link their account with WhatsApp.
     * This field is optional.
     */
    @NotNull(message = "wantsToLinkWhatsapp is required")
    private Boolean wantsToLinkWhatsapp;

    /**
     * URL or path to the practitioner's photo.
     * This field is optional.
     */
    private String photo;

    /**
     * The resource type associated with the practitioner (e.g.,patient).
     * Should match the ResourceType enum values
     * Must not be null.
     */
    @NotNull(message = "Resource Type is required.")
    private ResourceType resourceType;

    /**
     * Unique resource ID for the practitioner (optional).
     */
    private String resourceId;
}
