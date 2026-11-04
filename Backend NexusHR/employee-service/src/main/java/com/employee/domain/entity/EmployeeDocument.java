package com.employee.domain.entity;

import com.employee.domain.enums.DocumentType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "employee_documents")
@Getter
@Setter
@NoArgsConstructor
public class EmployeeDocument extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DocumentType documentType;

    @Column(nullable = false)
    private String fileName;

    @Column(nullable = false)
    private String filePath;

    @Column
    private String contentType;

    private boolean verified = false;

    private String verifiedBy;


}
