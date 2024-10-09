package ru.skillbox.auth_service.app.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.skillbox.auth_service.app.entity.User;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUuid(String uuid);

    Optional<User>  findByEmail(String email);

    boolean existsByUuid(String uuid);

    boolean existsByEmail(String email);
}
