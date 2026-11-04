package com.employee.dtos.response;

import com.employee.domain.enums.DocumentType;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class DocumentResponseDto {

    private Long id;

    private Long employeeId;

    private DocumentType documentType;

    private String fileName;

    private String contentType;

    private boolean verified;

    private String verifiedBy;

    private Instant uploadedAt;
}
