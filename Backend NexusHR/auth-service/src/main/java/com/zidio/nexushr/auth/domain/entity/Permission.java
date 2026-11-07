package com.zidio.nexushr.auth.domain.entity;

import com.zidio.nexushr.auth.domain.enums.PermissionCode;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Permission extends BaseEntity {

    @Enumerated(EnumType.STRING)
    private PermissionCode code;
    private String description;
}
