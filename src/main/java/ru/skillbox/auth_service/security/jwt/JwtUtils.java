package ru.skillbox.auth_service.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.skillbox.auth_service.security.service.AppUserDetails;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class JwtUtils {

    @Value("${app.jwt.secret}")
    private String secretKey;  // Секретный ключ из application.properties

    // Метод для создания токена
    public String generateToken(AppUserDetails userDetails) {

        List<String> listOfUserDetail = new ArrayList<>();

        String role = userDetails.getAuthorities().toString().substring(6,
                userDetails.getAuthorities().toString().length() - 1);

        listOfUserDetail.add(userDetails.getUUID());
        listOfUserDetail.add(role);

        return Jwts// Субъект (пользователь)
                .builder()
                .subject(userDetails.getUsername())
                .claim("UUID", userDetails.getUUID()) // uuid
                .claim("ROLE", userDetails.getAuthorities().toString().substring(6,
                        userDetails.getAuthorities().toString().length() - 1)) // user ROLE
                .issuedAt(new Date(System.currentTimeMillis())) // Время выпуска
                .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 10)) // Время истечения (10 часов)
                .signWith(getSignInKey(), Jwts.SIG.HS256) // Подписываем токен с использованием ключа и алгоритма HS512
                .compact(); // Компактифицируем токен в строку
    }

    public SecretKey getSignInKey() {

        byte[] keyBytes = Decoders.BASE64.decode(secretKey);  // Декодируем секретный ключ из BASE64

        return Keys.hmacShaKeyFor(keyBytes);  // Создаем ключ с помощью Keys.hmacShaKeyFor
    }

    @Value("${app.jwt.secret}")
    private String secretKeySome;

    public String getEmailFromToken(String token) {

        try {
            SecretKey key = Keys.hmacShaKeyFor(secretKeySome.getBytes(StandardCharsets.UTF_8));
            Claims claims = extractAllClaims(token, key);

            return claims.get("email", String.class);  // Получаем email
        } catch (Exception e) {
            log.error("Unable to get email from token: {}", e.getMessage());

            return null;
        }
    }

    public Date getExpirationDateFromToken(String token) {

        try {
            SecretKey key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
            Claims claims = extractAllClaims(token, key);

            return claims.getExpiration();  // Получаем expDate (дату истечения)
        } catch (Exception e) {
            log.error("Unable to get expiration date from token: {}", e.getMessage());

            return null;
        }
    }

    private Claims extractAllClaims(String token, SecretKey key) {

        Jws<Claims> jwsClaims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token);

        return jwsClaims.getPayload();
    }
}
