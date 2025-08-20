package com.golomt.gateway.GMTEntity;

import lombok.Data;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "TOTAL_ACCESS")
@Data
public class GMTTotalAccessEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "totalAccesSeq")
    @Column(name = "ID")
    private Long id;

    @Column(name = "METHOD")
    private String method;

    @Column(name = "URI")
    private String uri;

    @Column(name = "LOG_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    private Date logDate;

    @Column(name = "COUNT")
    private Long count;
}
