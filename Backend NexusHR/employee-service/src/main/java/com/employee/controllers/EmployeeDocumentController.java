package com.employee.controllers;

import com.employee.domain.enums.DocumentType;
import com.employee.dtos.response.ApiResponse;
import com.employee.dtos.response.DocumentDownload;
import com.employee.messages.SuccessMessages;
import com.employee.services.service.EmployeeDocumentService;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api")
public class EmployeeDocumentController {

    private final EmployeeDocumentService documentService;

    public EmployeeDocumentController(EmployeeDocumentService documentService) {
        this.documentService = documentService;
    }

    @PostMapping(value = "/employees/{employeeId}/documents", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadDocument(@PathVariable Long employeeId,
                                            @RequestParam("documentType") DocumentType documentType,
                                            @RequestParam("file") MultipartFile file,
                                            Authentication authentication) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new ApiResponse<>(Boolean.FALSE,
                        documentService.upload(employeeId, documentType, file, authentication),
                        SuccessMessages.DATA_SAVED_SUCCESSFULLY));
    }

    @GetMapping("/employees/{employeeId}/documents")
    public ResponseEntity<?> listDocuments(@PathVariable Long employeeId, Authentication authentication) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new ApiResponse<>(Boolean.FALSE,
                        documentService.list(employeeId, authentication),
                        SuccessMessages.DATA_FETCHED_SUCCESSFULLY));
    }

    @PatchMapping("/documents/{documentId}/verify")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ResponseEntity<?> verifyDocument(@PathVariable Long documentId, Authentication authentication) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new ApiResponse<>(Boolean.FALSE,
                        documentService.verify(documentId, authentication),
                        SuccessMessages.DATA_UPDATED_SUCCESSFULLY));
    }

    @GetMapping("/documents/{documentId}/download")
    public ResponseEntity<Resource> downloadDocument(@PathVariable Long documentId, Authentication authentication) {
        DocumentDownload download = documentService.download(documentId, authentication);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(download.contentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + download.fileName() + "\"")
                .body(download.resource());
    }
}