package carestack.encounter.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents the details of a doctor or practitioner.
 * <p>
 * This DTO is used to capture identifying information such as the doctor's name,
 * designation, and department.
 * </p>
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class DoctorDetails {
    /**
     * The full name of the doctor.
     */
    @JsonProperty("Name")
    private String name;

    /**
     * The doctor's official title or designation (e.g., "Consultant Cardiologist").
     */
    @JsonProperty("Designation")
    private String designation;

    /**
     * The department the doctor belongs to (e.g., "Cardiology").
     */
    @JsonProperty("Department")
    private String department;
}