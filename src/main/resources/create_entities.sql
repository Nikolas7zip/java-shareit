INSERT INTO users (name, email)
VALUES ('Tester', 'test@mail.com'),
       ('Master', 'master@mail.com');

INSERT INTO items (name, description, available, owner_id)
VALUES ('Лопата', 'Для огорода', TRUE, 1),
       ('Инструмент', 'Можно копать как лопатой', FALSE, 1),
       ('Набор для хозяйства', 'Лопата, грабли, тачка и др.', TRUE, 1);