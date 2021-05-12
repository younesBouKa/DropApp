package server.security.jwt;

import io.jsonwebtoken.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import server.data.IUser;
import server.user.services.CustomUserDetails;
import org.springframework.security.core.Authentication;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtUtils {
    private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);

    @Value("${app.jwtSecret}")
    private String jwtSecret;

    @Value("${app.jwtExpirationMinute}")
    private int jwtExpirationMinute;

    public String generateJwtToken(Authentication authentication) {

        CustomUserDetails userPrincipal = (CustomUserDetails) authentication.getPrincipal();
        Map<String, Object> privateClaims = new HashMap<>();
        privateClaims.put("uid",userPrincipal.getId());
        privateClaims.put("usr",userPrincipal.getUsername());
        privateClaims.put("eml",userPrincipal.getEmail());
        privateClaims.put("hdr",userPrincipal.getHomeDirectory());
        return Jwts.builder()
                .setId(userPrincipal.getId())
                .setSubject((userPrincipal.getUsername()))
                .setIssuedAt(new Date())
                .setExpiration(new Date((new Date()).getTime() + jwtExpirationMinute*60*1000))
                .addClaims(privateClaims)
                .signWith(SignatureAlgorithm.HS512, jwtSecret)
                .compact();
    }

    public String generateJwtToken(IUser userPrincipal) {
        Map<String, Object> privateClaims = new HashMap<>();
        privateClaims.put("uid",userPrincipal.getId());
        privateClaims.put("usr",userPrincipal.getUsername());
        privateClaims.put("eml",userPrincipal.getEmail());
        privateClaims.put("hdr",userPrincipal.getHomeDirectory());
        return Jwts.builder()
                .setId(userPrincipal.getId())
                .setSubject((userPrincipal.getUsername()))
                .setIssuedAt(new Date())
                .setExpiration(new Date((new Date()).getTime() + jwtExpirationMinute*60*1000))
                .addClaims(privateClaims)
                .signWith(SignatureAlgorithm.HS512, jwtSecret)
                .compact();
    }

    public String getUserNameFromJwtToken(String token) {
        return Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(token).getBody().getSubject();
    }

    public String getUserIdFromJwtToken(String token) {
        return Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(token).getBody().getId();
    }

    public boolean validateJwtToken(String authToken) {
        try {
            Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(authToken);
            return true;
        } catch (SignatureException e) {
            logger.error("Invalid JWT signature: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            logger.error("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            logger.error("JWT token is expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            logger.error("JWT token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.error("JWT claims string is empty: {}", e.getMessage());
        }

        return false;
    }

    public String getJwtSecret() {
        return jwtSecret;
    }

    public void setJwtSecret(String jwtSecret) {
        this.jwtSecret = jwtSecret;
    }

    public int getJwtExpirationMinute() {
        return jwtExpirationMinute;
    }

    public void setJwtExpirationMinute(int jwtExpirationMinute) {
        this.jwtExpirationMinute = jwtExpirationMinute;
    }
}
