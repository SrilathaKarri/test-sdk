package carestack.base.DTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;
import org.springframework.format.annotation.DateTimeFormat;
import carestack.base.enums.Gender;
import carestack.base.enums.StatesAndUnionTerritories;

/**
 * DTO (Data Transfer Object) for filtering search requests.
 * <p>
 * This class is used to encapsulate various search criteria for retrieving patient data.
 * It includes validation constraints to ensure data integrity.
 * </p>
 */
@Getter
@Setter
public class SearchFiltersDTO {

    /**
     * First name of the patient.
     * <p>
     * Must contain only letters (a-z, A-Z) and dots (.). The Minimum length is 3 characters,
     *  and the maximum length is 20 characters.
     * </p>
     */
    @Pattern(regexp = "^[a-zA-Z.]+$", message = "First name can only contain letters and dots")
    @Length(min = 3, max = 20, message = "First name must be between 3 and 20 characters")
    private String firstName;

    /**
     * Last name.
     * <p>
     * Must contain only letters (a-z, A-Z) and dots (.). The Minimum length is 3 characters,
     *  and the maximum length is 20 characters.
     * </p>
     */
    @Pattern(regexp = "^[a-zA-Z.]+$", message = "Last name can only contain letters and dots")
    @Length(min = 3, max = 20, message = "Last name must be between 3 and 20 characters")
    private String lastName;

    /**
     * Date of birth in ISO format (YYYY-MM-DD).
     */
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private String birthDate;

    /**
     * Gender (MALE, FEMALE, OTHER, UNKNOWN).
     */
    private Gender gender;

    /**
     * Phone number.
     * <p>
     * Can contain only digits and an optional leading '+' sign.
     * </p>
     */
    @Pattern(regexp = "^[+]?[0-9]*$", message = "Invalid phone number")
    private String phone;

    /**
     * State or Union Territory.
     */
    private StatesAndUnionTerritories state;

    /**
     * Pin code of the address.
     * <p>
     * Must be a valid 6-digit Indian pin code, starting with a digit from 1-9.
     * </p>
     */
    @Pattern(regexp = "^[1-9][0-9]{5}$", message = "Invalid pin code")
    private String pincode;

    /**
     * Email ID.
     * <p>
     * Must follow the standard email format (e.g., user@example.com).
     * </p>
     */
    @Email(message = "Invalid email format")
    private String emailId;

    /**
     * Organization ID.
     */
    private String organizationId;

    /**
     * Registration ID.
     */
    private String registrationId;

    /**
     * The number of records to fetch in the search result.
     * <p>
     * Must be a positive integer.
     * </p>
     */
    @Pattern(regexp = "^[0-9]+$", message = "Count must be a positive number")
    private String count;

    /**
     * Unique identifier.
     */
    private String identifier;

    /**
     * Start date for filtering records.
     * <p>
     * The date must follow the format YYYY-MM-DD and should not be in the future.
     * </p>
     */
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "Invalid date format (expected YYYY-MM-DD)")
    private String fromDate;

    /**
     * End date for filtering records.
     * <p>
     * The date must follow the format YYYY-MM-DD and should not be in the future.
     * It must be greater than or equal to {@code fromDate}.
     * </p>
     */
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "Invalid date format (expected YYYY-MM-DD)")
    private String toDate;


}
