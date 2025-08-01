package carestack.organization.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * Data Transfer Object (DTO) for various response types related to organization information.
 * <p>This class encapsulates a range of response structures related to states, districts, locations, and more.</p>
 */
@Data
public class ResponseDTOs {

    /**
     * Represents a state and its associated districts.
     * <p>This DTO contains information about a state, including its code, name, and the districts within it.</p>
     */
    @Data
    public static class StateDTO {
        private String code;
        private String name;

        private List<DistrictDTO> districts;
    }

    /**
     * Represents a district with its code and name.
     * <p>This DTO is used to capture district-level details within a state.</p>
     */
    @Data
    public static class DistrictDTO {
        private String code;
        private String name;
    }

    /**
     * Represents a type of data response, containing a type and a list of code-value pairs.
     * <p>This is used for responses that return a type and associated code-value pairs.</p>
     */
    @Data
    public static class TypeDataResponseDTO {
        private String type;
        private List<CodeValueDTO> data;
    }

    /**
     * Represents a code-value pair in the response data.
     * <p>This DTO is used within {@link TypeDataResponseDTO} to represent a code-value mapping.</p>
     */
    @Data
    public static class CodeValueDTO {
        private String code;
        private String value;
    }

    /**
     * Represents a list of master types with their descriptions.
     * <p>This DTO contains a list of master types, each represented by a type and description.</p>
     */
    @Data
    public static class masterTypesDTO {
        private List<TypeDescDTO> masterTypes;
    }

    /**
     * Represents a type with its description.
     * <p>This DTO is used to store a type and its corresponding description in the response.</p>
     */
    @Data
    public static class TypeDescDTO {
        private String type;
        private String desc;
    }

    /**
     * Represents a response containing location results and status information.
     * <p>This DTO is used for responses that return location-based results along with a status.</p>
     */
    @Data
    public static class LocationResponseDTO {
        private List<LocationResultDTO> results;
        private String status;
    }

    /**
     * Represents an individual location result with address components and geographical data.
     * <p>This DTO contains information about a specific location, including address components, formatted address,
     * geographical data, and other relevant details.</p>
     */
    @Data
    public static class LocationResultDTO {
        @JsonProperty("address_components")
        private List<AddressComponentDTO> addressComponents;

        @JsonProperty("formatted_address")
        private String formattedAddress;

        private GeometryDTO geometry;

        @JsonProperty("partial_match")
        private boolean partialMatch;

        @JsonProperty("place_id")
        private String placeId;

        private List<String> types;
    }

    /**
     * Represents an individual address component with its long name, short name, and types.
     * <p>This DTO is used within the location result to store individual address components like street name, city, etc.</p>
     */
    @Data
    public static class AddressComponentDTO {
        @JsonProperty("long_name")
        private String longName;

        @JsonProperty("short_name")
        private String shortName;

        private List<String> types;
    }

    /**
     * Represents the geographical data associated with a location, including its location and bounds.
     * <p>This DTO holds the geographical data for a location, including its latitude, longitude, bounds, and viewport.</p>
     */
    @Data
    public static class GeometryDTO {
        private LocationDTO location;
        private BoundsDTO bounds;
        private BoundsDTO viewport;

        @JsonProperty("location_type")
        private String locationType;
    }

    /**
     * Represents a location's latitude and longitude.
     * <p>This DTO is used within {@link GeometryDTO} to store the geographic coordinates of a location.</p>
     */
    @Data
    public static class LocationDTO {
        private double lat;
        private double lng;
    }

    /**
     * Represents the bounds of a geographic area.
     * <p>This DTO is used to store the northeast and southwest corners of a geographic area, defining its boundaries.</p>
     */
    @Data
    public static class BoundsDTO {
        private LocationDTO northeast;
        private LocationDTO southwest;
    }
}
