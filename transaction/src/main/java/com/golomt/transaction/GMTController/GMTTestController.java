package com.golomt.transaction.GMTController;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.jsonwebtoken.Claims;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/test")
@Slf4j
public class GMTTestController {

    @GetMapping("/protected")
    public ResponseEntity<Map<String, Object>> testProtectedEndpoint() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Bearer token authentication successful!");
        response.put("authenticated", authentication.isAuthenticated());
        response.put("username", authentication.getName());
        response.put("authorities", authentication.getAuthorities());
        
        if (authentication.getDetails() instanceof Claims) {
            Claims claims = (Claims) authentication.getDetails();
            response.put("userId", claims.get("userId"));
            response.put("email", claims.get("email"));
            response.put("customerId", claims.get("customerId"));
            response.put("tokenType", claims.get("tokenType"));
        }
        
        log.info("Protected endpoint accessed by user: {}", authentication.getName());
        return ResponseEntity.ok(response);
    }
}
