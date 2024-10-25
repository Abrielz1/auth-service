package ru.skillbox.auth_service.app.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.skillbox.auth_service.app.entity.User;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Boolean existsByUuid(String uuid);

    Boolean existsByEmail(String email);

    Optional<User> findByUuidAndEmail(String uuid, String email);

    Boolean existsByUuidAndEmail(String uuid, String email);
}
