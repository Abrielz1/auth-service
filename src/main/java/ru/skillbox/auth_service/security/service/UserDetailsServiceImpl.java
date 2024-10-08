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
    public UserDetails loadUserByUsername(String uuid) throws UsernameNotFoundException {

        var user = userRepository.findByUuid(uuid).orElseThrow(() -> new  ObjectNotFoundException(
                "User " +
                        " with uuid " + uuid +
                        " was not found in our DB at time: " + LocalDateTime.now()
        ));

        return new AppUserDetails(user);
    }
}
