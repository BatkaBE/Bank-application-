package com.golomt.auth.GMTEntity;

import com.golomt.auth.GMTConstant.GMTUserRole;
import com.golomt.auth.GMTDTO.GMTResponseDTO.GMTCommonDTO.GMTGeneralDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import com.golomt.auth.GMTConstant.GMTUserStatus;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GMTUserEntity implements GMTGeneralDTO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "username", unique = true, nullable = false, length = 50)
    private String username;

    @Column(name = "email", unique = true, nullable = false, length = 100)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Column(name = "first_name", nullable = false, length = 50)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 50)
    private String lastName;

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @Column(name = "customer_id", unique = true, length = 50)
    private String customerId;

    @Column(name = "user_status", nullable = false, length = 20)
    private String userStatus;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @Column(name = "is_locked", nullable = false)
    private Boolean isLocked;

    @Column(name = "is_expired", nullable = false)
    private Boolean isExpired;


    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "created_by", nullable = false, length = 50)
    private String createdBy;

    @Column(name = "updated_by", length = 50)
    private String updatedBy;


    @ElementCollection(fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "gmt_user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "role")
    private Set<GMTUserRole> roles;


    public GMTUserEntity(String username, String email, String passwordHash,
                         String firstName, String lastName, String createdBy) {
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
        this.firstName = firstName;
        this.lastName = lastName;
        this.createdBy = createdBy;
        this.userStatus = GMTUserStatus.ACTIVE.toString();
        this.isActive = true;
        this.isLocked = false;
        this.isExpired = false;

    }


    public String getFullName() {
        return firstName + " " + lastName;
    }

    public boolean hasRole(GMTUserRole role) {
        return roles != null && roles.contains(role);
    }

}