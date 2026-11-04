package com.employee.domain.enums;

public enum LeaveType {
    CASUAL(12),
    SICK(10),
    EARNED(18);

    private final int yearlyQuota;

    LeaveType(int yearlyQuota) {
        this.yearlyQuota = yearlyQuota;
    }

    public int getYearlyQuota() {
        return yearlyQuota;
    }
}