
    package com.golomt.auth.GMTEntity;

    import com.golomt.auth.GMTConstant.GMTTokenType;
    import lombok.AllArgsConstructor;
    import lombok.Data;
    import lombok.NoArgsConstructor;
    import org.hibernate.annotations.CreationTimestamp;

    import javax.persistence.*;
    import java.time.LocalDateTime;

    @Entity
    @Table(name = "gmt_token")
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public class GMTTokenEntity {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        @Column(name = "id")
        private Long id;

        @Column(name = "token_value", nullable = false, length = 1000)
        private String tokenValue;

        @Column(name = "token_type", nullable = false, length = 20)
        @Enumerated(EnumType.STRING)
        private GMTTokenType tokenType;

        @Column(name = "user_id", nullable = false)
        private Long userId;

        @Column(name = "username", nullable = false, length = 50)
        private String username;

        @Column(name = "ip_address", length = 45)
        private String ipAddress;

        @Column(name = "user_agent", length = 500)
        private String userAgent;

        @Column(name = "device_info", length = 200)
        private String deviceInfo;

        @Column(name = "expires_at", nullable = false)
        private boolean isRevoked = false;

        @CreationTimestamp
        @Column(name = "created_at", updatable = false)
        private LocalDateTime createdAt;

        public GMTTokenEntity(String tokenValue, GMTTokenType tokenType, Long userId,
                             String username, LocalDateTime expiresAt) {
            this.tokenValue = tokenValue;
            this.tokenType = tokenType;
            this.userId = userId;
            this.username = username;

        }

        public boolean isAccessToken() {
            return tokenType == GMTTokenType.ACCESS_TOKEN;
        }

        public boolean isRefreshToken() {
            return tokenType == GMTTokenType.REFRESH_TOKEN;
        }

        public boolean getIsRevoked() {
            return isRevoked;
        }
        public void setIsRevoked(boolean isRevoked) {
            this.isRevoked = isRevoked;
        }
    }