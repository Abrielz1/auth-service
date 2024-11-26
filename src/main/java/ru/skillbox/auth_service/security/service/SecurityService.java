package ru.skillbox.auth_service.security.service;

import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.skillbox.auth_service.app.entity.Captcha;
import ru.skillbox.auth_service.app.entity.RefreshToken;
import ru.skillbox.auth_service.app.entity.User;
import ru.skillbox.auth_service.app.entity.model.RoleType;
import ru.skillbox.auth_service.app.repository.CaptchaRepository;
import ru.skillbox.auth_service.app.repository.UserRepository;
import ru.skillbox.auth_service.exception.exceptions.AlreadyExistsException;
import ru.skillbox.auth_service.exception.exceptions.BadRequestException;
import ru.skillbox.auth_service.exception.exceptions.ObjectNotFoundException;
import ru.skillbox.auth_service.exception.exceptions.RefreshTokenException;
import ru.skillbox.auth_service.exception.exceptions.UnsupportedStateException;
import ru.skillbox.auth_service.security.jwt.JwtUtils;
import ru.skillbox.auth_service.web.dto.AuthResponseDto;
import ru.skillbox.auth_service.web.dto.CaptchaRs;
import ru.skillbox.auth_service.web.dto.CreateUserRequest;
import ru.skillbox.auth_service.web.dto.LoginRequest;
import ru.skillbox.auth_service.web.dto.NewPasswordDto;
import ru.skillbox.auth_service.web.dto.PasswordRecoveryRequest;
import ru.skillbox.auth_service.web.dto.RefreshTokenRequest;
import ru.skillbox.auth_service.web.dto.RefreshTokenResponse;
import ru.skillbox.common.events.CommonEvent;
import ru.skillbox.common.events.RegUserEvent;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static ru.skillbox.auth_service.web.mapper.EntityDtoMapper.toEvent;

@Slf4j
@Setter
@Service
@RequiredArgsConstructor
public class SecurityService {

    private final MyMailSender mailSender;

    private final CaptchaRepository captchaRepository;

    private final AuthenticationManager authenticationManager;

    private final JwtUtils jwtUtils;

    private final RefreshTokenService refreshTokenService;

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    @Value("${app.kafka.kafkaMessageTopic1}")
    private String topicToSend;

    private final KafkaTemplate<String, CommonEvent<RegUserEvent>> kafkaTemplate;

    private final CaptchaService captchaService;

    private static final String URL = "http://79.174.80.223:8085/api/v1/auth/captcha/displayImage/";

    private static final String LINK = "http://79.174.80.223:8085/api/v1/auth/password/recovery/";

    private static final String HEADER = "S7Y5K90E1";

    public AuthResponseDto authenticationUser(LoginRequest loginRequest) {

        var userFromDB = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> {
                    log.info("User with email like: %s not preset in our DB".formatted(loginRequest.getEmail()));
                    return new ObjectNotFoundException("User with email like: %s not preset in our DB");
                });

        if (Boolean.TRUE.equals(userFromDB.getDeleted()) || Boolean.TRUE.equals(userFromDB.getBlocked())) {
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
        User user = this.checkUser(userDetails.getEmail());

        var refreshToken = refreshTokenService.create(user);
        var token = refreshToken.getToken();

        new AuthResponseDto();
        return AuthResponseDto.builder()
                .refreshToken(refreshToken.getUuid())
                .accessToken(token)
                .build();
    }

    public void register(CreateUserRequest createUserRequest) {

        if (userRepository.existsByEmail(createUserRequest.getEmail()) &&
                userRepository.existsByUuid(createUserRequest.getUuid())) {

        log.info("Via Security service user with" +
                " entered email: %s and uuid: %s already exists!"
                        .formatted(createUserRequest.getEmail(), createUserRequest.getUuid()));
            throw new AlreadyExistsException("Via Security service user with" +
                    " entered email: %s and uuid: %s already exists!"
                            .formatted(createUserRequest.getEmail(), createUserRequest.getUuid()));
        }

        if (!createUserRequest.getCaptchaCode().equals(createUserRequest.getCaptchaSecret())) {

            log.info("Via Security service your input : %s and captcha code: %s did not match"
                    .formatted(createUserRequest.getCaptchaCode(), createUserRequest.getCaptchaSecret()));
            throw new UnsupportedStateException("Via Security service your input : %s and captcha code: %s did not match"
                    .formatted(createUserRequest.getCaptchaCode(), createUserRequest.getCaptchaSecret()));
        }

        if (!createUserRequest.getPassword1().equals(createUserRequest.getPassword2())) {

            log.info("Via Security service your input password1: %s and password2: %s are did not match"
                    .formatted(createUserRequest.getPassword1(), createUserRequest.getPassword2()));
            throw new UnsupportedStateException("Via Security service your input password1: %s and password2: %s are did not match"
                    .formatted(createUserRequest.getPassword1(), createUserRequest.getPassword2()));
        }

        List<RoleType> roleTypes = new ArrayList<>();
        roleTypes.add(RoleType.USER);

        Set<RoleType> roles = new HashSet<>(roleTypes);

        String password = passwordEncoder.encode(createUserRequest.getPassword1());

        var user = User.builder()
                .uuid(createUserRequest.getUuid() == null ?
                        UUID.randomUUID().toString() : createUserRequest.getUuid())
                .deleted(false)
                .blocked(false)
                .email(createUserRequest.getEmail())
                .firstName(createUserRequest.getFirstName())
                .lastName(createUserRequest.getLastName())
                .password1(password)
                .password2(password)
                .roles(roles)
                .build();

        log.info("Via Security service user: %s was created".formatted(user));

        var toSend = toEvent(userRepository.saveAndFlush(user));

        log.info("To Kafka " + toSend);

        System.out.println(kafkaTemplate.send(topicToSend, toSend));

        log.info("Via Security service user: %s was send through kafka".formatted(toSend));
    }

    public RefreshTokenResponse refreshToken(RefreshTokenRequest request) {

        var requestTokenRefresh = request.getRefreshToken();

        log.info("TOKEN " + requestTokenRefresh);

        return refreshTokenService.getByRefreshToken(requestTokenRefresh)
                .map(refreshTokenService::checkRefreshToken)
                .map(RefreshToken::getUuid)
                .map(uuid -> {
                    User user = userRepository.findByUuid(uuid).orElseThrow(() ->
                            new RefreshTokenException("Exception for userId: " + uuid));

                    String token = user.getUuid();
                    String newToken = (refreshTokenService.create(user).getToken());

                    return new RefreshTokenResponse(newToken, token);
                }).orElseThrow(() -> new RefreshTokenException("RefreshToken is not found!"));
    }

    public void logout() {

        var currentPrincipal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (currentPrincipal instanceof AppUserDetails userDetails) {
            refreshTokenService.deleteByUuid(userDetails.getUUID());
        }
    }

    public Boolean validate(String token) {

        if (token == null) {

            log.info("token is empty");
            throw new BadRequestException("token is empty");
        }

        if (token.startsWith("Bearer ")) {

            token = token.substring(7);
        }

        log.info("Via Security service token: %s was validated".formatted(token));
        return jwtUtils.getHash(token)
                .equals(jwtUtils.getHash(
                        jwtUtils.generateTokenFromValidateUserDetails(
                                jwtUtils.getUserFromToken(token))));
    }

    public CaptchaRs generateCaptcha() {

        Captcha captcha = new Captcha();

        String secret = captchaService.generateCaptcha();
        String uuid = UUID.randomUUID().toString();

        captcha.setUuid(uuid);
        captcha.setImage(captchaService.generateCaptchaImage());

        captchaRepository.save(captcha);

        log.info("Via Security service captcha was generated");
        return new CaptchaRs(secret, URL + uuid);
    }

    public byte[] sendCaptcha(String uuid) {

        log.info("Via Security service captcha was sent");
        return this.captchaChecker(uuid).getImage();
    }

    public void passwordRecovery(PasswordRecoveryRequest request) {

        if (userRepository.findByEmail(request.getEmail()).isEmpty()) {
            throw new ObjectNotFoundException("");
        }

        log.info("Via security service passwordRecovery " + request.getEmail());

        String link = this.generateRecoveryLink(userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ObjectNotFoundException("User not present in Db")));

        String recoveryLink = LINK + "_" + HEADER + "_" + request.getEmail() + "_" + link;

        mailSender.sendMailMessage(request, recoveryLink);
    }

    private String generateRecoveryLink(User user) {

        var link = user.getUuid() + user.getEmail() + user.getPassword1() + user.getPassword2();

        try {

            return convertByteArrayToHexString(MessageDigest.getInstance("SHA-1")
                    .digest(link.getBytes(StandardCharsets.UTF_8)));

        } catch (NoSuchAlgorithmException e) {
            throw new BadRequestException(
                    "Could not generate hash from String");
        }
    }

    private String convertByteArrayToHexString(byte[] arrayBytes) {

        var stringBuffer = new StringBuilder();

        for (byte arrayByte : arrayBytes) {
            stringBuffer.append(Integer.toString((arrayByte & 0xff) + 0x100, 16)
                    .substring(1));
        }

        return stringBuffer.toString();
    }

    public String checkSecurityLink(String linkId, NewPasswordDto request) {

        if (!linkId.contains(HEADER)) {
            throw new AlreadyExistsException("Link isn't correct");
        }

        String userEmail = this.getterUserEmailFromLink(linkId);
        String password = passwordEncoder.encode(request.getPassword());
        String linkToCheck = this.urlGetter(linkId);

        User user = userRepository.findByEmail(userEmail).orElseThrow(() -> new ObjectNotFoundException(""));

        if (this.urlChecker(linkToCheck, user)) {
            user.setPassword1(password);
            user.setPassword2(password);
            userRepository.saveAndFlush(user);

        log.info("User with email: %s via Security service change his/her password".formatted(userEmail));
            return "User with email: %s via Security service change his/her password".formatted(userEmail);
        } else {
            throw new AlreadyExistsException("Link isn't correct");
        }
    }

    private String getterUserEmailFromLink(String linkId) {

        return this.linkShredder(linkId)[2];
    }

    private String urlGetter(String linkId) {

        return this.linkShredder(linkId)[3];
    }

    private boolean urlChecker(String linkId, User user) {

        return linkId.equals(this.generateRecoveryLink(user));
    }

    private String[] linkShredder(String linkId) {

        return linkId.split("_");
    }

    private Captcha captchaChecker(String uuid) {

        log.info("No captcha for uuid: %s in Db are present!".formatted(uuid));
        return captchaRepository.findByUuid(uuid)
                .orElseThrow(() -> {
                    log.info("No captcha for uuid: %s in Db are not present!".formatted(uuid));
                    return new ObjectNotFoundException("No captcha for uuid: %s in Db are not present!"
                            .formatted(uuid));
                });
    }

    private User checkUser(String email) {

        log.info("Via Security service User with email: %s are present in db!".formatted(email));
        return userRepository.findByEmail(email).orElseThrow(() -> {
            log.info("Via Security service User with email: %s are not present in db!".formatted(email));
            return new ObjectNotFoundException(("User with email:" +
                    " %s are not present in db!").formatted(email));
        });
    }
}
