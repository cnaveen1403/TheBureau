package com.bureau.bureauapp.helperclasses;

import android.util.Log;

public class FamilyOriginInfo {
    private String familyOriginId;
    private String familyOriginName;


    public void setFamilyOriginId(String familyOriginId) {
        this.familyOriginId = familyOriginId;
        Log.d("", " ==== > familyOriginId : " + familyOriginId + ", familyOriginName : " + familyOriginName);
    }

    public void setFamilyOriginName(String familyOriginName) {
        this.familyOriginName = familyOriginName;
    }

    public String getFamilyOriginId() {
        return familyOriginId;
    }

    public String getFamilyOriginName() {
        return familyOriginName;
    }
}
