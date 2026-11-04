package com.employee.services.serviceImpl;

import com.employee.domain.entity.Employee;
import com.employee.domain.entity.EmployeeDocument;
import com.employee.domain.enums.DocumentType;
import com.employee.domain.enums.EmployeeStatus;
import com.employee.domain.enums.NotificationType;
import com.employee.dtos.response.DocumentDownload;
import com.employee.dtos.response.DocumentResponseDto;
import com.employee.events.NotificationEvent;
import com.employee.exceptions.ResourceNotFoundException;
import com.employee.repositories.EmployeeDocumentRepository;
import com.employee.repositories.EmployeeRepository;
import com.employee.services.service.EmployeeDocumentService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.io.Resource;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
public class EmployeeDocumentServiceImpl implements EmployeeDocumentService {

    private final EmployeeRepository employeeRepository;
    private final EmployeeDocumentRepository documentRepository;
    private final FileStorageService fileStorageService;
    private final ApplicationEventPublisher eventPublisher;

    public EmployeeDocumentServiceImpl(EmployeeRepository employeeRepository,
                                       EmployeeDocumentRepository documentRepository,
                                       FileStorageService fileStorageService, ApplicationEventPublisher eventPublisher) {
        this.employeeRepository = employeeRepository;
        this.documentRepository = documentRepository;
        this.fileStorageService = fileStorageService;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional
    public DocumentResponseDto upload(Long employeeId, DocumentType documentType,
                                      MultipartFile file, Authentication authentication) {

        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id " + employeeId));

        checkOwnerOrHr(employee, authentication);

        if (employee.getStatus() == EmployeeStatus.EXITED) {
            throw new IllegalStateException("Cannot upload documents for an exited employee");
        }

        String path = fileStorageService.store(file, employeeId);

        // Same type already uploaded? Replace it. Otherwise create a new one.
        EmployeeDocument document = documentRepository
                .findByEmployeeIdAndDocumentType(employeeId, documentType)
                .map(existing -> {
                    fileStorageService.delete(existing.getFilePath());
                    return existing;
                })
                .orElseGet(() -> {
                    EmployeeDocument created = new EmployeeDocument();
                    created.setEmployee(employee);
                    created.setDocumentType(documentType);
                    return created;
                });

        document.setFileName(StringUtils.cleanPath(file.getOriginalFilename()));
        document.setFilePath(path);
        document.setContentType(file.getContentType());
        document.setVerified(false);       // a new file must be checked again
        document.setVerifiedBy(null);

        return toResponse(documentRepository.save(document));
    }

    private void checkOwnerOrHr(Employee employee, Authentication authentication) {

        boolean isHrOrAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_HR") || a.getAuthority().equals("ROLE_ADMIN"));
        boolean isOwner = employee.getEmail().equalsIgnoreCase(authentication.getName());

        if (!isHrOrAdmin && !isOwner) {
            throw new AccessDeniedException("You can only upload your own documents");
        }
    }

    private DocumentResponseDto toResponse(EmployeeDocument document) {

        DocumentResponseDto response = new DocumentResponseDto();
        response.setId(document.getId());
        response.setEmployeeId(document.getEmployee().getId());
        response.setDocumentType(document.getDocumentType());
        response.setFileName(document.getFileName());
        response.setContentType(document.getContentType());
        response.setVerified(document.isVerified());
        response.setVerifiedBy(document.getVerifiedBy());
        response.setUploadedAt(document.getCreatedAt());
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public List<DocumentResponseDto> list(Long employeeId, Authentication authentication) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id " + employeeId));

        checkOwnerOrHr(employee, authentication);

        return documentRepository.findByEmployeeId(employeeId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public DocumentResponseDto verify(Long documentId, Authentication authentication) {
        EmployeeDocument document = documentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found with id " + documentId));

        document.setVerified(true);
        document.setVerifiedBy(authentication.getName());

        EmployeeDocument save = documentRepository.save(document);

        eventPublisher.publishEvent(new NotificationEvent(
                document.getEmployee().getEmail(),
                NotificationType.DOCUMENT_VERIFIED,
                "Your " + save.getDocumentType() + " document has been verified"
        ));

        return toResponse(save);
    }

    @Override
    @Transactional(readOnly = true)
    public DocumentDownload download(Long documentId, Authentication authentication) {
        EmployeeDocument document = documentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found with id " + documentId));

        checkOwnerOrHr(document.getEmployee(), authentication);

        Resource resource = fileStorageService.load(document.getFilePath());
        return new DocumentDownload(resource, document.getFileName(), document.getContentType());
    }
}
