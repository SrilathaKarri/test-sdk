package carestack.practitioner.hpr.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Represents the successful response after creating a new Healthcare Professional Registry (HPR) ID.
 * <p>
 * This DTO contains all the details of the newly created HPR account, including the
 * HPR ID, HPR Number, authentication token, and the professional's demographic information.
 * </p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateHprIdWithPreVerifiedResponse {

        /**
         * The authentication token for the newly created session.
         */
        private String token;

        /**
         * The unique, system-generated HPR Number for the professional.
         */
        @JsonProperty("hprIdNumber")
        private String hprIdNumber;

        /**
         * The full name of the professional.
         */
        @JsonProperty("name")
        private String name;

        /**
         * The gender of the professional.
         */
        @JsonProperty("gender")
        private String gender;

        /**
         * The year of birth of the professional.
         */
        @JsonProperty("yearOfBirth")
        private String yearOfBirth;

        /**
         * The month of birth of the professional.
         */
        @JsonProperty("monthOfBirth")
        private String monthOfBirth;

        /**
         * The day of birth of the professional.
         */
        @JsonProperty("dayOfBirth")
        private String dayOfBirth;

        /**
         * The first name of the professional.
         */
        @JsonProperty("firstName")
        private String firstName;

        /**
         * The user-chosen HPR ID (username).
         */
        @JsonProperty("hprId")
        private String hprId;

        /**
         * The last name of the professional.
         */
        @JsonProperty("lastName")
        private String lastName;

        /**
         * The middle name of the professional.
         */
        @JsonProperty("middleName")
        private String middleName;

        /**
         * The code for the state of residence.
         */
        @JsonProperty("stateCode")
        private String stateCode;

        /**
         * The code for the district of residence.
         */
        @JsonProperty("districtCode")
        private String districtCode;

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
         * The professional's email address.
         */
        @JsonProperty("email")
        private String email;

        /**
         * A URL or reference to the professional's KYC (Know Your Customer) photo.
         */
        @JsonProperty("kycPhoto")
        private String kycPhoto;

        /**
         * The professional's verified mobile number.
         */
        @JsonProperty("mobile")
        private String mobile;

        /**
         * The ID for the professional's primary category.
         */
        @JsonProperty("categoryId")
        private Integer categoryId;

        /**
         * The ID for the professional's subcategory or specialty.
         */
        @JsonProperty("subCategoryId")
        private Integer subCategoryId;

        /**
         * A list of authentication methods available for the account (e.g., "OTP", "BIOMETRIC").
         */
        @JsonProperty("authMethods")
        private List<String> authMethods;

        /**
         * A flag indicating if this is a new account creation. Should be {@code true} in this context.
         */
        @JsonProperty("new")
        private Boolean isNew;
}