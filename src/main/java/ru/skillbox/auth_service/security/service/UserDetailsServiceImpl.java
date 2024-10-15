package ru.skillbox.auth_service.security.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import ru.skillbox.auth_service.app.repository.UserRepository;
import ru.skillbox.auth_service.exception.exceptions.ObjectNotFoundException;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

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
