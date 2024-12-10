package ru.skillbox.auth_service.kafka.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.skillbox.auth_service.app.entity.User;
import ru.skillbox.auth_service.app.entity.model.RoleType;
import ru.skillbox.auth_service.app.repository.UserRepository;
import ru.skillbox.auth_service.exception.exceptions.ObjectNotFoundException;
import ru.skillbox.auth_service.kafka.service.KafkaUserService;
import ru.skillbox.auth_service.security.service.RefreshTokenService;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class KafkaUserServiceImpl implements KafkaUserService {

    private final UserRepository repository;

    private final RefreshTokenService refreshTokenService;

    @Override
    public boolean checkUser(String uuid, String email) {

        log.info(("%nUser was checked in DB with" +
                " uuid: %s and email: %s at time: "
                + LocalDateTime.now() + System.lineSeparator()).formatted(uuid, email));
        return !repository.existsByUuidAndEmail(uuid, email);
    }

    @Override
    public Optional<User> getUserFomDb(String uuid, String email) {

        log.info(("%nUser was got from DB" +
                " with uuid: %s and email: %s at time: " + LocalDateTime.now()).formatted(uuid, email));
        return repository.findByUuidAndEmail(uuid, email);
    }

    @Override
    @Transactional
    public void updateUser(User userToUpdate) {

        log.info("%nVia UserService User updater + %s at time: ".formatted(userToUpdate)
                + LocalDateTime.now() + System.lineSeparator());

        if (this.getUserFomDb(userToUpdate.getUuid(), userToUpdate.getEmail()).isEmpty()
                && this.checkUser(userToUpdate.getUuid(), userToUpdate.getEmail())) {

            log.info(("%nUser was not fond in Db or not valid" +
                    " via KafkaUserServiceImpl -> updateUser: %s at time: "
                    + LocalDateTime.now()).formatted(userToUpdate) + System.lineSeparator());
            throw new  ObjectNotFoundException("%nUser was not fond in Db or not valid");
        }

        User userFromDb = this.getUserFomDb(userToUpdate.getUuid(), userToUpdate.getEmail()).get();

        if (Boolean.TRUE.equals(userToUpdate.getDeleted())) {

            log.info("%nUser was deleted on server at time: " + LocalDateTime.now() + System.lineSeparator());
            refreshTokenService.deleteByUuid(userFromDb.getUuid());
            this.disableUserAccount(userToUpdate.getUuid(), userToUpdate.getEmail());

            return;
        }

        if (Boolean.TRUE.equals(userToUpdate.getBlocked())) {

            log.info("%nUser was banned on server at time: " + LocalDateTime.now() + System.lineSeparator());
            refreshTokenService.deleteByUuid(userFromDb.getUuid());
            this.banUserAccount(userToUpdate.getUuid(), userToUpdate.getEmail());

            return;
        }

        if (userToUpdate.getEmail() != null) {
            userFromDb.setEmail(userToUpdate.getEmail());
        }

        if (userToUpdate.getPassword1() != null) {
            userFromDb.setPassword1(userToUpdate.getPassword1());
        }

        if (userToUpdate.getPassword2() != null) {
            userFromDb.setPassword2(userToUpdate.getPassword1());
        }

        if (userToUpdate.getFirstName() != null) {
            userFromDb.setFirstName(userToUpdate.getFirstName());
        }

        if (userToUpdate.getLastName() != null) {
            userFromDb.setLastName(userToUpdate.getLastName());
        }

        if (userToUpdate.getMessagePermission() != null) {
            userFromDb.setMessagePermission(userToUpdate.getMessagePermission());
        }

        if (!userToUpdate.getRoles().isEmpty()) {
            Set<RoleType> roleTypes = new HashSet<>(userToUpdate.getRoles());
            userFromDb.setRoles(roleTypes);
        }

        repository.saveAndFlush(userFromDb);
    }

    @Override
    public void disableUserAccount(String uuid, String email) {

        User userAccountToDisable = this.userCheckerInDbByUuidAndEmail(uuid, email);
        userAccountToDisable.setDeleted(true);
        repository.saveAndFlush(userAccountToDisable);

        log.info(("%nUser account with email:" +
                " %s deleted at time: " + LocalDateTime.now()).formatted(userAccountToDisable.getEmail()));
    }

    @Override
    public void banUserAccount(String uuid, String email) {

        User userAccountToBan = this.userCheckerInDbByUuidAndEmail(uuid, email);

        userAccountToBan.setBlocked(true);
        repository.saveAndFlush(userAccountToBan);

        log.info("%nUser account with was saved");
        log.info(("%nUser account with email:" +
                " %s disabled at time: "
                + LocalDateTime.now() + System.lineSeparator()).formatted(userAccountToBan.getEmail()));
    }

    private User userCheckerInDbByUuidAndEmail(String uuid, String email) {

        if (this.getUserFomDb(uuid, email).isEmpty()
                && this.checkUser(uuid, email)) {

            log.info(("%nUser account with uuid: %s" +
                    " and email: %s is not present at time: "
                    + LocalDateTime.now() + System.lineSeparator()).formatted(uuid, email));
            throw new ObjectNotFoundException("%nUser account is not present");
        }

        return this.getUserFomDb(uuid, email).get();
    }
}
