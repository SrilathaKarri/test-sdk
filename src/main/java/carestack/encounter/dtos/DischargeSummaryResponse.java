package carestack.encounter.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Represents the response from the AI service after processing a document for data extraction.
 * <p>
 * This DTO contains the structured data extracted from an unstructured
 * document, such as a discharge summary file. The data is represented as a map
 * of key-value pairs.
 * </p>
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DischargeSummaryResponse {

    /**
     * A map containing the structured data extracted from the source document.
     * The keys represent the field names (e.g., "chiefComplaints"), and the values
     * are the extracted information.
     */
    private Map<String, Object> extractedData;
}