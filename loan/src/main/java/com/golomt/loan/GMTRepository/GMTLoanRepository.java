package com.golomt.loan.GMTRepository;

import com.golomt.loan.GMTEntity.GMTLoanEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface GMTLoanRepository extends JpaRepository<GMTLoanEntity, Long> {

    GMTLoanEntity findByLoanId(String loanId);

    List<GMTLoanEntity> findByUserId(String userId);

    List<GMTLoanEntity> findByAccountNumber(String accountNumber);

    List<GMTLoanEntity> findByStatus(String status);

    List<GMTLoanEntity> findByLoanType(String loanType);

    List<GMTLoanEntity> findByUserIdAndStatus(String userId, String status);

    @Transactional(Transactional.TxType.SUPPORTS)
    @Query("SELECT l FROM GMTLoanEntity l WHERE l.loanId = :loanId")
    GMTLoanEntity getLoanByLoanId(@Param("loanId") String loanId);

    @Transactional(Transactional.TxType.SUPPORTS)
    @Query("SELECT l FROM GMTLoanEntity l WHERE l.userId = :userId ORDER BY l.createdAt DESC")
    List<GMTLoanEntity> getAllLoansByUserId(@Param("userId") String userId);

    @Transactional(Transactional.TxType.SUPPORTS)
    @Query("SELECT l FROM GMTLoanEntity l WHERE l.status = :status AND l.createdAt BETWEEN :startDate AND :endDate")
    List<GMTLoanEntity> getLoansByStatusAndDateRange(
            @Param("status") String status,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    @Transactional(Transactional.TxType.SUPPORTS)
    @Query("SELECT l FROM GMTLoanEntity l WHERE l.loanAmount BETWEEN :minAmount AND :maxAmount")
    List<GMTLoanEntity> getLoansByAmountRange(
            @Param("minAmount") Double minAmount,
            @Param("maxAmount") Double maxAmount
    );

    boolean existsByLoanId(String loanId);

    @Transactional(Transactional.TxType.SUPPORTS)
    @Query("SELECT l FROM GMTLoanEntity l WHERE l.status = 'ACTIVE' AND l.remainingBalance > 0")
    List<GMTLoanEntity> getActiveLoans();
}
