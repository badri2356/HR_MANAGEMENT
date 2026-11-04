package com.employee.services.service;

import com.employee.domain.enums.DocumentType;
import com.employee.dtos.response.DocumentDownload;
import com.employee.dtos.response.DocumentResponseDto;
import org.springframework.security.core.Authentication;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface EmployeeDocumentService {

    DocumentResponseDto upload(Long employeeId, DocumentType documentType,
                               MultipartFile file, Authentication authentication);
    List<DocumentResponseDto> list(Long employeeId, Authentication authentication);
    DocumentResponseDto verify(Long documentId, Authentication authentication);
    DocumentDownload download(Long documentId, Authentication authentication);

}
