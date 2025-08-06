package carestack.practitioner.hpr.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents the response from the account existence check when no HPR account is found.
 * <p>
 * This DTO contains the user's demographic data as retrieved from their Aadhaar
 * details, which can then be used to pre-fill the HPR registration form.
 * </p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NonHprAccountResponse {

        /**
         * The unique transaction identifier (txnId) for the session.
         */
        private String txnId;

        /**
         * An authentication token for the current session.
         */
        private String token;

        /**
         * The HPR Number. Will be null in this context as no account exists.
         */
        @JsonProperty("hprIdNumber")
        private String hprIdNumber;

        /**
         * The full name of the user.
         */
        @JsonProperty("name")
        private String name;

        /**
         * The first name of the user.
         */
        @JsonProperty("firstName")
        private String firstName;

        /**
         * The last name of the user.
         */
        @JsonProperty("lastName")
        private String lastName;

        /**
         * The middle name of the user.
         */
        @JsonProperty("middleName")
        private String middleName;

        /**
         * The gender of the user.
         */
        private String gender;

        /**
         * The year of birth of the user.
         */
        @JsonProperty("yearOfBirth")
        private String yearOfBirth;

        /**
         * The month of birth of the user.
         */
        @JsonProperty("monthOfBirth")
        private String monthOfBirth;

        /**
         * The day of birth of the user.
         */
        @JsonProperty("dayOfBirth")
        private String dayOfBirth;

        /**
         * The code for the district of residence.
         */
        @JsonProperty("districtCode")
        private String districtCode;

        /**
         * The code for the state of residence.
         */
        @JsonProperty("stateCode")
        private String stateCode;

        /**
         * The name of the state of residence.
         */
        @JsonProperty("stateName")
        private String stateName;

        /**
         * The name of the district of residence.
         */
        @JsonProperty("districtName")
        private String districtName;

        /**
         * The user's full residential address.
         */
        private String address;

        /**
         * The postal pin code of the residence.
         */
        private String pincode;

        /**
         * A base64-encoded string of the user's profile photo from Aadhaar.
         */
        @JsonProperty("profilePhoto")
        private String profilePhoto;

        /**
         * The professional category ID. Will be null in this context.
         */
        @JsonProperty("categoryId")
        private Integer categoryId;

        /**
         * The professional subcategory ID. Will be null in this context.
         */
        @JsonProperty("subCategoryId")
        private Integer subCategoryId;

        /**
         * A flag indicating if this is a new account. Will be {@code true} in this context.
         */
        @JsonProperty("new")
        private Boolean isNew;
}