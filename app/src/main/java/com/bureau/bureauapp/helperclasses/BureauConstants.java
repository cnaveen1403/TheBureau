package com.bureau.bureauapp.helperclasses;

public class BureauConstants {

    public static final String TWITTER_KEY = "6d2aRApYdMGLlzq9k8enuDOJj";
    public static final String TWITTER_SECRET = "vE3IULL8XVwNEfq8BVOhePc8hGp5qA8IzeiEdgxWhJTyXUevMC";
    public static final String x_api_key = "KLSS36qOp36Ps6e0LBt97Dw4QJz47084";
    public static final String base64EncodedPublicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA+hb/ftGObd461RnHHb8vcyItuLAwPebLcOSKJspncZrXjdJAERLGlh1IpPzZIU5frbYIW604yb/USphBcvmf39w7KKuZ7wHHkh6ZlIq82Fjf4E54B8EE7KcLYfPeVoJ/C6U+hqoSw4So/JU5ZNww7Pon/JF0y4yTCg3roFDLRR0/foZ2kwlPg4/31l3W9a2K3qMKAoBNjzVYBkffnpXA+1W1NNLE4wodCA4qePdM3/4tzEnHHnIVfPu3Fbd+bOSPZA+Np537ltHM3HLO4CKGk5muNsNtJafZLCwqx36g/dlURHcy26eLRvlOFdPcsLNtI1QBbRZ+9ZWgvrjKJgTzVwIDAQAB";

    /* URL's */
    public static final String BASE_URL = "http://dev.thebureauapp.com/api/";

    public static final String REGISTER_URL = "register";
    public static final String LOGIN_URL = "login";
    public static final String READ_PROFILE_DETAILS_URL = "profile/readprofiledetails/userid/";
    public static final String ACCOUNT_CREATION_URL = "profile/create";
    public static final String ACCOUNT_UPDATE_URL = "profile/update";
    public static final String CHECK_ZIPCODE_URL = "login/checkZipCodes/zip_code/";
    public static final String EDIT_UPDATE_URL = "profile/update";
    public static final String UPDATE_PROFILE_INFO_URL = "profile/update_profile_step2";
    public static final String PROFILE_HERITAGE_URL = "profile/update_profile_step3";
    public static final String SOCIAL_HERITAGE_URL = "profile/update_profile_step4";
    public static final String OCCUPATION_URL = "profile/update_profile_step5";
    public static final String LEGAL_STATUS_URL = "profile/update_profile_step6";
    public static final String HERITAGE_RELIGION_URL = "read/religion";
    public static final String HERITAGE_MOTHERTONGUE_URL = "read/mother_tongue";
    public static final String HERITAGE_FAMILY_ORIGIN_URL = "read/family_origin/religion_id/";
    public static final String HERITAGE_SPECIFICATION_URL = "read/specification/family_origin_id/";
    public static final String DELETE_IMAGE_URL = "profile/deleteImage";
    public static final String AUTHENTICATE_URL = "http://dev.thebureauapp.com/layer/public/authenticate.php";

    public static String NOTIFICATIONS_STATUS = "configuration/view";
    public static String UPDATE_NOTIFICATIONS = "configuration/update";
    public static String HOW_WE_WORK = "read/howwework";
    public static String SEND_FEED_BACK_API = "profile/sendFeedback";
    public static String DELETE_ACCOUNT = "profile/delete_account";
    public static String DEACTIVATE_ACCOUNT = "profile/deactivate";
    public static String ACTIVATE_ACCOUNT = "profile/activate";
    public static String READ_PREFERENCES = "preference/readPreference";
    public static String CREATE_PREFERENCES = "preference/update";
    public static String VIEW_MATCH_TAB = "match/view";
    public static String LIKE_PASS_MATCH = "match/pass_like";
    public static String VIEW_POOL_TAB = "pool/view";
    public static String VIEW_POOL_ACCESS = "pool/accesstype";
    public static String VIEW_REMATCH = "rematch/view";
    public static String ACCESS_REMATCH = "rematch/rematch";
    public static String GOLD_CHECK_REFERRALS = "gold/checkReferralCode";
    public static String MULTI_UPLOAD = "profile/multi_upload";
    public static String LOGOUT = "logout";
    public static String REGISTER_CHANNEL_ID = "notification/register_device";
    public static String GET_AVAILABLE_GOLD = "/gold/getGoldAvailable";
    public static String GET_CHAT_VIEW = "chat/view";
    public static String PREVIEW_IMAGE_URL = "http://dev.thebureauapp.com/assets/images/icon.png";
    public static String UPLOAD_HOROSCOPE = "profile/uploadHoroscope";
    public static String DELETE_HOROSCOPE_IMAGE = "profile/deleteHoroscope";
    public static final String ADD_GOLD_URL = "gold/add";

    //    Login
    public static final String referralCode = "referral_code";
    public static final String referralCodeApplied = "referral_code_applied";

// pool tab

    public static final String goldAvailable = "gold_available";
    public static final String accessType = "access_type";
    public static final String visitedUserid = "visited_userid";
    public static final String goldToConsume = "gold_amount";


    /* Signup Page Fields */
    public static final String regType = "reg_type";
    public static final String loginType = "login_type";
    public static final String loginValue = "login_value";

    /*Account Creation Fields*/
    public static final String userid = "userid";
    public static final String firstName = "first_name";
    public static final String lastName = "last_name";
    public static final String dob = "dob";
    public static final String gender = "gender";
    public static final String phoneNumber = "phone_number";
    public static final String email = "email";
    public static final String profileFor = "profile_for";
    public static final String profileFirstName = "profile_first_name";
    public static final String profileLastName = "profile_last_name";
    public static final String profileStatus = "profile_status";
    public static final String imgUrl = "img_url";

    /* Profile Info Fields */
    public static final String zipCode = "zip_code";
    public static final String countryResiding = "country_residing";
    public static final String currentZipCode = "current_zip_code";
    public static final String maritialStatus = "marital_status";
    public static final String profileDob = "profile_dob";
    public static final String profileGender = "profile_gender";
    public static final String heightFeet = "height_feet";
    public static final String heightInch = "height_inch";

    /* Profile Heritage Fields */
    public static final String religionId = "religion_id";
    public static final String motherTongueId = "mother_tongue_id";
    public static final String familyOriginId = "family_origin_id";
    public static final String specificationId = "specification_id";
    public static final String gothra = "gothra";
    public static final String religionName = "religion_name";
    public static final String motherTongue = "mother_tongue";
    public static final String familyOriginName = "family_origin_name";
    public static final String specificationName = "specification_name";

    public static final String employmentStatus = "employment_status";
    public static final String positionTitle = "position_title";
    public static final String company = "company";
    public static final String highestEducation = "highest_education";
    public static final String honors = "honors";
    public static final String major = "major";
    public static final String college = "college";
    public static final String graduatedYear = "graduated_year";
    public static final String educationSecond = "education_second";
    public static final String honorsSecond = "honors_second";
    public static final String majorsSecond = "majors_second";
    public static final String collegeSecond = "college_second";
    public static final String graduatedYearSecond = "graduation_years_second";

    public static final String yearsInUsa = "years_in_usa";
    public static final String legalStatus = "legal_status";

    public static final String minimumEducationRequirement = "minimum_education_requirement";
    public static final String accountCreatedBy = "account_created_by";
    public static final String locationRadius = "location_radius";
    public static final String ageFrom = "age_from";
    public static final String ageTo = "age_to";
    public static final String heightFromFeet = "height_from_feet";
    public static final String heightFromInch = "height_from_inch";
    public static final String heightToFeet = "height_to_feet";
    public static final String heightToInch = "height_to_inch";


    public static final String diet = "diet";
    public static final String drinking = "drinking";
    public static final String smoking = "smoking";

    public static final String horoscopePath = "horoscope_path";
    public static final String horoscopeDob = "horoscope_dob";
    public static final String horoscopeTob = "horoscope_tob";
    public static final String horoscopeLob = "horoscope_lob";
    public static final String aboutMe = "about_me";

    public static final String DEVICEID = "device_id";

    //    Configurations
    public static final String DAILY_MATCH = "daily_match";
    public static final String CHAT_NOTIFICATION = "chat_notification";
    public static final String CUSTOMER_SERVICE = "customer_service";
    public static final String BLOG_RELEASE = "blog_release";
    public static final String SOUND = "sound";
    public static final String REASON = "reason ";

    //    How we work
    public static final String ABOUT_THE_BUREAU = "About The Bureau";
    public static final String WHY_IT_WAS_CREATED = "Why it was created";
    public static final String HOW_IS_IT_DIFFERENT = "How is it different";
    public static final String HOW_THE_APP_WORKS = "How the app works";
    public static final String HISTORY_OF_THE_APPLICATION = "History of the application";

    //    feedback
    public static final String FEEB_BACK = "feedback_msg";

    //    MATCH TAB
    public static final String PASS_BY = "passed_by";
    public static final String USERID_PASSED = "userid_passed";
    public static final String ACTION_TOKEN = "action_taken";
    public static final String USER_ACTION = "user_action";

    public static final String USER_FILE = "userfile";

    public static final String INVALID_USERID = "User Id is invalid";

    //Match of the day Constatns
    public static final String INDIA = "India";
    public static final String createdBy = "created_by";
    public static final String age = "age";
    public static final String location = "location";
    public static final String employed = "Employed";
    public static final String unemployed = "Unemployed";
    public static final String student = "Student";
    public static final String other = "Other";
    public static int badgeCount = 0;
    public static final String Gold_to_add = "gold_to_add";
}