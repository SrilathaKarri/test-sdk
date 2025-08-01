package carestack.base.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * Enum representing the states and union territories of India.
 * Each enum constant corresponds to the name of a state or union territory.
 * This enum can be used to map a given state name (as a string) to its corresponding enum constant,
 * and vice versa, as well as to perform operations based on state names.
 * The state names are represented in a case-insensitive manner.
 * Example usage:
 * <pre>
 *     StatesAndUnionTerritories state = StatesAndUnionTerritories.fromString("Andhra Pradesh");
 *     System.out.println(state.getState()); // Outputs: Andhra Pradesh
 * </pre>
 *
 * The enum also includes a default value, {@link #UNKNOWN}, for any state that doesn't match the known list.
 */

@Getter
public enum StatesAndUnionTerritories {
    ANDHRAPRADESH("Andhra Pradesh"),
    ARUNACHALPRADESH("Arunachal Pradesh"),
    ASSAM("Assam"),
    BIHAR("Bihar"),
    CHATTISGARH("Chattisgarh"),
    CHHATTISGARH("Chhattisgarh"),
    GOA("Goa"),
    GUJARAT("Gujarat"),
    HARYANA("Haryana"),
    HIMACHALPRADESH("Himachal Pradesh"),
    JHARKHAND("Jharkhand"),
    KARNATAKA("Karnataka"),
    KERALA("Kerala"),
    MADHYAPRADESH("Madhya Pradesh"),
    MAHARASHTRA("Maharashtra"),
    MANIPUR("Manipur"),
    MEGHALAYA("Meghalaya"),
    MIZORAM("Mizoram"),
    NAGALAND("Nagaland"),
    ODISHA("Odisha"),
    PUNJAB("Punjab"),
    RAJASTHAN("Rajasthan"),
    SIKKIM("Sikkim"),
    TAMILNADU("Tamil Nadu"),
    TELANGANA("Telangana"),
    TRIPURA("Tripura"),
    UTTARPRADESH("Uttar Pradesh"),
    UTTARAKHAND("Uttarakhand"),
    WESTBENGAL("West Bengal"),
    ANDAMANANDNICOBARS("Andaman and Nicobar"),
    LAKSHADWEEP("Lakshadweep"),
    DELHI("Delhi"),
    DADRAHAVELI("Dadra and Nagar Haveli and Daman & Diu"),
    JAMMUANDKASHMIR("Jammu and Kashmir"),
    CHANDIGARH("Chandigarh"),
    LADAKH("Ladakh"),
    PUDUCHERRY("Puducherry"),
    UNKNOWN("Unknown");

    private final String state;

    StatesAndUnionTerritories(String state) {
        this.state = state;
    }

    @JsonValue
    public String getState() {
        return state;
    }

    @JsonCreator
    public static StatesAndUnionTerritories fromString(String value) {
        for (StatesAndUnionTerritories state : StatesAndUnionTerritories.values()) {
            if (state.getState().equalsIgnoreCase(value)) {
                return state;
            }
        }
        throw new IllegalArgumentException("Invalid state: " + value);
    }

}
