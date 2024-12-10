package ru.skillbox.auth_service.security.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import ru.skillbox.auth_service.app.repository.UserRepository;
import ru.skillbox.auth_service.exception.exceptions.ObjectNotFoundException;

import java.time.LocalDateTime;

/**
 *  Проверяет наличие пользователя в базе данных
 */
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * Проверяет по имени пользователя в базе данных, в данном случае по email (почта)
     * @param email собственно не userName, а почта
     * @return возвращает новый экземпляр new AppUserDetails() содержащий user из Бд
     * @throws UsernameNotFoundException если нет такого пользователя
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        var user = userRepository.findByEmail(email).orElseThrow(() -> new  ObjectNotFoundException(
                "User " +
                        " with email " + email +
                        " was not found in our DB at time: " + LocalDateTime.now()
        ));

        return new AppUserDetails(user);
    }
}
