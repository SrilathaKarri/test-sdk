package carestack.base.utils;


public final class Constants {

    private Constants() {
        throw new AssertionError("Utility class should not be instantiated");
    }

    //Health-Lake Endpoints
    public static final String GET_PROFILES_URL = "/health-lake/get-profiles";

    //Organization Endpoints
    public static final String GET_ORGANIZATIONS_URL = "/facilities";
    public static final String REGISTER_ORGANIZATION_URL = "/register-facility";
    public static final String UPDATE_ORGANIZATION_URL = "/update-facility";
    public static final String DELETE_ORGANIZATION_URL = "/facility";
    public static final String SEARCH_ORGANIZATION_URL = "/search-facility";

    //Demographic Data Endpoints
    public static final String FETCH_STATES_URL = "/lgd-states";
    public static final String FETCH_SUB_DISTRICTS_URL = "/lgd-subdistricts";
    public static final String DISTRICT_CODE_PARAM = "?districtCode=%d";
    public static final String FETCH_OWNERSHIP_SUBTYPE_URL = "/owner-subtype";
    public static final String FETCH_SPECIALITIES_URL = "/specialities";
    public static final String FETCH_ORGANIZATION_TYPE_URL = "/facility-type";
    public static final String FETCH_ORGANIZATION_SUBTYPE_URL = "/facility-subtypes";
    public static final String FETCH_MASTER_TYPES_URL = "/master-types";
    public static final String FETCH_MASTER_DATA_URL = "/master-data";

    //Google Credentials
    public static final String GOOGLE_LOCATION_URL = "https://maps.googleapis.com/maps/api/geocode/json";

    //AI Endpoints
    public static final String GENERATE_CASE_SHEET_SUMMARY_URL = "/demo/generate-discharge-summary";
    public static final String GENERATE_FHIR_BUNDLE_URL = "/demo/generate-fhir-bundle";
    public static final String HEALTH_INFORMATION_EXTRACTION_URL= "/entity/extraction";

    //HPR Endpoints
    public static final String GENERATE_AADHAAR_OTP = "/aadhaar/generateOtp";
    public static final String VERIFY_AADHAAR_OTP = "/aadhaar/verifyOtp";
    public static final String CHECK_HPR_ACCOUNT = "/check/account-exist";
    public static final String DEMOGRAPHIC_AUTH_MOBILE = "/demographic-auth/mobile";
    public static final String GENERATE_MOBILE_OTP = "/generate/mobileOtp";
    public static final String VERIFY_MOBILE_OTP = "/verify/mobileOtp";
    public static final String GET_HPR_SUGGESTIONS = "/hpId/suggestion";
    public static final String CREATE_HPR = "/hprId/create";


    //ABHA Endpoints
    public static final String REGISTER_WITH_AADHAAR = "patient/registration/abha/aadhaar/request-otp";
    public static final String ENROLL_AADHAAR = "patient/registration/abha/aadhaar/enroll";
    public static final String UPDATE_MOBILE = "patient/registration/abha/update/mobile/request-otp";
    public static final String VERIFY_UPDATE_MOBILE = "patient/registration/abha/update/mobile/verify-otp";
    public static final String GET_ABHA_ADDRESS_SUGGESTIONS = "patient/registration/abha/address-suggestions";
    public static final String FINAL_ABHA_REGISTRATION = "patient/registration/abha/abha-address";

    //Encounter Endpoints
    public static final String ADD_APPOINTMENT = "/add/Appointment";
    public static final String CREATE_CARE_CONTEXT = "/abdm-flows/create-carecontext";
    public static final String LINK_CARE_CONTEXT = "/abdm-flows/link-carecontext";
    public static final String UPDATE_VISIT_RECORDS = "/abdm-flows/update-visit-records";
    public static final String ENTITY_EXTRACTION = "/entity/extraction";



}
