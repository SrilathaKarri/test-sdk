package carestack.organization.validators;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import carestack.organization.dto.UploadDocuments;

/**
 * Custom validator for validating photos in the {@link UploadDocuments} DTO.
 * <p>This class validates the presence and properties of photos such as board and building photos.</p>
 * It checks whether the photos are provided and whether they meet the file validation criteria.
 */
public class PhotoValidator implements ConstraintValidator<ValidPhotos, UploadDocuments> {

    /**
     * Validates the {@link UploadDocuments} object, ensuring that board and building photos are valid.
     * <p>This method will invoke the method to check each photo for validity.
     * If any photo is missing or invalid, a constraint violation will be added to the validation context.</p>
     *
     * @param uploadDocuments The {@link UploadDocuments} object containing the photos to validate.
     * @param context The context in which the constraint is validated.
     * @return {@code true} if both board and building photos are valid, {@code false} otherwise.
     */
    @Override
    public boolean isValid(UploadDocuments uploadDocuments, ConstraintValidatorContext context) {
        if (uploadDocuments == null) {
            context.buildConstraintViolationWithTemplate("Photo is required").addConstraintViolation();
            return false;
        }

        boolean isValid = true;

        // Disable the default violation messages and add custom messages as needed
        context.disableDefaultConstraintViolation();

        // Validate Board Photo
        try {
            FileValidator.validateDocument(uploadDocuments.getBoardPhoto(), "Board Photo");
        } catch (IllegalArgumentException e) {
            context.buildConstraintViolationWithTemplate(e.getMessage())
                    .addPropertyNode("boardPhoto")
                    .addConstraintViolation();
            isValid = false;
        }

        // Validate Building Photo
        try {
            FileValidator.validateDocument(uploadDocuments.getBuildingPhoto(), "Building Photo");
        } catch (IllegalArgumentException e) {
            context.buildConstraintViolationWithTemplate(e.getMessage())
                    .addPropertyNode("buildingPhoto")
                    .addConstraintViolation();
            isValid = false;
        }

        return isValid;
    }
}
