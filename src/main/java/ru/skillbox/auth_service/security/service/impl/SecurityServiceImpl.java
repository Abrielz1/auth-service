package ru.skillbox.auth_service.security.service.impl;

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
import org.springframework.transaction.annotation.Transactional;
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
import ru.skillbox.auth_service.security.service.CaptchaService;
import ru.skillbox.auth_service.security.service.MyMailSender;
import ru.skillbox.auth_service.security.service.RefreshTokenService;
import ru.skillbox.auth_service.security.service.SecurityService;
import ru.skillbox.auth_service.web.dto.request.ChangeEmailRequest;
import ru.skillbox.auth_service.web.dto.request.ChangePasswordRequest;
import ru.skillbox.auth_service.web.dto.request.CreateUserRequest;
import ru.skillbox.auth_service.web.dto.request.LoginRequest;
import ru.skillbox.auth_service.web.dto.request.PasswordRecoveryRequest;
import ru.skillbox.auth_service.web.dto.request.RefreshTokenRequest;
import ru.skillbox.auth_service.web.dto.responce.AuthResponseDto;
import ru.skillbox.auth_service.web.dto.responce.CaptchaResponse;
import ru.skillbox.auth_service.web.dto.responce.RefreshTokenResponse;
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

/**
 * Сервис проводит различные манипуляции для возможности работы нв сервере
 */
@Slf4j
@Setter
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class SecurityServiceImpl implements SecurityService {

    private final MyMailSender mailSender;

    private final CaptchaRepository captchaRepository;

    private final AuthenticationManager authenticationManager;

    private final JwtUtils jwtUtils;

    private final RefreshTokenService refreshTokenService;

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    @Value("${app.kafka.kafkaMessageTopic1}")
    private String topicToSend;

    @Value("${app.kafka.DEFAULT_PASSWORD}")
    private String defaultPassword;

    private final KafkaTemplate<String, CommonEvent<RegUserEvent>> kafkaTemplate;

    private final CaptchaService captchaService;

    private static final String URL = "http://79.174.80.223:8085/api/v1/auth/captcha/displayImage/";

    private static final String LINK = "http://79.174.80.223:8085/api/v1/auth/password/recovery/";

    private static final String HEADER = "S7Y5K90E1";

    private static final String TIME = " at time: ";

    /**
     * Обеспечивает проверку почты и пароля передаваемые через: LoginRequest
     * В результате входа отдаёт AuthResponseDto, содержащий токены доступа и обновления
     * @param loginRequest содержит почту и пароль необходимые для входа
     * @return authResponseDto содержит токены доступа и обновления
     * @throws ObjectNotFoundException сообщает, что пользователя нет в Бд,
     * AlreadyExistsException попытка создать дублирующую учётную запись, которая уже есть
     */
    @Override
    @Transactional
    public AuthResponseDto authenticationUser(LoginRequest loginRequest) {

        var userFromDB = this.getUser(loginRequest.getEmail());

        if (Boolean.TRUE.equals(userFromDB.getDeleted()) || Boolean.TRUE.equals(userFromDB.getBlocked())) {
            refreshTokenService.deleteByUuid(userFromDB.getUuid());
            log.info("%nUser with deleted or banned account tries to login at " + LocalDateTime.now());
            throw new UnsupportedStateException("%nUser with deleted account tries to login at time: " + LocalDateTime.now());
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

    /**
     * Создаёт учётную запись пользователя
     * @param createUserRequest содержит все поля, а так же код капчи и её секрет
     * @throws AlreadyExistsException нельзя создать дублирующую учётную запись,
     * UnsupportedStateException капча не совпадает с вводом или пароли не совпадают
     */
    @Override
    @Transactional
    public void register(CreateUserRequest createUserRequest) {

        if (userRepository.existsByEmail(createUserRequest.getEmail()) &&
                userRepository.existsByUuid(createUserRequest.getUuid())) {

        log.info("%nVia Security service user with" +
                " entered email: %s and uuid: %s already exists!"
                        .formatted(createUserRequest.getEmail(), createUserRequest.getUuid()) + TIME + LocalDateTime.now());
            throw new AlreadyExistsException("%nVia Security service user with" +
                    " entered email: %s and uuid: %s already exists!"
                            .formatted(createUserRequest.getEmail(), createUserRequest.getUuid()) + TIME + LocalDateTime.now());
        }

        if (!createUserRequest.getCaptchaCode().equals(createUserRequest.getCaptchaSecret())) {

            log.info("%nVia Security service your captcha code : %s and captcha secret: %s did not match"
                    .formatted(createUserRequest.getCaptchaCode(), createUserRequest.getCaptchaSecret()) + TIME + LocalDateTime.now());
            throw new UnsupportedStateException("%nVia Security service your input : %s and captcha code: %s did not match"
                    .formatted(createUserRequest.getCaptchaCode(), createUserRequest.getCaptchaSecret())  + TIME + LocalDateTime.now());
        }

        if (!createUserRequest.getPassword1().equals(createUserRequest.getPassword2())) {

            log.info("%nVia Security service your input password1: %s and password2: %s are did not match"
                    .formatted(createUserRequest.getPassword1(), createUserRequest.getPassword2()) + TIME + LocalDateTime.now());
            throw new UnsupportedStateException("%nVia Security service your input password1: %s and password2: %s are did not match"
                    .formatted(createUserRequest.getPassword1(), createUserRequest.getPassword2()) + TIME + LocalDateTime.now());
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

        log.info("%nVia Security service user: %s was created".formatted(user) + System.lineSeparator());

        log.info("%nUser to Db: " + user);

        var toSend = toEvent(userRepository.saveAndFlush(user));

        log.info("%nTo Kafka " + toSend + TIME + LocalDateTime.now() + System.lineSeparator());

        System.out.println(kafkaTemplate.send(topicToSend, toSend));

        log.info("%nVia Security service user: %s was send through kafka".formatted(toSend)
                + TIME + LocalDateTime.now() + System.lineSeparator());
    }

    /**
     * Проверяет токен и обновляет его
     * @param request содержит в себе user uuid для обновления его токена
     * @return refreshTokenResponse отдаёт uuid пользователя и токен доступа
     * @throws RefreshTokenException если время жизни токена прошло
     */
    @Override
    @Transactional
    public RefreshTokenResponse refreshToken(RefreshTokenRequest request) {

        var requestTokenRefresh = request.getRefreshToken();

        log.info("%nTOKEN " + requestTokenRefresh);

        return refreshTokenService.getByRefreshToken(requestTokenRefresh)
                .map(refreshTokenService::checkRefreshToken)
                .map(RefreshToken::getUuid)
                .map(uuid -> {
                    User user = userRepository.findByUuid(uuid).orElseThrow(() ->
                            new RefreshTokenException("%nException for userId: " + uuid
                                    + TIME + LocalDateTime.now() + System.lineSeparator()));

                    String token = user.getUuid();
                    String newToken = (refreshTokenService.create(user).getToken());

                    return new RefreshTokenResponse(newToken, token);
                }).orElseThrow(() -> new RefreshTokenException("%nRefreshToken is not found! At time: "
                        + LocalDateTime.now() + System.lineSeparator()));
    }

    @Override
    @Transactional
    public void logout() {

        var currentPrincipal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (currentPrincipal instanceof AppUserDetails userDetails) {

            log.info("%nUser was logging out at time: " + LocalDateTime.now() + System.lineSeparator());
            refreshTokenService.deleteByUuid(userDetails.getUUID());
        }
    }

    /**
     * Проверяет токен пришедший из других микросервисов на подлинность
     * @param token принимает и проверяет путём пере сборки и создания нового токена
     * @return boolean true если ключ совпадает с ключом из токена и false если нет
     * @throws BadRequestException выбрасывает если токена нет в базе Redis
     */
    @Override
    public Boolean validate(String token) {

        if (token == null) {

            log.info("%nPresented token are empty");
            throw new BadRequestException("%nPresented token are empty"
                    + TIME + LocalDateTime.now() + System.lineSeparator());
        }

        if (token.startsWith("Bearer ")) {

            token = token.substring(7);
        }

        log.info("%nVia Security service token: %s was validated".formatted(token)
                + TIME + LocalDateTime.now() + System.lineSeparator());
        return jwtUtils.getHash(token)
                .equals(jwtUtils.getHash(
                        jwtUtils.generateTokenFromValidateUserDetails(
                                jwtUtils.getUserFromToken(token))));
    }

    /**
     * Создаёт картинку PNG капчи и кладёт её в базу Redis по сгенерированному UUID
     * @return отдаёт Секрет капчи и UUID
     */
    @Override
    @Transactional
    public CaptchaResponse generateCaptcha() {

        Captcha captcha = new Captcha();

        String secret = captchaService.generateCaptcha();
        String uuid = UUID.randomUUID().toString();

        captcha.setUuid(uuid);
        captcha.setImage(captchaService.generateCaptchaImage());

        captchaRepository.save(captcha);

        log.info("%nVia Security service captcha was generated at time: "
                + LocalDateTime.now() + System.lineSeparator());
        return new CaptchaResponse(secret, URL + uuid);
    }

    /**
     * Высылает картинку капчи (PNG) в виде массива байтов по ключу UUID
     * @param uuid ключ для получения капчи из Redis
     * @return картинку PNG капчи в виде массива байтов
     */
    @Override
    public byte[] sendCaptcha(String uuid) {

        log.info("%nVia Security service captcha was sent at time: "
                + LocalDateTime.now() + System.lineSeparator());
        return this.captchaChecker(uuid).getImage();
    }

    /**
     * Принимает email пользователя и высылает на указанную почту ссылку
     * @param request email для высылки ссылки
     * @throws ObjectNotFoundException если почта в сообщении отсутствует
     */
    @Override
    public void passwordRecovery(PasswordRecoveryRequest request) {

        if (userRepository.findByEmail(request.getEmail()).isEmpty()) {
            throw new ObjectNotFoundException("");
        }

        log.info("%nVia security service passwordRecovery " + request.getEmail()
                + TIME + LocalDateTime.now());

        String link = this.generateRecoveryLink(this.checkUser(request.getEmail()));

        StringBuilder recoveryLink = new StringBuilder();
        recoveryLink
                .append(LINK)
                .append("_")
                .append(HEADER)
                .append("_")
                .append(request
                .getEmail())
                .append("_")
                .append(link);

        log.info("%nVia security service passwordRecovery mail with link was sent at time: "
                + LocalDateTime.now());
        mailSender.sendMailMessage(request, recoveryLink);
    }

    /**
     * Проверяет ссылку на корректность данных и в случае успеха устанавливает пароль по умолчанию
     * @param linkId собственно ссылка состоящая из заголовка, почты пользователя и секретной части
     * @return возвращает строку сообщение об успехе
     * @throws BadRequestException в случае пустой ссылки, AlreadyExistsException в случае не корректной ссылки
     */
    @Override
    @Transactional
    public String checkSecurityLink(String linkId) {

        if (linkId == null || linkId.isBlank()) {
            log.info("%nEmpty link was present");
            throw new BadRequestException("No link to check!");
        }

        if (!linkId.contains(HEADER) && !linkId.isEmpty()) {
            throw new AlreadyExistsException("%nPresented link isn't correct at time: "
                    + LocalDateTime.now() + System.lineSeparator());
        }

        String userEmail = this.getterUserEmailFromLink(linkId);
        String password = passwordEncoder.encode(defaultPassword);
        String linkToCheck = this.urlGetter(linkId);

        User user = checkUser(userEmail);

        if (this.urlChecker(linkToCheck, user)) {
            user.setPassword1(password);
            user.setPassword2(password);
            userRepository.saveAndFlush(user);

        log.info("%nUser password was changed at time: " + LocalDateTime.now() + System.lineSeparator());
        log.info(("%nUser with email: %s via Security service change his/her " +
                "password: %s").formatted(userEmail, defaultPassword) + TIME + LocalDateTime.now()
                + System.lineSeparator());

            var toSend = toEvent(user);

            kafkaTemplate.send(topicToSend, toSend);

        mailSender.sendMailMessage(userEmail, defaultPassword);
            return ("%nUser with email: %s via Security service change" +
                    " his/her password: %s").formatted(userEmail, defaultPassword) + System.lineSeparator();
        } else {
            throw new AlreadyExistsException("%nLink isn't correct at time: " + LocalDateTime.now()
                    + System.lineSeparator());
        }
    }

    @Override
    public void changePassword(String email, ChangePasswordRequest changePasswordRequest) {

        User userFromDb = this.checkUser(email);

        if (userFromDb.getPassword1().equals(changePasswordRequest.getOldPassword())) {

            userFromDb.setPassword1(changePasswordRequest.getNewPassword1());
            userFromDb.setPassword2(changePasswordRequest.getNewPassword1());

            userRepository.saveAndFlush(userFromDb);
        }
    }

    @Override
    public boolean changeEmail(String username, ChangeEmailRequest emailRequest) {

        System.out.println("UserName via security service change email" + username
                + " Email request: " + emailRequest.getEmail());
        return false;
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

        log.info("%nNo captcha for uuid: %s in Db are present!".formatted(uuid) + TIME + LocalDateTime.now());
        return captchaRepository.findByUuid(uuid)
                .orElseThrow(() -> {
                    log.info("%nNo captcha for uuid: %s in Db are not present!".formatted(uuid)
                            + System.lineSeparator());
                    return new ObjectNotFoundException("%nNo captcha for uuid: %s in Db are not present!"
                            .formatted(uuid) + TIME + LocalDateTime.now() + System.lineSeparator());
                });
    }

    private User checkUser(String email) {

        log.info("%nVia Security service User with email: %s are present in db!".formatted(email)
                + TIME + LocalDateTime.now());
        return userRepository.findByEmail(email).orElseThrow(() -> {
            log.info("%nVia Security service User with email: %s are not present in db!".formatted(email)
                    + TIME + LocalDateTime.now() + System.lineSeparator());
            return new ObjectNotFoundException(("%nUser with email:" +
                    " %s are not present in db!").formatted(email) + TIME + LocalDateTime.now()
                    + System.lineSeparator());
        });
    }

    private String convertByteArrayToHexString(byte[] arrayBytes) {

        var stringBuffer = new StringBuilder();

        for (byte arrayByte : arrayBytes) {
            stringBuffer.append(Integer.toString((arrayByte & 0xff) + 0x100, 16)
                    .substring(1));
        }

        return stringBuffer.toString();
    }

    private String generateRecoveryLink(User user) {

        StringBuilder link = new StringBuilder();
        link.append(user.getUuid())
                .append(user.getEmail())
                .append(user.getPassword1())
                .append(user.getPassword2());

        try {

            return convertByteArrayToHexString(MessageDigest.getInstance("SHA-1")
                    .digest(link.toString().getBytes(StandardCharsets.UTF_8)));

        } catch (NoSuchAlgorithmException e) {
            throw new BadRequestException(
                    "%nCould not generate hash from String at time: " + LocalDateTime.now() + System.lineSeparator());
        }
    }

    private User getUser(String email) {

        log.info("%nUser with email: %s was fon in Db at time: ".formatted(email) + LocalDateTime.now());
        return userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.info("%nUser with email like: %s not preset in our DB at time: ".formatted(email)
                            + LocalDateTime.now());
                    return new ObjectNotFoundException("%nUser with email like: %s not preset in our DB");
                });
    }
}
