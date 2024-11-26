package ru.skillbox.auth_service.kafka.configuration.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.skillbox.auth_service.app.entity.User;
import ru.skillbox.auth_service.app.entity.model.RoleType;
import ru.skillbox.auth_service.app.repository.UserRepository;
import ru.skillbox.auth_service.exception.exceptions.ObjectNotFoundException;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaUserServiceImpl implements KafkaUserService {

    private final UserRepository repository;

    @Override
    public boolean checkUser(String uuid, String email) {

        log.info(("User was checked in DB with" +
                " uuid: %s and email: %s").formatted(uuid, email));
        return repository.existsByUuidAndEmail(uuid, email);
    }

    @Override
    public Optional<User> getUserFomDb(String uuid, String email) {

        log.info(("User was got from DB" +
                " with uuid: %s and email: %s ").formatted(uuid, email));
        return repository.findByUuidAndEmail(uuid, email);
    }

    @Override
    public User updateUser(User userToUpdate) {

        if (this.getUserFomDb(userToUpdate.getUuid(), userToUpdate.getEmail()).isPresent()
                && this.checkUser(userToUpdate.getUuid(), userToUpdate.getEmail())) {

            log.info("User not fond in Db or not valid");
            throw new  ObjectNotFoundException("User not fond in Db or not valid");
        }

        User userFromDb = this.getUserFomDb(userToUpdate.getUuid(), userToUpdate.getEmail()).get();

        if (Boolean.TRUE.equals(userToUpdate.getDeleted())) {

            log.info("User deleted on server");
            this.disableUserAccount(userToUpdate.getUuid(), userToUpdate.getEmail());

            return userToUpdate;
        }

        if (Boolean.TRUE.equals(userToUpdate.getBlocked())) {

            log.info("User banned on server");
            this.banUserAccount(userToUpdate.getUuid(), userToUpdate.getEmail());

            return userToUpdate;
        }

        if (userToUpdate.getEmail() != null) {
            userFromDb.setEmail(userToUpdate.getEmail());
        }

        if (userToUpdate.getPassword1() != null) {
            userFromDb.setPassword1(userToUpdate.getPassword1());
        }

        if (userToUpdate.getPassword2() != null) {
            userFromDb.setPassword2(userToUpdate.getPassword2());
        }

        if (userToUpdate.getFirstName() != null) {
            userFromDb.setFirstName(userToUpdate.getFirstName());
        }

        if (userToUpdate.getLastName() != null) {
            userFromDb.setLastName(userToUpdate.getLastName());
        }

        if (userToUpdate.getBlocked() != null) {
            this.banUserAccount(userToUpdate.getUuid(), userToUpdate.getEmail());
        }

        if (userToUpdate.getMessagePermission() != null) {
            userFromDb.setMessagePermission(userToUpdate.getMessagePermission());
        }

        if (userToUpdate.getRoles() != null) {
            Set<RoleType> roleTypes = new HashSet<>(userToUpdate.getRoles());
            userFromDb.setRoles(roleTypes);
        }

        return userFromDb;
    }

    @Override
    public void saveUserToDb(User user) {

        repository.saveAndFlush(user);
        log.info(("User account with uuid: %s and email:" +
                " %s is saved").formatted(user.getUuid(), user.getEmail()));
    }

    @Override
    public void disableUserAccount(String uuid, String email) {

        if (this.getUserFomDb(uuid, email).isPresent()
                && this.checkUser(uuid, email)) {

            log.info(("User account with uuid: %s" +
                    " and email: %s is not present").formatted(uuid, email));
            throw new ObjectNotFoundException("User account is not present");
        }

        User userAccountToDisable = this.getUserFomDb(uuid, email).get();
        userAccountToDisable.setDeleted(true);
        this.saveUserToDb(userAccountToDisable);

        log.info(("User account with email:" +
                " %s deleted").formatted(userAccountToDisable.getEmail()));
    }

    @Override
    public void banUserAccount(String uuid, String email) {

        if (this.getUserFomDb(uuid, email).isPresent()
                && this.checkUser(uuid, email)) {

            log.info(("User account with uuid: %s" +
                    " and email: %s is not present").formatted(uuid, email));
            throw new ObjectNotFoundException("User account is not present");
        }

        User userAccountToDisable = this.getUserFomDb(uuid, email).get();
        userAccountToDisable.setBlocked(true);
        this.saveUserToDb(userAccountToDisable);
        log.info(("User account with email:" +
                " %s disabled").formatted(userAccountToDisable.getEmail()));

    }
}
