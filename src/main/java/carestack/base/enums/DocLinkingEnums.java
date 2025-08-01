package carestack.base.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * This class contains various enumerations used for a document linking operations.
 * <p>
 * The enums cover different aspects of healthcare-related data, including appointment priorities,
 * authentication methods, care plan statuses, medication details, health information types, and more.
 * They are designed to ensure data consistency across the SDK and facilitate seamless data mapping
 * with external systems.
 * </p>
 */
public class DocLinkingEnums {

    /**
     * Enum representing the priority of an appointment.
     */
    @Getter
    public enum AppointmentPriority {
        Emergency("Emergency"),
        FOLLOW_UP_VISIT("Follow-up visit"),
        NEW("New");

        private final String value;

        AppointmentPriority(String value) {
            this.value = value;
        }

        @JsonValue
        public String getValue() {
            return value;
        }

        @JsonCreator
        public static AppointmentPriority fromValue(String value) {
            if (value == null || value.isEmpty()) {
                return null;
            }
            for (AppointmentPriority priority : values()) {
                if (priority.getValue().equalsIgnoreCase(value)) {
                    return priority;
                }
            }
            throw new IllegalArgumentException("Unknown AppointmentPriority value: " + value);
        }
    }

    /**
     * Enum representing various authentication methods.
     */
    @Getter
    public enum AuthMode {
        MOBILE_OTP("MOBILE_OTP"),
        AADHAAR_OTP("AADHAAR_OTP"),
        DEMOGRAPHICS("DEMOGRAPHICS"),
        DIRECT("DIRECT");

        private final String value;

        AuthMode(String value) {
            this.value = value;
        }
    }

    /**
     * Enum representing the intent of a care plan.
     */
    @Getter
    public enum CarePlanIntent {
        PROPOSAL("proposal"),
        PLAN("plan"),
        ORDER("order"),
        OPTION("option");

        private final String value;

        CarePlanIntent(String value) {
            this.value = value;
        }
    }

    /**
     * Enum representing the status of a care plan.
     */
    @Getter
    public enum CarePlanStatus {
        DRAFT("draft"),
        ACTIVE("active"),
        ON_HOLD("on-hold"),
        REVOKED("revoked"),
        COMPLETED("completed"),
        ENTERED_IN_ERROR("entered-in-error"),
        UNKNOWN("unknown");

        private final String value;

        CarePlanStatus(String value) {
            this.value = value;
        }
    }

    /**
     * Enum representing the clinical status of a patient or condition.
     */
    @Getter
    public enum ClinicalStatus {
        ACTIVE("active"),
        RECURRENCE("recurrence"),
        RELAPSE("relapse"),
        INACTIVE("inactive"),
        REMISSION("remission"),
        RESOLVED("resolved");

        private final String value;

        ClinicalStatus(String value) {
            this.value = value;
        }
    }

    /**
     * Enum representing the status of a diagnostic report.
     */
    @Getter
    public enum DiagnosticReportStatus {
        REGISTERED("registered"),
        PARTIAL("partial"),
        PRELIMINARY("preliminary"),
        FINAL("final");

        private final String value;

        DiagnosticReportStatus(String value) {
            this.value = value;
        }
    }

    /**
     * Enum representing the dosage frequency of medication.
     */
    @Getter
    public enum DosageFrequency {
        ONCE("Once"),
        TWICE("Twice"),
        THRICE("Thrice"),
        QUADTUPLE("Quadtuple");

        private final String value;

        DosageFrequency(String value) {
            this.value = value;
        }
    }

    /**
     * Enum representing the route through which medication is administered.
     */
    @Getter
    public enum MedicationRoute {
        ORAL("Oral"),
        TOPICAL("Topical"),
        INTRAVENOUS("Intravenous"),
        INTRAMUSCULAR("Intramuscular"),
        SUBCUTANEOUS("Subcutaneous"),
        INHALATION("Inhalation"),
        INTRANASAL("Intranasal"),
        RECTAL("Rectal"),
        SUBLINGUAL("Sublingual"),
        BUCCAL("Buccal"),
        IV("Intra venal");

        private final String value;

        MedicationRoute(String value) {
            this.value = value;
        }
    }

    /**
     * Enum representing methods of medication administration.
     */
    @Getter
    public enum MedicationMethod {
        SWALLOW("Swallow");

        private final String value;

        MedicationMethod(String value) {
            this.value = value;
        }
    }

    /**
     * Enum representing different types of health information records.
     */
    @Getter
    public enum HealthInformationTypes {
        OPConsultation("OPConsultation"),
        Prescription("Prescription"),
        DischargeSummary("DischargeSummary"),
        DiagnosticReport("DiagnosticReport"),
        ImmunizationRecord("ImmunizationRecord"),
        HealthDocumentRecord("HealthDocumentRecord"),
        WellnessRecord("WellnessRecord");

        private final String value;

        HealthInformationTypes(String value) {
            this.value = value;
        }

        @JsonValue
        public String getValue() {
            return value;
        }

        @JsonCreator
        public static HealthInformationTypes fromValue(String value) {
            if (value == null || value.isEmpty()) {
                return null;
            }
            for (HealthInformationTypes type : values()) {
                if (type.getValue().equalsIgnoreCase(value)) {
                    return type;
                }
            }
            throw new IllegalArgumentException("Unknown HealthInformationTypes value: " + value);
        }
    }
    /**
     * Enum representing the status of immunization.
     */
    @Getter
    public enum ImmunizationStatusEnum {
        COMPLETED("completed"),
        ENTERED_IN_ERROR("entered-in-error"),
        NOT_DONE("not-done");

        private final String value;

        ImmunizationStatusEnum(String value) {
            this.value = value;
        }
    }

    /**
     * Enum representing the status of medication requests.
     */
    @Getter
    public enum MedicationRequestStatus {
        DRAFT("draft"),
        ACTIVE("active"),
        ON_HOLD("on-hold"),
        REVOKED("revoked"),
        COMPLETED("completed"),
        ENTERED_IN_ERROR("entered-in-error"),
        UNKNOWN("unknown");

        private final String value;

        MedicationRequestStatus(String value) {
            this.value = value;
        }
    }

    /**
     * Enum representing the status of medication statements.
     */
    @Getter
    public enum MedicationStatementStatus {
        ACTIVE("active"),
        COMPLETED("completed"),
        ENTERED_IN_ERROR("entered-in-error"),
        INTENDED("intended"),
        STOPPED("stopped"),
        ON_HOLD("on-hold"),
        UNKNOWN("unknown"),
        NOT_TAKEN("not-taken");

        private final String value;

        MedicationStatementStatus(String value) {
            this.value = value;
        }
    }

    /**
     * Enum representing the status of an observation.
     */
    @Getter
    public enum ObservationStatus {
        REGISTERED("registered"),
        PRELIMINARY("preliminary"),
        FINAL("final"),
        AMENDED("amended");

        private final String value;

        ObservationStatus(String value) {
            this.value = value;
        }
    }

    /**
     * Enum representing the status of a medical procedure.
     */
    @Getter
    public enum ProcedureStatus {
        PREPARATION("preparation"),
        IN_PROGRESS("in-progress"),
        NOT_DONE("not-done"),
        ON_HOLD("on-hold"),
        STOPPED("stopped"),
        COMPLETED("completed"),
        ENTERED_IN_ERROR("entered-in-error"),
        UNKNOWN("unknown");

        private final String value;

        ProcedureStatus(String value) {
            this.value = value;
        }
    }

    /**
     * Enum representing the intent of a service request.
     */
    @Getter
    public enum ServiceRequestIntent {
        PROPOSAL("proposal"),
        PLAN("plan"),
        DIRECTIVE("directive"),
        ORDER("order"),
        ORIGINAL_ORDER("original-order"),
        REFLEX_ORDER("reflex-order"),
        FILLER_ORDER("filler-order"),
        INSTANCE_ORDER("instance-order"),
        OPTION("option");

        private final String value;

        ServiceRequestIntent(String value) {
            this.value = value;
        }
    }

    /**
     * Enum representing the status of a service request.
     */
    @Getter
    public enum ServiceRequestStatus {
        DRAFT("draft"),
        ACTIVE("active"),
        ON_HOLD("on-hold"),
        REVOKED("revoked"),
        COMPLETED("completed"),
        ENTERED_IN_ERROR("entered-in-error"),
        UNKNOWN("unknown");

        private final String value;

        ServiceRequestStatus(String value) {
            this.value = value;
        }
    }

    /**
     * Enum representing the verification status of a resource.
     */
    @Getter
    public enum VerificationStatus {
        UNCONFIRMED("unconfirmed"),
        CONFIRMED("confirmed"),
        REFUTED("refuted"),
        ENTERED_IN_ERROR("entered-in-error");

        private final String value;

        VerificationStatus(String value) {
            this.value = value;
        }
    }
}
