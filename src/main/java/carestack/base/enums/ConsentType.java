package carestack.base.enums;

/**
 * Enum representing the type of consent.
 * This enum is used to track whether consent was received or requested.
 * Example usage:
 * <pre>
 *     ConsentType consent = ConsentType.Received;
 *     System.out.println(consent); // Outputs: Received
 * </pre>
 */
public enum ConsentType {
    Received, Requested
}
