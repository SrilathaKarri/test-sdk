package carestack.encounter.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a single medical or surgical procedure.
 * <p>
 * This DTO captures a description of the procedure and any associated complications.
 * The setter for complications ensures that empty strings are converted to null.
 * </p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProcedureItem {

    /**
     * A description of the procedure performed (e.g., "Coronary Artery Bypass Graft").
     * This field is mandatory.
     */
    @NotBlank(message = "Description cannot be empty.")
    @JsonProperty(required = true)
    private String description;

    /**
     * Any complications that occurred during or after the procedure. This field is optional.
     */
    private String complications;

    /**
     * Sets the complications, converting an empty or whitespace-only string to null.
     * @param complications The complications text.
     */
    public void setComplications(String complications) {
        this.complications = (complications != null && complications.trim().isEmpty()) ? null : complications;
    }
}