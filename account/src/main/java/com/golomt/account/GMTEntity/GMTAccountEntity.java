package com.golomt.account.GMTEntity;

import com.golomt.account.GMTDTO.GMTResponseDTO.GMTCommonDTO.GMTGeneralDTO;
import javax.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;



@Entity
@Table(name = "account")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GMTAccountEntity implements GMTGeneralDTO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String accountNumber;

    @Column(nullable = false)
    private String accountName;

    @Column(nullable = false)
    private String accountType;

    @Column(nullable = false)
    private Double balance;

    @Column(nullable = false)
    private String currencyCode;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Column(nullable = false)
    private String createdBy;

    private String updatedBy;

    private boolean isActive = true;
    private boolean isLocked = false;
    @Version
    private Long version;
}