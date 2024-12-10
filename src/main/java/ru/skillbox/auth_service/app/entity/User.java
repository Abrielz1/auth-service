package ru.skillbox.auth_service.app.entity;

import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.NamedAttributeNode;
import jakarta.persistence.NamedEntityGraph;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.proxy.HibernateProxy;
import ru.skillbox.auth_service.app.entity.model.RoleType;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Table(name = "users")
@Entity
@Getter
@Setter
@Builder
@ToString
@NamedEntityGraph(name = "users_entity-graph", attributeNodes = @NamedAttributeNode("roles"))
@NoArgsConstructor
@AllArgsConstructor
public class User implements Serializable {

    /**
     * id пользователя
     */
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * uuid пользователя
     */
    @Column(nullable = false, name = "uuid", unique = true)
    private String uuid;

    /**
     * флаг удалён/deleted ли пользователь
     */
    @Column(nullable = false, name = "deleted")
    private Boolean deleted;

    /**
     * флаг забанен/заблокирован ли пользователь
     */
    @Column(name = "blocked")
    private Boolean blocked;

    /**
     * Имя пользователя
     */
    @Column(nullable = false, name = "first_name")
    private String firstName;

    /**
     * Фамилия пользователя
     */
    @Column(nullable = false, name = "last_name")
    private String lastName;

    /**
     * пароль 1
     */
    @Column(nullable = false, name = "password1")
    private String password1;

    /**
     * пароль 2
     */
    @Column(nullable = false, name = "password2")
    private String password2;

    /**
     * электронная пользователя
     */
    @Column(nullable = false, name = "email", unique = true)
    private String email;

    /**
     * кому может писать пользователь
     */
    @Column(name = "message_permission")
    private String messagePermission;

    @ElementCollection(targetClass = RoleType.class, fetch = FetchType.EAGER)
    @JoinTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "roles",nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Set<RoleType> roles = new HashSet<>();

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        User user = (User) o;
        return getId() != null && Objects.equals(getId(), user.getId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
    }
}
