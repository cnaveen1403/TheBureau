package com.bureau.bureauapp.model;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MatchInfo {
    private final String LOG_TAG = MatchInfo.class.getSimpleName();

    private String profileCreatedBy = "", profileName = "", age = "", location = "", height_feet = "", height_inch = "", motherToungue = "", religion = "", familyOrigin = "",
            specification = "", education = "", honor = "", major = "", college = "", year = "", occupation = "", employer = "", diet = "", smoking = "", drinking = "", yearsInUS = "",
            legalStatus = "", profileDOB = "", timeOfBirth = "", birthLocation = "", aboutMe = "", profileUserId = "", userAction = "", defaultValue = "-";
    private JSONArray profileImage;

    public MatchInfo() {
    }

    public MatchInfo(JSONObject profileMatchInfo) {
        try {
            this.profileUserId = profileMatchInfo.getString("userid");
            this.profileCreatedBy = profileMatchInfo.getString("created_by");
            this.profileName = profileMatchInfo.getString("first_name")/*+" "+profileMatchInfo.getString("last_name")*/;
            this.age = profileMatchInfo.getString("age");
            this.location = profileMatchInfo.getString("location");
            this.height_feet = profileMatchInfo.getString("height_feet");
            this.height_inch = profileMatchInfo.getString("height_inch");
            this.motherToungue = profileMatchInfo.getString("mother_tongue");
            this.religion = profileMatchInfo.getString("religion_name");
            this.familyOrigin = profileMatchInfo.getString("family_origin_name");
            if (profileMatchInfo.has("specification_name")) {
                this.specification = profileMatchInfo.getString("specification_name");
            }
            this.education = profileMatchInfo.getString("highest_education");
            this.honor = profileMatchInfo.getString("honors");
            this.major = profileMatchInfo.getString("major");
            this.college = profileMatchInfo.getString("college");
            this.year = profileMatchInfo.getString("graduated_year");
            this.occupation = profileMatchInfo.getString("position_title");
            this.employer = profileMatchInfo.getString("company");
            this.diet = profileMatchInfo.getString("diet");
            this.smoking = profileMatchInfo.getString("smoking");
            this.drinking = profileMatchInfo.getString("drinking");
            this.yearsInUS = profileMatchInfo.getString("years_in_usa");
            this.legalStatus = profileMatchInfo.getString("legal_status");
            this.profileDOB = profileMatchInfo.getString("horoscope_dob");
            this.timeOfBirth = profileMatchInfo.getString("horoscope_tob");
            this.birthLocation = profileMatchInfo.getString("horoscope_lob");
            this.aboutMe = profileMatchInfo.getString("about_me");
            this.profileImage = profileMatchInfo.getJSONArray("img_url");
            if (profileMatchInfo.has("user_action")) {
                this.userAction = profileMatchInfo.getString("user_action");
            }

        } catch (JSONException e) {

            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }
    }

    public String getProfileUserId() {
        return profileUserId;
    }

    public String getProfileCreatedBy() {
        return getfromattedValue(profileCreatedBy);
    }

    public String getProfileName() {
        return getfromattedValue(profileName);
    }

    public String getAge() {
        return getfromattedValue(age);
    }

    public String getLocation() {
        return getfromattedValue(location);
    }

    public String getHeight_feet() {
        return getfromattedValue(height_feet);
    }

    public String getHeight_inch() {
        return getfromattedValue(height_inch);
    }

    public String getMotherToungue() {
        return getfromattedValue(motherToungue);
    }

    public String getReligion() {
        return getfromattedValue(religion);
    }

    public String getFamilyOrigin() {
        return getfromattedValue(familyOrigin);
    }

    public String getSpecification() {
        return getfromattedValue(specification);
    }

    public String getEducation() {
        return getfromattedValue(education);
    }

    public String getHonor() {
        return getfromattedValue(honor);
    }

    public String getMajor() {
        return getfromattedValue(major);
    }

    public String getCollege() {
        return getfromattedValue(college);
    }

    public String getYear() {
        return getfromattedValue(year);
    }

    public String getOccupation() {
        return getfromattedValue(occupation);
    }

    public String getEmployer() {
        return getfromattedValue(employer);
    }

    public String getDiet() {
        return getfromattedValue(diet);
    }

    public String getSmoking() {
        return getfromattedValue(smoking);
    }

    public String getDrinking() {
        return getfromattedValue(drinking);
    }

    public String getYearsInUS() {
        return getfromattedValue(yearsInUS);
    }

    public String getLegalStatus() {
        return getfromattedValue(legalStatus);
    }

    public String getProfileDOB() {
        return getfromattedValue(profileDOB);
    }

    public String getTimeOfBirth() {
        return getfromattedValue(timeOfBirth);
    }

    public String getBirthLocation() {
        return getfromattedValue(birthLocation);
    }

    public String getAboutMe() {
        return getfromattedValue(aboutMe);
    }

    public JSONArray getProfileImage() {
        return profileImage;
    }

    public String getUserAction() {
        return getfromattedValue(userAction);
    }

    public String getfromattedValue(String value) {
        if (value.equals(""))
            return defaultValue;

        return value;
    }
}
