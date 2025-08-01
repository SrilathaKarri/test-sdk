package carestack.organization.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * Data Transfer Object (DTO) for representing the specialties of an organization.
 * <p>This class is used to capture the system of medicine and the list of specialties associated with the organization.</p>
 */
@Data
public class Speciality {

    /**
     * The code for the system of medicine.
     * <p>This field is used to specify the system of medicine that the organization follows (e.g., Allopathy, Homeopathy, Ayurveda).</p>
     * <p>This field is required.</p>
     */
    @NotNull
    @JsonProperty("systemofMedicineCode")
    private String systemOfMedicineCode;

    /**
     * A list of specialities offered by the organization.
     * <p>This field holds the specialties that the organization specializes in. The list must contain at least one speciality.</p>
     * <p>This field is required and should contain at least one element.</p>
     */
    @NotNull
    @Size(min = 1, message = "At least one speciality must be provided")
    private List<String> specialities;
}
