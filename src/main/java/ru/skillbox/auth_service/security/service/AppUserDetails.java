package ru.skillbox.auth_service.security.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import ru.skillbox.auth_service.app.entity.User;
import java.util.Collection;

@RequiredArgsConstructor
public class AppUserDetails implements UserDetails {

    private final User user;

    public Long getId() {
        return user.getId();
    }

    public String getEmail() {
        return user.getEmail();
    }

    public String getUUID() {
        return  user.getUuid();
    }

    public String getFirstname() {
        return user.getFirstName();
    }

    public String getLastName() {
        return user.getLastName();
    }

    public Boolean getIsDeleted() {
        return user.getIsDeleted();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return user.getRoles().stream().map(
                roleType -> new SimpleGrantedAuthority(roleType.name()))
                .toList();
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    public String getPassword2() {
        return user.getPassword2();
    }

    @Override
    public String getUsername() {
        return user.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
