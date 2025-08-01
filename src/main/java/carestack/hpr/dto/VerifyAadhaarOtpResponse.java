package carestack.hpr.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents the detailed response after successful Aadhaar OTP verification.
 * <p>
 * This DTO contains the demographic data of the user as registered with their
 * Aadhaar, which can be used to pre-fill registration forms and confirm the user's identity.
 * </p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VerifyAadhaarOtpResponse {

        /**
         * The unique transaction identifier (txnId) for the session.
         */
        private String txnId;

        /**
         * The user's Aadhaar-linked mobile number, masked for privacy.
         */
        private String mobileNumber;

        /**
         * A base64-encoded string of the user's profile photo from Aadhaar.
         */
        private String photo;

        /**
         * The gender of the user.
         */
        private String gender;

        /**
         * The full name of the user as per Aadhaar records.
         */
        private String name;

        /**
         * The user's email address, if available in Aadhaar records.
         */
        private String email;

        /**
         * The postal pincode from the user's address.
         */
        private String pincode;

        /**
         * The user's date of birth.
         */
        private String birthDate;

        /**
         * The "Care Of" person's name from the user's address (e.g., father's name).
         */
        private String careOf;

        /**
         * The house number or name from the user's address.
         */
        private String house;

        /**
         * The street name from the user's address.
         */
        private String street;

        /**
         * A landmark near the user's address.
         */
        private String landmark;

        /**
         * The locality or area from the user's address.
         */
        private String locality;

        /**
         * The village, town, or city from the user's address.
         */
        private String villageTownCity;

        /**
         * The subdistrict from the user's address.
         */
        private String subDist;

        /**
         * The district from the user's address.
         */
        private String district;

        /**
         * The state from the user's address.
         */
        private String state;

        /**
         * The post office from the user's address.
         */
        private String postOffice;

        /**
         * The full, combined address string.
         */
        private String address;
}