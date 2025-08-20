
package com.golomt.auth.GMTRepository;

import com.golomt.auth.GMTConstant.GMTTokenType;
import com.golomt.auth.GMTEntity.GMTTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface GMTTokenRepository extends JpaRepository<GMTTokenEntity, Long> {

    // Basic queries
    Optional<GMTTokenEntity> findByTokenValue(String tokenValue);
    
    List<GMTTokenEntity> findByUserId(Long userId);
    
    List<GMTTokenEntity> findByUsername(String username);
    
    List<GMTTokenEntity> findByTokenType(GMTTokenType tokenType);
    
    List<GMTTokenEntity> findByUserIdAndTokenType(Long userId, GMTTokenType tokenType);
}