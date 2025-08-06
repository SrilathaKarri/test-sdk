package carestack.practitioner.hpr.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents the final request to create a Healthcare Professional Registry (HPR) ID
 * using pre-verified data from the preceding workflow steps.
 * <p>
 * This DTO aggregates all the necessary demographic, professional, and contact
 * information required to register a new healthcare professional.
 * </p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateHprIdWithPreVerifiedRequest {

        /**
         * The full residential address of the professional. This field is mandatory.
         */
        @NotBlank(message = "Address cannot be empty")
        private String address;

        /**
         * The day of the month the professional was born (e.g., "15"). This field is mandatory.
         */
        @NotBlank(message = "Day of birth cannot be empty")
        @Pattern(regexp = "^([1-9]|[12][0-9]|3[01])$", message = "Day of birth must be between 1 and 31")
        private String dayOfBirth;

        /**
         * The code for the district of residence. This field is mandatory.
         */
        @NotBlank(message = "District code cannot be empty")
        private String districtCode;

        /**
         * The professional's email address. This field is mandatory and must be a valid email format.
         */
        @NotBlank(message = "Email cannot be empty")
        @Email(message = "Invalid email format")
        private String email;

        /**
         * The first name of the professional. This field is mandatory.
         */
        @NotBlank(message = "First name cannot be empty")
        private String firstName;

        /**
         * The code for the healthcare professional's primary category (e.g., "HP01" for Doctors).
         * This field is mandatory.
         */
        @NotBlank(message = "HP category code cannot be empty")
        private String hpCategoryCode;

        /**
         * The code for the healthcare professional's subcategory or specialty. This field is mandatory.
         */
        @NotBlank(message = "HP subcategory code cannot be empty")
        private String hpSubCategoryCode;

        /**
         * The user-chosen HPR ID (username) from the suggested list. This field is mandatory.
         */
        @NotBlank(message = "HPR ID cannot be empty")
        private String hprId;

        /**
         * The last name of the professional. This field is mandatory.
         */
        @NotBlank(message = "Last name cannot be empty")
        private String lastName;

        /**
         * The middle name of the professional. This field is optional.
         */
        private String middleName;

        /**
         * The month of the year the professional was born (e.g., "7" for July). This field is mandatory.
         */
        @NotBlank(message = "Month of birth cannot be empty")
        @Pattern(regexp = "^([1-9]|1[0-2])$", message = "Month of birth must be between 1 and 12")
        private String monthOfBirth;

        /**
         * The password for the new HPR account. This field is mandatory.
         */
        @NotBlank(message = "Password cannot be empty")
        private String password;

        /**
         * The postal pincode of the residence. This field is mandatory.
         */
        @NotBlank(message = "Pincode cannot be empty")
        private String pincode;

        /**
         * A base64-encoded string of the professional's profile photo. This field is optional.
         */
        private String profilePhoto;

        /**
         * The code for the state of residence. This field is mandatory.
         */
        @NotBlank(message = "State code cannot be empty")
        private String stateCode;

        /**
         * The unique transaction identifier (txnId) from the registration workflow. This field is mandatory.
         */
        @NotBlank(message = "Transaction ID cannot be empty")
        private String txnId;

        /**
         * The year the professional was born (e.g., "1990"). This field is mandatory.
         */
        @NotBlank(message = "Year of birth cannot be empty")
        @Pattern(regexp = "\\d{4}", message = "Year of birth must be exactly 4 digits")
        private String yearOfBirth;
}