INSERT INTO users (name, email)
VALUES ('Tester', 'test@mail.com'),
       ('Master', 'master@mail.com');

INSERT INTO items (name, description, available, owner_id)
VALUES ('Лопата', 'Для огорода', TRUE, 1),
       ('Инструмент', 'Можно копать как лопатой', FALSE, 1),
       ('Набор для хозяйства', 'Лопата, грабли, тачка и др.', TRUE, 1);

INSERT INTO requests (description, requester_id, created)
    VALUES ('Нужен молоток', 2, '2022-12-01 19:30:00'),
           ('Лестница найдись', 2, '2022-12-01 20:00:00'),
           ('Ищу шуруповерт', 1, '2022-12-01 21:00:00');