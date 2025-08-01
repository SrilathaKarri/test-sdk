package carestack.ai.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import carestack.base.enums.CaseType;

import java.util.List;

/**
 * Data Transfer Object (DTO) for processing discharge summary (DS) requests.
 * This DTO is used for processing requests related to discharge summaries,
 * containing a list of file URLs, an optional public key for encryption,
 * and other related metadata.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProcessDSDto {

    /**
     * A list of file URLs representing the discharge summary files.
     * This field cannot be null.
     */
    @NotNull(message = "Files list cannot be null")
    private List<String> files;

    /**
     * The public key used for encryption, if needed.
     * This field is optional and may be null.
     */
    private String publicKey;

    /**
     * Encrypted data related to the discharge summary.
     * This field is optional and may be null.
     */
    private String encryptedData;

    /**
     * The type of case associated with the discharge summary.
     * This field is optional and may be null.
     */
    private CaseType caseType;

    /**
     * Custom string representation of the ProcessDSDto object.
     * This method provides a string representation of the object, but hides sensitive information such as
     * publicKey and encryptedData by displaying "[PROTECTED]" and "[ENCRYPTED]" instead of the actual values.
     *
     * @return A string representation of the ProcessDSDto object.
     */
    @Override
    public String toString() {
        return "ProcessDSDto{" +
                "files=" + files +
                ", publicKey='" + (publicKey != null ? "[PROTECTED]" : "null") + '\'' +
                ", encryptedData='" + (encryptedData != null ? "[ENCRYPTED]" : "null") + '\'' +
                ", caseType=" + caseType +
                '}';
    }

    /**
     * Constructor for creating a ProcessDSDto with a list of files and a public key.
     * This constructor can be used when only files and a public key are required, excluding encrypted data and case type.
     *
     * @param files     A list of discharge summary file URLs.
     * @param publicKey The public key for encryption, if needed.
     */
    public ProcessDSDto(List<String> files, String publicKey) {
        this.files = files;
        this.publicKey = publicKey;
    }
}
