package ru.skillbox.auth_service.app.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ru.skillbox.auth_service.app.entity.Captcha;

import java.util.Optional;

@Repository
public interface CaptchaRepository extends CrudRepository<Captcha, Long> {

    Optional<Captcha> findByUuid(String uuid);
}
