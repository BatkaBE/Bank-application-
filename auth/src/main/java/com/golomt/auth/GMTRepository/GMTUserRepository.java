
package com.golomt.auth.GMTRepository;

import com.golomt.auth.GMTEntity.GMTUserEntity;
import com.golomt.auth.GMTConstant.GMTUserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface GMTUserRepository extends JpaRepository<GMTUserEntity, Long> {

    // Basic queries
    Optional<GMTUserEntity> findByUsername(String username);
    
    Optional<GMTUserEntity> findByEmail(String email);
    
    Optional<GMTUserEntity> findByCustomerId(String customerId);
    
    Optional<GMTUserEntity> findByUsernameOrEmail(String username, String email);
    
    boolean existsByUsername(String username);
    
    boolean existsByEmail(String email);
    
    boolean existsByCustomerId(String customerId);

    // Status and activation queries
    List<GMTUserEntity> findByIsActive(Boolean isActive);

    
    List<GMTUserEntity> findByIsLocked(Boolean isLocked);
    

    // Role-based queries
    @Query("SELECT u FROM GMTUserEntity u JOIN u.roles r WHERE r = :role")
    List<GMTUserEntity> findByRole(@Param("role") GMTUserRole role);
    
    @Query("SELECT u FROM GMTUserEntity u JOIN u.roles r WHERE r IN :roles")
    List<GMTUserEntity> findByRoles(@Param("roles") List<GMTUserRole> roles);

    
    @Modifying
    @Transactional
    @Query("UPDATE GMTUserEntity u SET u.isActive = :isActive, u.updatedBy = :updatedBy WHERE u.id = :userId")
    int updateActiveStatus(@Param("userId") Long userId, 
                          @Param("isActive") Boolean isActive, 
                          @Param("updatedBy") String updatedBy);

    // Statistical queries
    @Query("SELECT COUNT(u) FROM GMTUserEntity u WHERE u.isActive = true")
    Long countActiveUsers();
    
    @Query("SELECT COUNT(u) FROM GMTUserEntity u WHERE u.isLocked = true")
    Long countLockedUsers();
    
    @Query("SELECT COUNT(u) FROM GMTUserEntity u JOIN u.roles r WHERE r = :role")
    Long countUsersByRole(@Param("role") GMTUserRole role);
    
    @Query("SELECT COUNT(u) FROM GMTUserEntity u WHERE u.createdAt BETWEEN :startDate AND :endDate")
    Long countUsersCreatedBetween(@Param("startDate") LocalDateTime startDate, 
                                 @Param("endDate") LocalDateTime endDate);

    // Validation queries
    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END FROM GMTUserEntity u WHERE u.username = :username AND u.id != :excludeId")
    boolean existsByUsernameExcludingId(@Param("username") String username, @Param("excludeId") Long excludeId);
    
    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END FROM GMTUserEntity u WHERE u.email = :email AND u.id != :excludeId")
    boolean existsByEmailExcludingId(@Param("email") String email, @Param("excludeId") Long excludeId);

    // Search queries
    @Query("SELECT u FROM GMTUserEntity u WHERE " +
           "LOWER(u.username) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(u.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<GMTUserEntity> searchUsers(@Param("searchTerm") String searchTerm);

    @Query("SELECT u FROM GMTUserEntity u WHERE u.createdAt BETWEEN :startDate AND :endDate ORDER BY u.createdAt DESC")
    List<GMTUserEntity> findUsersCreatedBetween(@Param("startDate") LocalDateTime startDate, 
                                               @Param("endDate") LocalDateTime endDate);
}