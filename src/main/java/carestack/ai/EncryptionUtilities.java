package carestack.ai;

import carestack.base.config.EmbeddedSdkProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.RSAEncrypter;
import com.nimbusds.jose.jwk.RSAKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import carestack.base.utils.LogUtil;

import javax.crypto.Cipher;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <h3>Encryption Utilities for Secure Data Handling</h3>
 *
 * This utility class provides centralized methods for performing cryptographic operations required by the SDK.
 * It supports two primary encryption schemes:
 * <ul>
 *   <li><b>JWE (JSON Web Encryption):</b> Used for encrypting payloads sent to the AI services. It employs RSA-OAEP-256 for key wrapping and A256GCM for content encryption.</li>
 *   <li><b>RSA-OAEP with SHA-1:</b> Used for encrypting sensitive data (like Aadhaar numbers and OTPs) for ABHA (Ayushman Bharat Health Account) services, as per NDHM specifications.</li>
 * </ul>
 * The class relies on certificates configured in the application properties for sourcing public keys.
 */
@Component
public class EncryptionUtilities {

    private final ObjectMapper objectMapper;

    @Autowired
    private EmbeddedSdkProperties embeddedProperties;

    // User can override certificates if needed (optional)
    @Value("${abha.encryption.certificate:#{null}}")
    private String userAbhaCertificatePem;

    @Value("${certificate.pem:#{null}}")
    private String userCertificatePem;

    /**
     * Constructs the EncryptionUtilities component.
     *
     * @param objectMapper Jackson's ObjectMapper for JSON serialization.
     */
    public EncryptionUtilities(ObjectMapper objectMapper) {
            this.objectMapper = objectMapper;
        }

    /**
     * <h3>Encrypt Payload using JWE for AI Service</h3>
     * Encrypts a given payload object into a JWE (JSON Web Encryption) compact serialization string,
     * suitable for secure transmission to the AI service. This method uses the `RSA_OAEP_256`
     * algorithm for key encryption and `A256GCM` for content encryption.
     *
     * <h3>Input and Validation:</h3>
     * The method accepts a generic {@link Object} as its payload.
     * <ul>
     *     <li><b>payloadData (Object):</b> The data to be encrypted. This can be:
     *         <ul>
     *             <li>A {@code List<String>} containing file URLs. In this case, the list is wrapped in a JSON object with a "files" key: {@code {"files": [...]}}.</li>
     *             <li>A {@code Map<String, Object>} or any other POJO representing structured data (e.g., extracted clinical entities). This object is serialized directly to JSON.</li>
     *         </ul>
     *     </li>
     * </ul>
     * The input object must be serializable to JSON by {@link ObjectMapper}. The method relies on the calling service to provide a valid, non-null payload.
     *
     * <h3>Workflow:</h3>
     * <ol>
     *     <li>Loads the RSA public key from the certificate specified by the {@code certificate.pem} application property.</li>
     *     <li>Constructs a JWE header with the required encryption algorithms and custom parameters (e.g., {@code correlation_id}).</li>
     *     <li>Serializes the input {@code payloadData} into a JSON string.</li>
     *     <li>Creates a {@link JWEObject} with the header and the JSON payload.</li>
     *     <li>Encrypts the JWE object using the loaded public key.</li>
     *     <li>Serializes the final encrypted object into a compact JWE string format (header.encrypted_key.iv.ciphertext.tag).</li>
     * </ol>
     *
     * <h3>Output:</h3>
     * @return A {@link String} containing the JWE compact serialization of the encrypted payload.
     *
     * <h3>Error Handling:</h3>
     * Throws a {@link RuntimeException} if any step in the process fails, such as:
     * <ul>
     *     <li>The certificate cannot be found or parsed.</li>
     *     <li>The public key cannot be extracted.</li>
     *     <li>The payload cannot be serialized to JSON.</li>
     *     <li>The JWE encryption process fails.</li>
     * </ul>
     *
     * <h3>Example Usage (Encrypting File URLs):</h3>
     * <pre>{@code
     * List<String> fileUrls = Arrays.asList(
     *     "https://example-bucket.s3.amazonaws.com/discharge1.pdf",
     *     "https://example-bucket.s3.amazonaws.com/report.jpg"
     * );
     *
     * try {
     *     String jweString = encryptionUtilities.encryption(fileUrls);
     *     System.out.println("Encrypted JWE String: " + jweString);
     * } catch (RuntimeException e) {
     *     System.err.println("Encryption failed: " + e.getMessage());
     * }
     * }</pre>
     *
     * <h3>Expected Output (Example):</h3>
     * <pre>{@code
     * // A long, dot-separated JWE string
     * Encrypted JWE String: eyJhbGciOiJSU0EtT0FFUC0yNTYiLCJlbmMiOiJBMjU2R0NNIiwiY29ycmVsYXRpb25faWQiOjJ9.K1o...[rest of encrypted key]...Q.m9y...[rest of IV]...w.g5V...[rest of ciphertext and tag]...A
     * }</pre>
     *
     * @param payloadData The object (e.g., List of URLs, Map of data) to be encrypted.
     * @throws RuntimeException if the encryption process fails at any stage.
     */
    public String encryption(Object payloadData) {
        try {
            Map<String, Object> protectedHeadersData = new HashMap<>();
            protectedHeadersData.put("correlation_id", 2);

            // Use user certificate if provided, otherwise use embedded
            String effectiveCertificate = (userCertificatePem != null && !userCertificatePem.trim().isEmpty())
                    ? userCertificatePem
                    : embeddedProperties.getCertificatePem();

            RSAPublicKey publicKey = extractPublicKeyFromCertificate(effectiveCertificate);
            RSAKey rsaKey = new RSAKey.Builder(publicKey).build();

            JWEHeader.Builder headerBuilder = new JWEHeader.Builder(JWEAlgorithm.RSA_OAEP_256, EncryptionMethod.A256GCM);
            protectedHeadersData.forEach(headerBuilder::customParam);
            JWEHeader jweHeader = headerBuilder.build();

            String payloadJson;
            if (payloadData instanceof List<?>) {
                Map<String, Object> wrapped = new HashMap<>();
                wrapped.put("files", payloadData);
                payloadJson = objectMapper.writeValueAsString(wrapped);
            } else {
                payloadJson = objectMapper.writeValueAsString(payloadData);
            }

            JWEObject jweObject = new JWEObject(jweHeader, new Payload(payloadJson));
            RSAEncrypter encrypter = new RSAEncrypter(rsaKey);
            jweObject.encrypt(encrypter);

            return jweObject.serialize();

        } catch (Exception e) {
            LogUtil.logger.error("Failed to encrypt payload", e);
            throw new RuntimeException("Encryption failed", e);
        }
    }



        /**
         * Extracts RSA public key from X.509 certificate PEM string.
         *
         * @param certificatePem PEM encoded certificate string
         * @return RSAPublicKey extracted from the certificate
         * @throws RuntimeException if certificate parsing fails
         */
        private RSAPublicKey extractPublicKeyFromCertificate(String certificatePem) {
            try {
                String cleanedPem = certificatePem
                        .replace("\\n", "\n")
                        .replaceAll(" +", " ")
                        .trim();

                CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
                ByteArrayInputStream certInputStream = new ByteArrayInputStream(cleanedPem.getBytes());
                X509Certificate certificate = (X509Certificate) certificateFactory.generateCertificate(certInputStream);

                return (RSAPublicKey) certificate.getPublicKey();

            } catch (Exception e) {
                LogUtil.logger.error("Failed to extract public key from certificate", e);
                throw new RuntimeException("Failed to extract public key from certificate", e);
            }
        }

    /**
     * <h3>Encrypt Data for ABHA Service</h3>
     * Encrypts a string using the RSA/ECB/OAEPWithSHA-1AndMGF1Padding algorithm, which is the standard
     * required for interacting with ABHA (Ayushman Bharat Health Account) APIs. The result is Base64 encoded.
     *
     * <h3>Input and Validation:</h3>
     * <ul>
     *     <li><b>dataToEncrypt (String):</b> The plaintext data that needs to be encrypted (e.g., an Aadhaar number or a one-time password). This parameter must not be null.</li>
     *     <li><b>certificatePem (String):</b> The public key or X.509 certificate in PEM format used for encryption. The string should be a valid PEM-encoded key or certificate. The method can handle both `-----BEGIN PUBLIC KEY-----` and `-----BEGIN CERTIFICATE-----` formats.</li>
     * </ul>
     *
     * <h3>Workflow:</h3>
     * <ol>
     *     <li>Loads the {@link PublicKey} from the provided PEM string.</li>
     *     <li>Initializes a {@link javax.crypto.Cipher} instance with the `RSA/ECB/OAEPWithSHA-1AndMGF1Padding` transformation.</li>
     *     <li>Encrypts the UTF-8 bytes of the {@code dataToEncrypt} string.</li>
     *     <li>Encodes the resulting ciphertext into a Base64 string.</li>
     * </ol>
     *
     * <h3>Output:</h3>
     * @return A {@link String} containing the Base64-encoded encrypted data.
     *
     * <h3>Error Handling:</h3>
     * Throws a {@link RuntimeException} if any step in the process fails, such as:
     * <ul>
     *     <li>The provided PEM string is invalid or cannot be parsed.</li>
     *     <li>The encryption cipher cannot be initialized.</li>
     *     <li>The encryption operation fails.</li>
     * </ul>
     *
     * <h3>Example Usage:</h3>
     * <pre>{@code
     * String aadhaarNumber = "123456789012";
     * String publicKeyPem = "-----BEGIN PUBLIC KEY-----\n" +
     *                       "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA...[key content]...\n" +
     *                       "-----END PUBLIC KEY-----";
     *
     * try {
     *     String encryptedAadhaar = encryptionUtilities.encryptDataForAbha(aadhaarNumber, publicKeyPem);
     *     System.out.println("Encrypted Aadhaar: " + encryptedAadhaar);
     * } catch (RuntimeException e) {
     *     System.err.println("ABHA encryption failed: " + e.getMessage());
     * }
     * }</pre>
     *
     * <h3>Expected Output (Example):</h3>
     * <pre>{@code
     * // A Base64 encoded string
     * Encrypted Aadhaar: Zm9vYmFyYmF6cXV4...[rest of base64 string]...=
     * }</pre>
     *
     * @param dataToEncrypt The plaintext string to encrypt.
     * @param certificatePem The PEM-formatted public key or certificate string.
     * @throws RuntimeException if the encryption process fails.
     */
    public String encryptDataForAbha(String dataToEncrypt, String certificatePem) {
        try {
            String keyContent = certificatePem.trim();
            if (!keyContent.startsWith("-----BEGIN")) {
                keyContent = "-----BEGIN PUBLIC KEY-----\n" + keyContent + "\n-----END PUBLIC KEY-----";
            }

            PublicKey publicKey = loadPublicKeyFromPem(keyContent);
            Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-1AndMGF1Padding");
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);

            byte[] encryptedBytes = cipher.doFinal(dataToEncrypt.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encryptedBytes);

        } catch (Exception e) {
            LogUtil.logger.error("Failed to encrypt data for ABHA", e);
            throw new RuntimeException("Failed to encrypt data for ABHA", e);
        }
    }

    /**
     * <h3>Encrypt Data for ABHA Service using Configured Certificate</h3>
     * A convenience method that encrypts a string for ABHA services using the globally configured
     * ABHA certificate. This method retrieves the certificate from the `abha.encryption.certificate`
     * application property.
     *
     * <h3>Input and Validation:</h3>
     * <ul>
     *     <li><b>dataToEncrypt (String):</b> The plaintext data that needs to be encrypted (e.g., an OTP). This parameter must not be null.</li>
     * </ul>
     * The method performs an internal validation to ensure that the `abha.encryption.certificate` property has been set in the application configuration.
     *
     * <h3>Workflow:</h3>
     * <ol>
     *     <li>Checks if the `ABHA_CERTIFICATE_PEM` field (injected from properties) is configured.</li>
     *     <li>If not configured, it throws an {@link IllegalStateException}.</li>
     *     <li>If configured, it delegates the encryption logic to {@link #encryptDataForAbha(String, String)}.</li>
     * </ol>
     *
     * <h3>Output:</h3>
     * @return A {@link String} containing the Base64-encoded encrypted data.
     *
     * <h3>Error Handling:</h3>
     * <ul>
     *     <li>Throws an {@link IllegalStateException} if the `abha.encryption.certificate` property is not configured.</li>
     *     <li>Throws a {@link RuntimeException} if the underlying encryption process fails (propagated from the delegate method).</li>
     * </ul>
     *
     * <h3>Example Usage:</h3>
     * <pre>{@code
     * // Assuming 'abha.encryption.certificate' is set in application.properties
     * String otp = "123456";
     *
     * try {
     *     String encryptedOtp = encryptionUtilities.encryptDataForAbha(otp);
     *     System.out.println("Encrypted OTP: " + encryptedOtp);
     * } catch (IllegalStateException e) {
     *     System.err.println("Configuration error: " + e.getMessage());
     * } catch (RuntimeException e) {
     *     System.err.println("ABHA encryption failed: " + e.getMessage());
     * }
     * }</pre>
     *
     * <h3>Expected Output (Example):</h3>
     * <pre>{@code
     * // A Base64 encoded string
     * Encrypted OTP: dGhpcyBpcyBhIHR...[rest of base64 string]...=
     * }</pre>
     *
     * @param dataToEncrypt The plaintext string to encrypt.
     * @throws IllegalStateException if the ABHA certificate is not configured in application properties.
     * @throws RuntimeException if the encryption process fails.
     */
    public String encryptDataForAbha(String dataToEncrypt) {
        // Use user ABHA certificate if provided, otherwise use embedded
        String effectiveAbhaCertificate = (userAbhaCertificatePem != null && !userAbhaCertificatePem.trim().isEmpty())
                ? userAbhaCertificatePem
                : embeddedProperties.getAbhaCertificatePem();

        if (effectiveAbhaCertificate == null || effectiveAbhaCertificate.trim().isEmpty()) {
            throw new IllegalStateException("ABHA certificate not configured (neither user nor embedded)");
        }
        return encryptDataForAbha(dataToEncrypt, effectiveAbhaCertificate);
    }

    /**
     * Loads a public key from PEM string.
     */
    private PublicKey loadPublicKeyFromPem(String pemString) throws Exception {
        String cleanPem = pemString
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replace("-----BEGIN CERTIFICATE-----", "")
                .replace("-----END CERTIFICATE-----", "")
                .replaceAll("\\s", "");

        if (pemString.contains("BEGIN CERTIFICATE")) {
            return extractPublicKeyFromCertificate(pemString);
        }

        byte[] keyBytes = Base64.getDecoder().decode(cleanPem);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePublic(spec);
    }

}