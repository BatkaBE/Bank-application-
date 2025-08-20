package com.golomt.gateway.GMTEntity;

import lombok.Data;


import javax.persistence.*;

@Entity
@Data
@Table(name = "AUDIT_LOG")
public class GMTAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "auditSeq")
    @Column(name = "ID")
    private Long id;

    @Column(name = "METHOD")
    private String method;

    @Column(name = "URI")
    private String uri;

    @Column(name = "USER_AGENT")
    private String userAgent;

    @Column(name = "IP_ADDRESS")
    private String ipAddress;
}
