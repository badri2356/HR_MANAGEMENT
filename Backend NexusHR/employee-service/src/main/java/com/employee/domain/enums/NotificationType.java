package com.employee.domain.enums;

import lombok.Getter;

@Getter
public enum NotificationType {

    LEAVE_APPLIED("New leave request"),
    LEAVE_APPROVED("Leave approved"),
    LEAVE_REJECTED("Leave rejected"),
    DOCUMENT_VERIFIED("Document verified"),
    EMPLOYEE_APPROVED("Welcome aboard!"),
    PAYROLL_PAID("Payslip ready");

    private final String title;

    NotificationType(String title) {
        this.title = title;
    }

}
