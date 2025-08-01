package carestack.organization.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import carestack.organization.validators.ValidPhotos;


/**
 * Represents a set of documents required for organization verification or registration.
 * This class ensures that both the board photo and building photo are provided.
 */
@Data
@ValidPhotos
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UploadDocuments {

    /**
     * Board photo of the organization.
     * Expected format: JPG, PNG.
     * File size must be less than 1MB.
     */
    @NotNull(message = "Board photo is required.")
    private Document boardPhoto;

    /**
     * Building photo of the organization.
     * Expected format: JPG, PNG.
     * File size must be less than 1MB.
     */
    @NotNull(message = "Building photo is required.")
    private Document buildingPhoto;


}



