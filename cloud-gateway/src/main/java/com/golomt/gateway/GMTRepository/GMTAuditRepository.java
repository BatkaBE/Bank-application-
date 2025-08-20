package com.golomt.gateway.GMTRepository;

import com.golomt.gateway.GMTEntity.GMTAuditEntity;
import org.springframework.data.jpa.repository.JpaRepository;
public interface GMTAuditRepository extends JpaRepository<GMTAuditEntity, Long> {
}
