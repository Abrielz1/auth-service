package ru.skillbox.auth_service.app.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.skillbox.auth_service.app.entity.User;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    @EntityGraph(type = EntityGraph.EntityGraphType.FETCH, value = "users_entity-graph")
    Optional<User> findByUuid(String uuid);

    @EntityGraph(type = EntityGraph.EntityGraphType.FETCH, value = "users_entity-graph")
    Optional<User> findByEmail(String email);

    @EntityGraph(type = EntityGraph.EntityGraphType.FETCH, value = "users_entity-graph")
    Boolean existsByUuid(String uuid);

    @EntityGraph(type = EntityGraph.EntityGraphType.FETCH, value = "users_entity-graph")
    Boolean existsByEmail(String email);

    @EntityGraph(type = EntityGraph.EntityGraphType.FETCH, value = "users_entity-graph")
    Optional<User> findByUuidAndEmail(String uuid, String email);

    @EntityGraph(type = EntityGraph.EntityGraphType.FETCH, value = "users_entity-graph")
    Boolean existsByUuidAndEmail(String uuid, String email);
}
