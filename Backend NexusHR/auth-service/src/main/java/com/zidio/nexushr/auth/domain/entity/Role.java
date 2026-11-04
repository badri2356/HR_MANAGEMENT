package com.zidio.nexushr.auth.domain.entity;

import com.zidio.nexushr.auth.domain.enums.RoleName;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "roles")
@NoArgsConstructor
@Getter
@Setter
public class Role extends BaseEntity {
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private RoleName name;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "role_permission",
            joinColumns = @JoinColumn(name = "role_id"),
            inverseJoinColumns = @JoinColumn(name = "permission_id")

    )
    private Set<Permission> permissions = new HashSet<>();

    private String description;

}
