package carestack.practitioner.hpr.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents the response from the account existence check when an HPR account is found.
 * <p>
 * This DTO provides details of the existing HPR account, allowing the application
 * to inform the user or link to the existing profile.
 * </p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HprAccountResponse {

        /**
         * An authentication token for the existing account session.
         */
        private String token;

        /**
         * The unique, system-generated HPR Number of the existing account.
         */
        @JsonProperty("hprIdNumber")
        private String hprIdNumber;

        /**
         * The user-chosen HPR ID (username) of the existing account.
         */
        @JsonProperty("hprId")
        private String hprId;

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
         * A flag indicating if this is a new account. Will be {@code false} in this context.
         */
        @JsonProperty("new")
        private Boolean isNew;

        /**
         * The name of the professional's primary category.
         */
        @JsonProperty("categoryName")
        private String categoryName;

        /**
         * The name of the professional's subcategory or specialty.
         */
        @JsonProperty("categorySubName")
        private String categorySubName;
}