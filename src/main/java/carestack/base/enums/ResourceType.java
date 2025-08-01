package carestack.base.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Enum representing different types of healthcare resources.
 * Each enum constant corresponds to a specific healthcare resource type,
 * which can be used to categorize and filter healthcare data.
 * Example usage:
 * <pre>
 *     ResourceType resource = ResourceType.AllergyIntolerance;
 *     System.out.println(resource); // Outputs: AllergyIntolerance
 * </pre>
 */
public enum ResourceType {
    AllergyIntolerance, Appointment, MedicationRequest, MedicationStatement,
    DocumentReference, Observation, Patient, Binary, Bundle, CarePlan,
    Composition, Condition, Encounter, FamilyMemberHistory, ImagingStudy,
    Immunization, ImmunizationRecommendation, Media, Organization,
    Practitioner, PractitionerRole, Procedure, ServiceRequest, Specimen,
    Staff, Consent, CareContext, HiuHealthBundle, Location, Coverage,
    CoverageEligibilityRequest, CoverageEligibilityResponse, Claim,
    ClaimResponse, CommunicationRequest, Communication, PaymentNotice,
    PaymentReconciliation, Task, InsurancePlan;

    @JsonValue
    public String getValue() {
        return name();
    }

    @JsonCreator
    public static ResourceType fromString(String value) {
        for (ResourceType type : ResourceType.values()) {
            if (type.name().equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Invalid ResourceType: " + value);
    }
}
