package com.golomt.account.GMTRepository;

import com.golomt.account.GMTEntity.GMTAccountEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.net.ContentHandler;
import java.util.List;
import java.util.Optional;

@Repository
public interface GMTAccountRepository extends JpaRepository<GMTAccountEntity, Long> {

    Optional<GMTAccountEntity> findByAccountNumber(String accountNumber);

    List<GMTAccountEntity> findByAccountType(String accountType);

    List<GMTAccountEntity> findByCurrencyCode(String currencyCode);

    List<GMTAccountEntity> findByCreatedBy(String createdBy);

    boolean existsByAccountNumber(String accountNumber);

    @Query("SELECT COALESCE(SUM(a.balance), 0.0) FROM GMTAccountEntity a WHERE a.currencyCode = :currencyCode")
    Double sumBalanceByCurrencyCode(@Param("currencyCode") String currencyCode);

}