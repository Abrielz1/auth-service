insert into users
(blocked, deleted, email, first_name, last_name, message_permission, password1, password2, uuid)
values
    (false, false, 'admin@example.com', 'Admin', 'Adminov', 'ALL', '$2a$10$32gqiVbTTgQUFv4M2Gmfq.D2nw3pHBQZpM3A2T4Ik7X4UzGlpJv.G', '$2a$10$32gqiVbTTgQUFv4M2Gmfq.D2nw3pHBQZpM3A2T4Ik7X4UzGlpJv.G', '123e4567-e89b-12d3-a456-426614174000');

insert into user_roles
(user_id, roles)
values
    (1, 'ADMIN');

insert into users
(blocked, deleted, email, first_name, last_name, message_permission, password1, password2, uuid)
values
    (false, false, 'user@example.com', 'User', 'Userov', 'FRIENDS_ONLY', '$2a$10$OtxJrtd33zZZgfaPJlNt3O.Bd89NDu.Wh9hIdNx/4wIxv4/lRccx2', '$2a$10$OtxJrtd33zZZgfaPJlNt3O.Bd89NDu.Wh9hIdNx/4wIxv4/lRccx2', '987e6543-e21c-34b2-c567-527819174abc');

insert into user_roles
(user_id, roles)
values
    (2, 'USER');