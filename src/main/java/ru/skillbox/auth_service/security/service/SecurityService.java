package ru.skillbox.auth_service.security.service;

import com.mewebstudio.captcha.Captcha;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.skillbox.auth_service.app.entity.RefreshToken;
import ru.skillbox.auth_service.app.entity.User;
import ru.skillbox.auth_service.app.repository.UserRepository;
import ru.skillbox.auth_service.exception.exceptions.AlreadyExistsException;
import ru.skillbox.auth_service.exception.exceptions.BadRequestException;
import ru.skillbox.auth_service.exception.exceptions.ObjectNotFoundException;
import ru.skillbox.auth_service.exception.exceptions.RefreshTokenException;
import ru.skillbox.auth_service.kafka.dto.KafkaMessageOutputDto;
import ru.skillbox.auth_service.security.jwt.JwtUtils;
import ru.skillbox.auth_service.web.dto.AuthResponseDto;
import ru.skillbox.auth_service.web.dto.CreateUserRequest;
import ru.skillbox.auth_service.web.dto.LoginRequest;
import ru.skillbox.auth_service.web.dto.RefreshTokenRequest;
import ru.skillbox.auth_service.web.dto.RefreshTokenResponse;
import java.time.LocalDateTime;
import static ru.skillbox.auth_service.web.mapper.EntityDtoMapper.toDto;

@Slf4j
@Service
@RequiredArgsConstructor
public class SecurityService {

    private final AuthenticationManager authenticationManager;

    private final JwtUtils jwtUtils;

    private final RefreshTokenService refreshTokenService;

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final UserDetailsServiceImpl userDetailsService;

    private final KafkaTemplate<String, KafkaMessageOutputDto> kafkaTemplate;

    @Value("${app.kafka.kafkaMessageTopic0}")
    private String topicToSend;

    public AuthResponseDto authenticationUser(LoginRequest loginRequest) {

        var userFromDB = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> {
                    log.info("User with email like: %s not preset in our DB".formatted(loginRequest.getEmail()));
                    return new ObjectNotFoundException("User with email like: %s not preset in our DB");
                });

        if (Boolean.TRUE.equals(userFromDB.getDeleted())) {
            log.info("user with deleted account tries to login at " + LocalDateTime.now());
            throw new AlreadyExistsException("user with deleted account tries to login");
        }

        var authentication = authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(
                        loginRequest.getEmail(),
                        loginRequest.getPassword()
                ));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        var userDetails = (AppUserDetails) authentication.getPrincipal();
        var refreshToken = refreshTokenService.create(userDetails.getId());

        return AuthResponseDto.builder()
                .uuid(userDetails.getUUID())
                .email(userDetails.getEmail())
                .token(jwtUtils.generateToken(userDetails))
                .refreshToken(refreshToken)
                .build();
    }

    public void register(CreateUserRequest createUserRequest) {

        if (userRepository.existsByEmail(createUserRequest.getEmail()) &&
                userRepository.existsByUuid(createUserRequest.getUuid())) {

            throw new AlreadyExistsException("User with" +
                    " entered email: %s and uuid: %s already exists!"
                            .formatted(createUserRequest.getEmail(), createUserRequest.getUuid()));
        }

        if (!createUserRequest.getPassword().equals(createUserRequest.getPassword2())) {
            throw new BadRequestException("Passwords not mach!");
        }

        var user = User.builder()
                .uuid(createUserRequest.getUuid())
                .deleted(false)
                .email(createUserRequest.getEmail())
                .firstName(createUserRequest.getFirstName())
                .lastName(createUserRequest.getLastName())
                .password(passwordEncoder.encode(createUserRequest.getPassword()))
                .password2(passwordEncoder.encode(createUserRequest.getPassword2()))
                .build();

        var toSend = toDto(user);

       userRepository.saveAndFlush(user);

        kafkaTemplate.send(topicToSend, toSend);
    }

    public RefreshTokenResponse refreshToken(RefreshTokenRequest request) {

        var requestTokenRefresh = request.getRefreshToken();

        if (refreshTokenService.checkRefreshToken(requestTokenRefresh)) {
            return refreshTokenService.getByRefreshToken(requestTokenRefresh)
                    .map(refreshTokenService::checkRefreshToken)
                    .map(RefreshToken::getEmail)
                    .map(email -> {
                        User user = userRepository.findByEmail(email).orElseThrow(() ->
                                new RefreshTokenException("Exception for userId: " + email));

                        var userDetails = userDetailsService.loadUserByUsername(email);

                        String token = jwtUtils.generateToken(userDetails);

                        return new RefreshTokenResponse(token,
                                refreshTokenService.create(user.getId()).getToken(),
                                true);

                    }).get();
        }

        return new RefreshTokenResponse(null,
                null,
                false);
    }

    public void logout() {
        var currentPrincipal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (currentPrincipal instanceof AppUserDetails userDetails) {
            refreshTokenService.deleteByUuid(userDetails.getUUID());
        }
    }

    public String generateCaptcha() {

        Captcha captcha = new Captcha();

        return captcha.generate().getCode();
    }
}
