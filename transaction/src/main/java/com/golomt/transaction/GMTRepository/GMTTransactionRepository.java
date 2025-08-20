package com.golomt.transaction.GMTRepository;

import com.golomt.transaction.GMTConstant.GMTTransactionStatus;
import com.golomt.transaction.GMTEntity.GMTTransactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface GMTTransactionRepository extends JpaRepository<GMTTransactionEntity, Long> {

    GMTTransactionEntity findByTransactionId(String transactionId);

    // FIXED: Changed from findByFromAccountAndStatus to findByFromAccountNumberAndStatus
    List<GMTTransactionEntity> findByFromAccountNumberAndStatus(String fromAccountNumber, String status);

    List<GMTTransactionEntity> findByToAccountNumberAndStatus(String toAccountNumber, String status);

    List<GMTTransactionEntity> findByStatusAndCreatedAtBetween(
            GMTTransactionStatus status,
            LocalDateTime startDate,
            LocalDateTime endDate
    );

    @Transactional(Transactional.TxType.SUPPORTS)
    @Query("SELECT t FROM GMTTransactionEntity t WHERE t.transactionId = :transactionId")
    GMTTransactionEntity getTransactionByTransactionId(@Param("transactionId") String transactionId);

    @Transactional(Transactional.TxType.SUPPORTS)
    @Query("SELECT t FROM GMTTransactionEntity t WHERE t.fromAccountNumber = :accountNumber OR t.toAccountNumber = :accountNumber ORDER BY t.createdAt DESC")
    List<GMTTransactionEntity> getAllTransactions(@Param("accountNumber") String accountNumber);

    // FIXED: Changed from existsByReferenceNumber to existsByReference (matching entity property)
    boolean existsByReference(String reference);

    // Additional queries for user-specific transactions
    @Transactional(Transactional.TxType.SUPPORTS)
    @Query("SELECT t FROM GMTTransactionEntity t WHERE t.createdBy = :userId AND (t.fromAccountNumber = :accountNumber OR t.toAccountNumber = :accountNumber) ORDER BY t.createdAt DESC")
    List<GMTTransactionEntity> getUserTransactionsByAccountNumber(@Param("userId") String userId, @Param("accountNumber") String accountNumber);

    @Transactional(Transactional.TxType.SUPPORTS)
    @Query("SELECT t FROM GMTTransactionEntity t WHERE t.createdBy = :userId ORDER BY t.createdAt DESC")
    List<GMTTransactionEntity> getUserTransactionsByUser(@Param("userId") String userId);

    @Transactional(Transactional.TxType.SUPPORTS)
    @Query("SELECT t FROM GMTTransactionEntity t WHERE t.createdBy = :userId AND t.status = :status ORDER BY t.createdAt DESC")
    List<GMTTransactionEntity> getUserTransactionsByStatus(@Param("userId") String userId, @Param("status") String status);

    @Transactional(Transactional.TxType.SUPPORTS)
    @Query("SELECT t FROM GMTTransactionEntity t WHERE t.createdBy = :userId AND t.createdAt BETWEEN :startDate AND :endDate ORDER BY t.createdAt DESC")
    List<GMTTransactionEntity> getUserTransactionsByDateRange(@Param("userId") String userId, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Transactional(Transactional.TxType.SUPPORTS)
    @Query("SELECT COUNT(t) FROM GMTTransactionEntity t WHERE t.createdBy = :userId")
    Long countByCreatedBy(@Param("userId") String userId);

    @Transactional(Transactional.TxType.SUPPORTS)
    @Query("SELECT SUM(t.amount) FROM GMTTransactionEntity t WHERE t.createdBy = :userId")
    Double sumAmountByCreatedBy(@Param("userId") String userId);

    @Transactional(Transactional.TxType.SUPPORTS)
    @Query("SELECT t FROM GMTTransactionEntity t WHERE t.status = 'PENDING' ORDER BY t.createdAt ASC")
    List<GMTTransactionEntity> findPendingTransactions();

    @Transactional(Transactional.TxType.SUPPORTS)
    @Query("SELECT t FROM GMTTransactionEntity t WHERE t.status = 'FAILED' ORDER BY t.createdAt DESC")
    List<GMTTransactionEntity> findFailedTransactions();

    // FIXED: Added proper @Param annotations and changed return type to List
    @Transactional(Transactional.TxType.SUPPORTS)
    @Query("SELECT t FROM GMTTransactionEntity t WHERE t.amount BETWEEN :minAmount AND :maxAmount ORDER BY t.createdAt DESC")
    List<GMTTransactionEntity> getTransactionsByAmountRange(@Param("minAmount") Double minAmount, @Param("maxAmount") Double maxAmount);

    @Transactional(Transactional.TxType.SUPPORTS)
    @Query("SELECT t FROM GMTTransactionEntity t WHERE t.createdBy = :createdBy OR t.toUserId = :toUserId ORDER BY t.createdAt DESC")
    List<GMTTransactionEntity> getUserTransactionsByRelatedUsers(@Param("toUserId")String toUserId, @Param("createdBy") String createdBy);


}