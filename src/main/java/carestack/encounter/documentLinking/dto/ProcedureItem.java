package carestack.encounter.documentLinking.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a single medical or surgical procedure.
 * <p>
 * This DTO captures a description of the procedure and any associated complications.
 * </p>
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProcedureItem {

    /**
     * A description of the procedure performed (e.g., "Coronary Artery Bypass Graft").
     * This field is mandatory.
     */
    @NotBlank
    private String description;

    /**
     * Any complications that occurred during or after the procedure.
     * This field is optional.
     */
    private String complications;
}