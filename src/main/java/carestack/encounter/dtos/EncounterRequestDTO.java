package carestack.encounter.dtos;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import carestack.base.enums.CaseType;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Represents a request to the {@link carestack.encounter.Encounter} service.
 * <p>
 * This DTO is the primary input for creating an encounter. It specifies the
 * {@link CaseType} and provides the actual data in the {@code dto} field.
 * The data can be either a list of file URLs for AI extraction or a structured
 * JSON payload for direct processing. It also supports an optional list of
 * lab report URLs.
 * </p>
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class EncounterRequestDTO {

    /**
     * The type of clinical case, which determines how the data is processed. This field is mandatory.
     */
    @NotNull(message = "Case type cannot be null")
    private CaseType caseType;

    /**
     * The main data object for the request. This is a flexible field that can hold
     * either a Map containing a 'payload' (structured JSON) or 'caseSheets' (file URLs).
     */
    private Object dto;

    /**
     * An optional list of URLs pointing to supporting lab report documents.
     */
    @JsonAlias("labReports")
    private List<String> labReports;

    /**
     * A convenience method to safely extract the 'payload' object from the generic {@code dto} field.
     *
     * @return An {@link Optional} containing the payload if it exists, otherwise an empty Optional.
     */
    public Optional<Object> getPayload() {
        if (dto instanceof Map<?, ?> map && map.containsKey("payload")) {
            return Optional.ofNullable(map.get("payload"));
        }
        return Optional.empty();
    }

    /**
     * A convenience method to safely extract the 'caseSheets' list from the generic {@code dto} field.
     *
     * @return An {@link Optional} containing the list of case sheet URLs if it exists, otherwise an empty Optional.
     */
    @SuppressWarnings("unchecked")
    public Optional<List<String>> getCaseSheets() {
        if (dto instanceof Map<?, ?> map && map.containsKey("caseSheets")) {
            try {
                return Optional.ofNullable((List<String>) map.get("caseSheets"));
            } catch (ClassCastException e) {
                // The value associated with "caseSheets" is not a List<String>, return empty.
                return Optional.empty();
            }
        }
        return Optional.empty();
    }
}