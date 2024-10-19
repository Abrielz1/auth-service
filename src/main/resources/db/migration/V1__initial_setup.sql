--V1__initial_setup.sql

CREATE TABLE IF NOT EXISTS users (
  id                BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
  uuid              VARCHAR(32) NOT NULL UNIQUE,
  is_deleted        BOOLEAN NOT NULL,
  first_name        VARCHAR(32) NOT NULL,
  last_name         VARCHAR(32) NOT NULL,
  email             VARCHAR(32) NOT NULL UNIQUE,
  password          VARCHAR(320) NOT NULL UNIQUE,
  password2         VARCHAR(320) NOT NULL UNIQUE
);

CREATE TABLE user_roles (
    user_id         BIGINT NOT NULL,
    roles           VARCHAR(255) NOT NULL CHECK (roles IN ('ROLE_USER','ROLE_ADMIN')),

    CONSTRAINT fk_user_roles_on_user
        FOREIGN KEY (user_id) REFERENCES users (id)
);
--
--  ALTER TABLE user_roles
--      ADD CONSTRAINT fk_user_roles_on_user FOREIGN KEY (user_id) REFERENCES users (id);


-- generated ny hibernate orm UNIQUE added by me
-- create table user_roles (
--                             user_id bigint not null,
--                             roles varchar(255) not null check (roles in ('ROLE_USER','ROLE_ADMIN')),
--                             primary key (user_id, roles)
-- );
--
-- create table users (
--                        id bigint generated by default as identity,
--                        email varchar(255) not null UNIQUE,
--                        first_name varchar(255) not null,
--                        last_name varchar(255) not null,
--                        password varchar(255) not null UNIQUE,
--                        password2 varchar(255) not null UNIQUE,
--                        uuid varchar(255) not null UNIQUE,
--                        primary key (id)
-- );
--
-- alter table if exists user_roles
--     add constraint fk_user_roles_on_user
--         foreign key (user_id)
--             references users(id);
