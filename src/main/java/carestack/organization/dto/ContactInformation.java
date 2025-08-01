package carestack.organization.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * DTO for representing the contact information of an organization.
 * Contains fields for mobile number, email, landline, STD code, and website link.
 */
@Data
public class ContactInformation {

    /**
     * Mobile number of the organization contact person.
     * The mobile number must be 10 digits long and start with 9, 8, or 7.
     * This field is required.
     */
    @NotBlank(message = "Mobile Number is required")
    @Pattern(regexp = "[987]\\d{9}",
            message = "Mobile number must start with 9, 8, or 7 and be exactly 10 digits")
    private String mobileNumber;

    /**
     * Email address of the organization contact person.
     * This field is required and must follow a valid email format.
     */
    @NotBlank(message = "Email ID is required")
    @Email(message = "Invalid email format")
    private String email;

    /**
     * Landline number of the organization.
     * The landline number must contain between 6 and 12 digits.
     * This field is required.
     */
    @NotBlank(message = "landline is required")
    @Pattern(regexp = "^[0-9]{6,12}$", message = "Please enter a valid landline number")
    private String landline;

    /**
     * STD code of the organization.
     * The STD code must contain between 2 and 5 digits.
     * This field is required.
     */
    @NotBlank(message = "stdCode is required")
    @Pattern(regexp = "^[0-9]{2,5}$", message = "Please enter a valid STD code")
    private String stdcode;

    /**
     * Website URL of the organization.
     * The URL must follow a valid format for either HTTP, HTTPS, or FTP.
     * This field is required.
     */
    @NotBlank(message = "website link is required")
    @Pattern(
            regexp = "^(https?|ftp)://[^\\s/$.?#].[^\\s]*$",
            message = "Please enter a valid website URL"
    )
    private String websiteLink;
}
