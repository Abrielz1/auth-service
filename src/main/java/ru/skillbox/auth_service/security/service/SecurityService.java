package ru.skillbox.auth_service.security.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.skillbox.auth_service.app.entity.RefreshToken;
import ru.skillbox.auth_service.app.entity.User;
import ru.skillbox.auth_service.app.entity.model.RoleType;
import ru.skillbox.auth_service.app.repository.UserRepository;
import ru.skillbox.auth_service.exception.exceptions.AlreadyExistsException;
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
import java.util.List;
import java.util.Set;
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

    private final KafkaTemplate<String, KafkaMessageOutputDto> kafkaTemplate;

    @Value("${app.kafka.kafkaMessageTopic0}")
    private String topicToSend;

    public AuthResponseDto authenticationUser(LoginRequest loginRequest) {

        var userFromDB = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> {
                    log.info("User with email like: %s not preset in our DB".formatted(loginRequest.getEmail()));
                    return new ObjectNotFoundException("User with email like: %s not preset in our DB");
                });

        if (Boolean.TRUE.equals(userFromDB.getIsDeleted())) {
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

        var roles = userDetails.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        var refreshToken = refreshTokenService.create(userDetails.getId());

        var response = AuthResponseDto.builder()
                .id(userDetails.getId())
                .token(jwtUtils.generateJwtToken(userDetails))
                .refreshToken(refreshToken.getToken())
                .uuid(userDetails.getUUID())
                .isDeleted(userDetails.getIsDeleted())
                .email(userDetails.getEmail())
                .password(userDetails.getPassword())
                .password2(userDetails.getPassword2())
                .firstName(userDetails.getFirstname())
                .lastName(userDetails.getLastName())
                .roles(roles)
                .build();

        kafkaTemplate.send(topicToSend, toDto(response));

        return response;
    }

    public void register(CreateUserRequest createUserRequest) {

        var user = User.builder()
                .uuid(createUserRequest.getUuid())
                .isDeleted(false)
                .email(createUserRequest.getEmail())
                .firstName(createUserRequest.getFirstANme())
                .lastName(createUserRequest.getLastName())
                .password(passwordEncoder.encode(createUserRequest.getPassword()))
                .password2(passwordEncoder.encode(createUserRequest.getPassword2()))
                .roles(createUserRequest.getRoles())
                .build();

        var toSend = toDto(user);

        toSend.setRoles(this.rolesMapper(user.getRoles()));

        kafkaTemplate.send(topicToSend, toSend);

        userRepository.save(user);
    }

    public RefreshTokenResponse refreshToken(RefreshTokenRequest request) {

        var requestTokenRefresh = request.getRefreshToken();

        return refreshTokenService.getByRefreshToken(requestTokenRefresh)
                .map(refreshTokenService::checkRefreshToken)
                .map(RefreshToken::getUuid)
                .map(uuid -> {
                    User user = userRepository.findByUuid(uuid).orElseThrow(() ->
                            new RefreshTokenException("Exception for userId: " + uuid));

                    String token = jwtUtils.generateTokenFromUUID(user.getUuid());

                    return new RefreshTokenResponse(token, refreshTokenService.create(user.getId()).getToken());
                }).orElseThrow(() -> new RefreshTokenException("RefreshToken is not found!"));
    }

    public void logout() {
        var currentPrincipal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (currentPrincipal instanceof AppUserDetails userDetails) {
            refreshTokenService.deleteByUuid(userDetails.getUUID());
        }
    }

    private List<String> rolesMapper(Set<RoleType> roles) {

        return  roles.stream().map(Enum::toString).toList();
    }
}
