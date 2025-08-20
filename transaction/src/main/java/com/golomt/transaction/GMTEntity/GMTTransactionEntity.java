package com.golomt.transaction.GMTEntity;

import com.golomt.transaction.GMTDTO.GMTResponseDTO.GMTCommonDTO.GMTGeneralDTO;
import javax.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "transaction")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GMTTransactionEntity implements GMTGeneralDTO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String transactionId;

    @Column(nullable = false)
    private String fromAccountNumber;

    @Column(nullable = false)
    private String toAccountNumber;

    @Column(nullable = false, precision = 19, scale = 2)
    private Double amount;

    @Column(nullable = false)
    private String currencyCode;

    @Column(nullable = false)
    private String transactionType; // TRANSFER, DEPOSIT, WITHDRAWAL, PAYMENT

    @Column(nullable = false)
    private String status; // PENDING, COMPLETED, FAILED, CANCELLED

    private String description;

    private String reference;

    @Column(nullable = false, name = "to_user_id")
    private String toUserId;

    @Column(nullable = false, precision = 19, scale = 2)
    private Double fee;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Column(nullable = false, name = "from_user_id")
    private String createdBy;

    private String updatedBy;

    private LocalDateTime processedAt;

    private String failureReason;
}