package carestack.documentLinking.dto;

import lombok.Data;
import carestack.base.utils.StringUtils;

/**
 * Represents the state of a document linking transaction.
 * <p>
 * This class holds all relevant identifiers and flags that track the progress
 * of the multistep document linking process. Its {@code toString()} method masks
 * sensitive data for safe logging, which is essential for debugging failed transactions.
 * </p>
 */
@Data
public class TransactionState {

    /**
     * The initial request DTO, stored to access its data in later steps of the transaction.
     */
    private HealthDocumentLinkingDTO healthDocumentLinkingDTO;

    /**
     * The reference ID for the care context, generated during the transaction.
     */
    private String careContextReference;

    /**
     * The reference ID for the appointment associated with the transaction.
     */
    private String appointmentReference;

    /**
     * The unique ID for the entire transaction request.
     */
    private String requestId;

    /**
     * A flag indicating whether the care context was successfully created.
     */
    private boolean careContextCreated = false;

    /**
     * A flag indicating whether the visit records were successfully updated.
     */
    private boolean visitRecordsUpdated = false;

    /**
     * A flag indicating whether the care context was successfully linked.
     */
    private boolean careContextLinked = false;

    /**
     * Masks sensitive data for safe logging.
     *
     * @param data The string to mask.
     * @return A masked version of the string.
     */
    private String maskData(String data) {
        if (StringUtils.isNullOrEmpty(data)) return "null";
        if (data.length() <= 4) return "*".repeat(data.length());
        return data.substring(0, 2) + "*".repeat(data.length() - 4) + data.substring(data.length() - 2);
    }

    @Override
    public String toString() {
        return String.format(
                "TransactionState[careContextReference=%s, appointmentReference=%s, requestId=%s, careContextCreated=%b, visitRecordsUpdated=%b, careContextLinked=%b]",
                maskData(careContextReference),
                maskData(appointmentReference),
                maskData(requestId),
                careContextCreated,
                visitRecordsUpdated,
                careContextLinked
        );
    }
}