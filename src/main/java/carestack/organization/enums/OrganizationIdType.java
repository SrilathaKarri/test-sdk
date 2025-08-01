package carestack.organization.enums;

import lombok.Getter;
import carestack.base.errors.EhrApiError;
import carestack.base.errors.ErrorType;

import java.util.Arrays;

@Getter
public enum OrganizationIdType {
    ACCOUNT_ID("accountId"),
    ORGANIZATION_ID("facilityId"),
    organizationId("facilityId"),
    ID("id");

    private final String value;

    OrganizationIdType(String organizationIdType) {
        this.value = organizationIdType;
    }

    /**
     * Converts a string representation of an Organization ID type to the corresponding enum.
     *
     * @param idType the string representation of the organization ID type
     * @return the corresponding OrganizationIdType enum
     * @throws EhrApiError if the provided idType is null, empty, or invalid
     * @note The matching is case-insensitive and works with both enum names and their string values
     * <p><b>Usage Example:</b></p>
     * <pre>
     *     OrganizationIdType type = OrganizationIdType.fromString("accountId");
     *     System.out.println(type); // Outputs: ACCOUNT_ID
     * </pre>
     */
    public static OrganizationIdType fromString(String idType) {
        if (idType == null || idType.trim().isEmpty()) {
            throw new EhrApiError("Organization ID Type cannot be null or empty.", ErrorType.VALIDATION);
        }

        return Arrays.stream(OrganizationIdType.values())
                .filter(type -> type.name().equalsIgnoreCase(idType) || type.getValue().equalsIgnoreCase(idType))
                .findFirst()
                .orElseThrow(() -> {
                    String validTypes = Arrays.stream(OrganizationIdType.values())
                            .map(type -> String.format("%s ,%s", type.name(), type.getValue()))
                            .reduce((a, b) -> a + ", " + b)
                            .orElse("No valid types defined");

                    String message = String.format("Invalid Organization ID Type: \"%s\". Valid types are: %s", idType, validTypes);
                    return new EhrApiError(message, ErrorType.NOT_FOUND);
                });
    }

}
