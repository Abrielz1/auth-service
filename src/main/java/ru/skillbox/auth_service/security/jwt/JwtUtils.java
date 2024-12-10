package ru.skillbox.auth_service.security.jwt;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.UnsupportedJwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.skillbox.auth_service.app.entity.User;
import ru.skillbox.auth_service.app.entity.model.RoleType;
import ru.skillbox.auth_service.exception.exceptions.BadRequestException;
import ru.skillbox.auth_service.web.dto.ValidateUserDetails;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.Map;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtUtils {

    @Value("${app.jwt.secret}")
    private String someSecretKey;

    @Value("${app.jwt.tokenExpiration}")
    private Duration tokenExpiration;

    private final ObjectMapper objectMapper;

    private static final String EMAIL = "email";

    private static final String UUID = "uuid";

    private static final String ROLES = "roles";

    public String generateTokenFromUser(User user) {

        Date issuedAt = new Date();

        return Jwts.builder()
                .setSubject(user.getEmail())
                .claim(UUID, user.getUuid())
                .claim(EMAIL, user.getEmail())
                .claim(ROLES, user.getRoles())
                .setIssuedAt(issuedAt)
                .setExpiration(new Date(issuedAt.getTime() + tokenExpiration.toMillis()))
                .signWith(SignatureAlgorithm.HS512, someSecretKey)
                .compact();
    }

    public String generateTokenFromValidateUserDetails(ValidateUserDetails user) {

        return Jwts.builder()
                .setSubject(user.getEmail())
                .claim(UUID, user.getUuid())
                .claim(EMAIL, user.getEmail())
                .claim(ROLES, user.getRoles())
                .setIssuedAt(user.getIat())
                .setExpiration(user.getExp())
                .signWith(SignatureAlgorithm.HS512, someSecretKey)
                .compact();
    }

    public String getEmail(String token) {
        return Jwts.parser().setSigningKey(someSecretKey)
                .parseClaimsJws(token).getBody().getSubject();
    }

    public Boolean validateToken(String authToken) {

        try {
            Jwts.parser().setSigningKey(someSecretKey).parseClaimsJws(authToken);

            return true;
        } catch (SignatureException e) {
            log.error("%nSignature is Invalid: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            log.error("%nToken is Invalid: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            log.error("%nToken is Expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.error("%nToken is Unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("%nClaims string is Empty: {}", e.getMessage());
        }

        return false;
    }

    public ValidateUserDetails getUserFromToken(String token) {

        String payload = new String(Base64.getDecoder().decode(this.shredToken(token)[1]));
        ValidateUserDetails userDetails = new ValidateUserDetails();

        Map claims;

        try {

            claims = objectMapper.readValue(payload, Map.class);
        } catch (JsonProcessingException e) {
            log.info("%nVia Security Service getUserFromToken get null or empty or malformed token: %s !".formatted(token));
            e.printStackTrace();
            throw new BadRequestException("%nNo payload in token or token may be empty or null!");
        }

        userDetails.setEmail(claims.get(EMAIL).toString());
        userDetails.setUuid(claims.get(UUID).toString());
        userDetails.setRoles(claims.get(ROLES).toString().equals("ADMIN") ? Set.of(RoleType.ADMIN) : Set.of(RoleType.USER));
        userDetails.setIat(new Date(Timestamp.from(Instant.ofEpochSecond((Integer) claims.get("iat"))).getTime()));
        userDetails.setExp(new Date(Timestamp.from(Instant.ofEpochSecond((Integer) claims.get("exp"))).getTime()));

        return userDetails;
    }

    public String getHash(String token) {

        return this.shredToken(token)[2];
    }

    private String[] shredToken(String token) {

        try {

            String[] tokenParts = token.split("\\.");

            if (tokenParts.length != 3) {

                throw new IllegalArgumentException("%nInvalid JWT token format");
            }

            return tokenParts;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new String[0];
    }
}

