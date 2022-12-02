MERGE INTO users (id, name, email)
VALUES (1, 'Tester', 'test@mail.com'),
       (2, 'Master', 'master@mail.com');

MERGE INTO items (id, name, description, available, owner_id)
VALUES (1, 'Лопата', 'Для огорода', TRUE, 1),
       (2, 'Инструмент', 'Можно копать как лопатой', FALSE, 1),
       (3, 'Набор для хозяйства', 'Лопата, грабли, тачка и др.', TRUE, 1);

MERGE INTO bookings (id, start_date, end_date, item_id, booker_id, status)
VALUES (1, '2022-11-20 16:00:00', '2022-11-22 18:00:00', 1, 2, 'APPROVED'),
       (2, '2022-12-01 16:00:00', '2022-12-03 19:00:00', 1, 2, 'WAITING'),
       (3, '2022-12-01 15:00:00', '2022-12-03 14:00:00', 3, 2, 'REJECTED'),
       (4, '2022-12-04 15:00:00', '2022-12-06 15:00:00', 3, 2, 'APPROVED');