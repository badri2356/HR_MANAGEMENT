package com.employee.repositories;

import com.employee.domain.entity.EmployeeDocument;
import com.employee.domain.enums.DocumentType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EmployeeDocumentRepository extends JpaRepository<EmployeeDocument, Long> {

    // Find all documents uploaded by a particular employee
    List<EmployeeDocument> findByEmployeeId(Long employeeId);

    // Find a particular document using employee ID and document type
    Optional<EmployeeDocument> findByEmployeeIdAndDocumentType(Long employeeId, DocumentType documentType);

    // Check whether an employee has uploaded a particular document type
    boolean existsByEmployeeIdAndDocumentType(Long employeeId, DocumentType documentType);

    // Find all documents that are verified or unverified
    List<EmployeeDocument> findByVerified(boolean verified);

    // Find all documents uploaded by an employee that are pending verification
    List<EmployeeDocument> findByEmployeeIdAndVerified(Long employeeId, boolean verified);
}
