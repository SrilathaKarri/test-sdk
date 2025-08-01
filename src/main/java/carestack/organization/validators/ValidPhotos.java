package carestack.organization.validators;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import carestack.organization.dto.UploadDocuments;

import java.lang.annotation.*;

/**
 * Custom annotation for validating the photos in {@link UploadDocuments}.
 * <p>This annotation is used to ensure that the uploaded photos, such as the board photo and building photo,
 * meet specific validation criteria including file format and size.</p>
 * It can be applied at the class or method level, and it is processed by the {@link PhotoValidator}.
 *
 * <p>The validation checks include:
 * <ul>
 *     <li>Validating that the file format is either JPG or PNG.</li>
 *     <li>Ensuring the file size is no more than 1MB.</li>
 * </ul>
 * </p>
 *
 * <p>The default error message indicates that the file format and size are invalid, but it can be customized.</p>
 *
 * <p>This annotation is typically used on the {@link UploadDocuments} object that contains the photos to validate.</p>
 */
@Documented
@Constraint(validatedBy = PhotoValidator.class)
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidPhotos {

    /**
     * The default error message to display if the validation fails.
     * This message is shown when a photo does not meet the validation criteria.
     *
     * @return The default error message.
     */
    String message() default "Invalid file format or size. Supported formats: JPG, PNG. Maximum size: 1MB.";

    /**
     * Groups that can be used to organize validation constraints.
     * This can be left empty for basic validation or used to group constraints logically.
     *
     * @return The groups associated with this validation constraint.
     */
    Class<?>[] groups() default {};

    /**
     * Additional data or metadata associated with the constraint.
     * This can be used for further customization or extension.
     *
     * @return The payload associated with this validation constraint.
     */
    Class<? extends Payload>[] payload() default {};
}
