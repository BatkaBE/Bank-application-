package com.golomt.loan.GMTEntity;

import com.golomt.loan.GMTDTO.GMTResponseDTO.GMTCommonDTO.GMTGeneralDTO;
import javax.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "loan")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GMTLoanEntity implements GMTGeneralDTO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String loanId;

    @Column(nullable = false)
    private String userId;

    @Column(nullable = false)
    private String accountNumber;

    @Column(nullable = false, precision = 19, scale = 2)
    private Double loanAmount;

    @Column(nullable = false, precision = 19, scale = 2)
    private Double interestRate;

    @Column(nullable = false)
    private Integer loanTerm; // in months

    @Column(nullable = false)
    private String loanType; // PERSONAL, BUSINESS, MORTGAGE, AUTO, STUDENT, CONSUMER

    @Column(nullable = false)
    private String status; // PENDING, APPROVED, REJECTED, ACTIVE, COMPLETED, DEFAULTED, CANCELLED

    @Column(nullable = false, precision = 19, scale = 2)
    private Double monthlyPayment;

    @Column(nullable = false, precision = 19, scale = 2)
    private Double totalAmount;

    @Column(nullable = false, precision = 19, scale = 2)
    private Double remainingBalance;

    @Column(nullable = false)
    private String currencyCode;

    @Column(nullable = false)
    private String purpose;

    private String collateral;

    private String guarantor;

    @Column(nullable = false)
    private LocalDateTime startDate;

    private LocalDateTime endDate;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Column(nullable = false)
    private String createdBy;

    private String updatedBy;

    private LocalDateTime approvedAt;

    private String approvedBy;

    private String rejectionReason;

    private String notes;
}
