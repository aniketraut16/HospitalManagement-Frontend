package com.frontend.HospitalManagement.dto.Nurse;

public enum NursePosition {

    HEAD_NURSE("Head Nurse"),
    STAFF_NURSE("Staff Nurse");

    private final String displayName;

    NursePosition(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static NursePosition fromDisplayName(String displayName) {
        if (displayName == null) return null;
        for (NursePosition p : values()) {
            if (p.displayName.equalsIgnoreCase(displayName.trim())) {
                return p;
            }
        }
        return null;
    }
}
