-- =============================================================
-- EduTrack — Demo Data (PostgreSQL)
-- Run AFTER the tables have been created.
-- All demo users share the password: password
-- BCrypt hash below corresponds to plain-text "password"
-- =============================================================

-- -------------------------------------------------------------
-- TRUNCATE all tables (reverse dependency order)
-- -------------------------------------------------------------
TRUNCATE TABLE
    school_schedule_entries,
    absences,
    grades,
    teacher_qualifications,
    schedules,
    parent_student,
    students,
    teachers,
    classes,
    schools,
    users,
    roles
RESTART IDENTITY CASCADE;

-- -------------------------------------------------------------
-- 1. ROLES
-- -------------------------------------------------------------
INSERT INTO Roles (Id, Name) VALUES
    (1, 'ADMIN'),
    (2, 'HEADMASTER'),
    (3, 'TEACHER'),
    (4, 'STUDENT'),
    (5, 'PARENT');


-- -------------------------------------------------------------
-- 2. USERS
-- BCrypt(password) = $2a$10$xh6NdTDAHBbT5c3CoQ.DQevUFcnMgxsYFxBFluURaaxPDCZrxoZlu
-- -------------------------------------------------------------
-- All demo users share the password: password
INSERT INTO Users (Id, Email, PasswordHash, FirstName, LastName, RoleId) VALUES
    -- Admin
    (1,  'agenta007@edutrack.bg',          '$2a$10$xh6NdTDAHBbT5c3CoQ.DQevUFcnMgxsYFxBFluURaaxPDCZrxoZlu', 'Стати',     'Косев',    1),
    (2,  'kiwifruit47@edutrack.bg',          '$2a$10$xh6NdTDAHBbT5c3CoQ.DQevUFcnMgxsYFxBFluURaaxPDCZrxoZlu', 'Виктория',     'Димитрова',    1),

    -- Headmasters
    (3,  'm.georgieva@school1.bg',     '$2a$10$xh6NdTDAHBbT5c3CoQ.DQevUFcnMgxsYFxBFluURaaxPDCZrxoZlu', 'Мария',    'Георгиева', 2),
    (4,  'p.nikolov@school2.bg',       '$2a$10$xh6NdTDAHBbT5c3CoQ.DQevUFcnMgxsYFxBFluURaaxPDCZrxoZlu', 'Петър',    'Николов',   2),

    -- Teachers (School 1)
    (5,  's.dimitrov@school1.bg',      '$2a$10$xh6NdTDAHBbT5c3CoQ.DQevUFcnMgxsYFxBFluURaaxPDCZrxoZlu', 'Стефан',   'Димитров',  3),
    (6,  'e.ivanova@school1.bg',       '$2a$10$xh6NdTDAHBbT5c3CoQ.DQevUFcnMgxsYFxBFluURaaxPDCZrxoZlu', 'Елена',    'Иванова',   3),

    -- Teachers (School 2)
    (7,  't.stoyanova@school2.bg',     '$2a$10$xh6NdTDAHBbT5c3CoQ.DQevUFcnMgxsYFxBFluURaaxPDCZrxoZlu', 'Теодора',  'Стоянова',  3),
    (8,  'h.hristov@school2.bg',       '$2a$10$xh6NdTDAHBbT5c3CoQ.DQevUFcnMgxsYFxBFluURaaxPDCZrxoZlu', 'Христо',   'Христов',   3),

    -- Students (School 1 — 10А)
    (9,  'a.angelov@students.bg',      '$2a$10$xh6NdTDAHBbT5c3CoQ.DQevUFcnMgxsYFxBFluURaaxPDCZrxoZlu', 'Ангел',    'Ангелов',   4),
    (10,  'b.borisova@students.bg',     '$2a$10$xh6NdTDAHBbT5c3CoQ.DQevUFcnMgxsYFxBFluURaaxPDCZrxoZlu', 'Борислава','Борисова',  4),

    -- Students (School 1 — 11Б)
    (11, 'v.vasilev@students.bg',      '$2a$10$xh6NdTDAHBbT5c3CoQ.DQevUFcnMgxsYFxBFluURaaxPDCZrxoZlu', 'Виктор',   'Василев',   4),
    (12, 'g.georgieva@students.bg',    '$2a$10$xh6NdTDAHBbT5c3CoQ.DQevUFcnMgxsYFxBFluURaaxPDCZrxoZlu', 'Габриела', 'Георгиева', 4),

    -- Students (School 2 — 9А)
    (13, 'd.dimitrova@students.bg',    '$2a$10$xh6NdTDAHBbT5c3CoQ.DQevUFcnMgxsYFxBFluURaaxPDCZrxoZlu', 'Диана',    'Димитрова', 4),
    (14, 'e.enchev@students.bg',       '$2a$10$xh6NdTDAHBbT5c3CoQ.DQevUFcnMgxsYFxBFluURaaxPDCZrxoZlu', 'Емил',     'Енчев',     4),

    -- Students (School 2 — 10А)
    (15, 'zh.zhelev@students.bg',      '$2a$10$xh6NdTDAHBbT5c3CoQ.DQevUFcnMgxsYFxBFluURaaxPDCZrxoZlu', 'Живко',    'Желев',     4),
    (16, 'i.ivanova@students.bg',      '$2a$10$xh6NdTDAHBbT5c3CoQ.DQevUFcnMgxsYFxBFluURaaxPDCZrxoZlu', 'Ивелина',  'Иванова',   4),

    -- Parents (Schools 1–2)
    (17, 'n.angelova@parents.bg',      '$2a$10$xh6NdTDAHBbT5c3CoQ.DQevUFcnMgxsYFxBFluURaaxPDCZrxoZlu', 'Надя',        'Ангелова',   5),  -- parent of 9, 10
    (18, 'k.vasilev@parents.bg',       '$2a$10$xh6NdTDAHBbT5c3CoQ.DQevUFcnMgxsYFxBFluURaaxPDCZrxoZlu', 'Калоян',      'Василев',    5),  -- parent of 11, 12
    (19, 'l.dimitrova@parents.bg',     '$2a$10$xh6NdTDAHBbT5c3CoQ.DQevUFcnMgxsYFxBFluURaaxPDCZrxoZlu', 'Лиляна',      'Димитрова',  5),  -- parent of 13, 14
    (20, 'm.zhelev@parents.bg',        '$2a$10$xh6NdTDAHBbT5c3CoQ.DQevUFcnMgxsYFxBFluURaaxPDCZrxoZlu', 'Методи',      'Желев',      5),  -- parent of 15, 16

    -- Headmasters (Schools 3–4)
    (21, 'kr.todorova@school3.bg',     '$2a$10$xh6NdTDAHBbT5c3CoQ.DQevUFcnMgxsYFxBFluURaaxPDCZrxoZlu', 'Красимира',   'Тодорова',   2),
    (22, 'iv.marinov@school4.bg',      '$2a$10$xh6NdTDAHBbT5c3CoQ.DQevUFcnMgxsYFxBFluURaaxPDCZrxoZlu', 'Ивайло',      'Маринов',    2),

    -- Teachers (School 3)
    (23, 'r.petkov@school3.bg',        '$2a$10$xh6NdTDAHBbT5c3CoQ.DQevUFcnMgxsYFxBFluURaaxPDCZrxoZlu', 'Ростислав',   'Петков',     3),
    (24, 'n.stoilova@school3.bg',      '$2a$10$xh6NdTDAHBbT5c3CoQ.DQevUFcnMgxsYFxBFluURaaxPDCZrxoZlu', 'Надежда',     'Стоилова',   3),

    -- Teachers (School 4)
    (25, 'sv.koleva@school4.bg',       '$2a$10$xh6NdTDAHBbT5c3CoQ.DQevUFcnMgxsYFxBFluURaaxPDCZrxoZlu', 'Светлана',    'Колева',     3),
    (26, 'b.popova@school4.bg',        '$2a$10$xh6NdTDAHBbT5c3CoQ.DQevUFcnMgxsYFxBFluURaaxPDCZrxoZlu', 'Бояна',       'Попова',     3),

    -- Students (School 3 — 8В)
    (27, 'k.kostadinov@students.bg',   '$2a$10$xh6NdTDAHBbT5c3CoQ.DQevUFcnMgxsYFxBFluURaaxPDCZrxoZlu', 'Красимир',    'Костадинов', 4),
    (28, 'm.mihailova@students.bg',    '$2a$10$xh6NdTDAHBbT5c3CoQ.DQevUFcnMgxsYFxBFluURaaxPDCZrxoZlu', 'Маргарита',   'Михайлова',  4),

    -- Students (School 3 — 9Б)
    (29, 'ni.nedelchev@students.bg',   '$2a$10$xh6NdTDAHBbT5c3CoQ.DQevUFcnMgxsYFxBFluURaaxPDCZrxoZlu', 'Николай',     'Неделчев',   4),
    (30, 'pe.panova@students.bg',      '$2a$10$xh6NdTDAHBbT5c3CoQ.DQevUFcnMgxsYFxBFluURaaxPDCZrxoZlu', 'Петя',        'Панова',     4),

    -- Students (School 4 — 10В)
    (31, 'ra.rusev@students.bg',       '$2a$10$xh6NdTDAHBbT5c3CoQ.DQevUFcnMgxsYFxBFluURaaxPDCZrxoZlu', 'Радослав',    'Русев',      4),
    (32, 'si.simeonova@students.bg',   '$2a$10$xh6NdTDAHBbT5c3CoQ.DQevUFcnMgxsYFxBFluURaaxPDCZrxoZlu', 'Симона',      'Симеонова',  4),

    -- Students (School 4 — 11А)
    (33, 'to.todorov@students.bg',     '$2a$10$xh6NdTDAHBbT5c3CoQ.DQevUFcnMgxsYFxBFluURaaxPDCZrxoZlu', 'Тодор',       'Тодоров',    4),
    (34, 'fa.filipova@students.bg',    '$2a$10$xh6NdTDAHBbT5c3CoQ.DQevUFcnMgxsYFxBFluURaaxPDCZrxoZlu', 'Фани',        'Филипова',   4),

    -- Parents (Schools 3–4)
    (35, 'al.kostadinova@parents.bg',  '$2a$10$xh6NdTDAHBbT5c3CoQ.DQevUFcnMgxsYFxBFluURaaxPDCZrxoZlu', 'Александра',  'Костадинова',5),  -- parent of 27, 28
    (36, 'bo.nedelchev@parents.bg',    '$2a$10$xh6NdTDAHBbT5c3CoQ.DQevUFcnMgxsYFxBFluURaaxPDCZrxoZlu', 'Борислав',    'Неделчев',   5),  -- parent of 29, 30
    (37, 've.rusev@parents.bg',        '$2a$10$xh6NdTDAHBbT5c3CoQ.DQevUFcnMgxsYFxBFluURaaxPDCZrxoZlu', 'Венелин',     'Русев',      5),  -- parent of 31, 32
    (38, 'go.todorov@parents.bg',      '$2a$10$xh6NdTDAHBbT5c3CoQ.DQevUFcnMgxsYFxBFluURaaxPDCZrxoZlu', 'Горан',       'Тодоров',    5);  -- parent of 33, 34


-- -------------------------------------------------------------
-- 3. SCHOOLS
-- DirectorId set after Users are inserted
-- -------------------------------------------------------------
INSERT INTO Schools (Id, Name, Type, Address, DirectorId) VALUES
    (1, 'СОУ "Христо Ботев"',           NULL,           'ул. „Шипка" 34, София',            3),
    (2, 'СОУ "Иван Вазов"',             NULL,           'бул. „Марица" 12, Пловдив',         4),
    (3, 'ОУ "Васил Левски"',            'GENERAL',      'ул. „Дунав" 7, Варна',              21),
    (4, 'ПГЕЕ "Константин Фотинов"',    'PROFESSIONAL', 'бул. „Александровска" 21, Бургас',  22);


-- -------------------------------------------------------------
-- 4. CLASSES
-- -------------------------------------------------------------
INSERT INTO Classes (Id, Name, SchoolYear, SchoolId) VALUES
    (1, '10А', '2024/2025', 1),
    (2, '11Б', '2024/2025', 1),
    (3, '9А',  '2024/2025', 2),
    (4, '10А', '2024/2025', 2),
    (5, '8В',  '2024/2025', 3),
    (6, '9Б',  '2024/2025', 3),
    (7, '10В', '2024/2025', 4),
    (8, '11А', '2024/2025', 4);


-- -------------------------------------------------------------
-- 5. TEACHERS
-- -------------------------------------------------------------
INSERT INTO Teachers (UserId, SchoolId) VALUES
    (5,  1),   -- Стефан Димитров  → School 1
    (6,  1),   -- Елена Иванова    → School 1
    (7,  2),   -- Теодора Стоянова → School 2
    (8,  2),   -- Христо Христов   → School 2
    (23, 3),   -- Ростислав Петков → School 3
    (24, 3),   -- Надежда Стоилова → School 3
    (25, 4),   -- Светлана Колева  → School 4
    (26, 4);   -- Бояна Попова     → School 4


-- -------------------------------------------------------------
-- 6. STUDENTS
-- -------------------------------------------------------------
INSERT INTO Students (UserId, SchoolId, ClassId) VALUES
    (9,  1, 1),   -- Ангел      → School 1, 10А
    (10, 1, 1),   -- Борислава  → School 1, 10А
    (11, 1, 2),   -- Виктор     → School 1, 11Б
    (12, 1, 2),   -- Габриела   → School 1, 11Б
    (13, 2, 3),   -- Диана      → School 2, 9А
    (14, 2, 3),   -- Емил       → School 2, 9А
    (15, 2, 4),   -- Живко      → School 2, 10А
    (16, 2, 4),   -- Ивелина    → School 2, 10А
    (27, 3, 5),   -- Красимир   → School 3, 8В
    (28, 3, 5),   -- Маргарита  → School 3, 8В
    (29, 3, 6),   -- Николай    → School 3, 9Б
    (30, 3, 6),   -- Петя       → School 3, 9Б
    (31, 4, 7),   -- Радослав   → School 4, 10В
    (32, 4, 7),   -- Симона     → School 4, 10В
    (33, 4, 8),   -- Тодор      → School 4, 11А
    (34, 4, 8);   -- Фани       → School 4, 11А


-- -------------------------------------------------------------
-- 7. PARENT–STUDENT
-- -------------------------------------------------------------
INSERT INTO Parent_Student (ParentId, StudentId) VALUES
    (17, 9),    -- Надя         → Ангел
    (17, 10),   -- Надя         → Борислава
    (18, 11),   -- Калоян       → Виктор
    (18, 12),   -- Калоян       → Габриела
    (19, 13),   -- Лиляна       → Диана
    (19, 14),   -- Лиляна       → Емил
    (20, 15),   -- Методи       → Живко
    (20, 16),   -- Методи       → Ивелина
    (35, 27),   -- Александра   → Красимир
    (35, 28),   -- Александра   → Маргарита
    (36, 29),   -- Борислав     → Николай
    (36, 30),   -- Борислав     → Петя
    (37, 31),   -- Венелин      → Радослав
    (37, 32),   -- Венелин      → Симона
    (38, 33),   -- Горан        → Тодор
    (38, 34);   -- Горан        → Фани


-- -------------------------------------------------------------
-- 8. SUBJECTS
-- -------------------------------------------------------------
INSERT INTO Subjects (Id, Name) VALUES
    (1, 'Математика'),
    (2, 'Физика и астрономия'),
    (3, 'Български език и литература'),
    (4, 'История и цивилизации'),
    (5, 'География и икономика'),
    (6, 'Химия и опазване на околната среда'),
    (7, 'Биология и здравно образование'),
    (8, 'Английски език');


-- -------------------------------------------------------------
-- 9. TEACHER QUALIFICATIONS
-- -------------------------------------------------------------
INSERT INTO Teacher_Qualifications (TeacherId, SubjectId) VALUES
    (5,  1),   -- Стефан    → Математика
    (5,  2),   -- Стефан    → Физика
    (6,  3),   -- Елена     → Български
    (7,  4),   -- Теодора   → История
    (7,  5),   -- Теодора   → География
    (8,  1),   -- Христо    → Математика
    (8,  6),   -- Христо    → Химия
    (23, 1),   -- Ростислав → Математика
    (23, 7),   -- Ростислав → Биология
    (24, 7),   -- Надежда   → Биология
    (24, 6),   -- Надежда   → Химия
    (25, 4),   -- Светлана  → История
    (25, 8),   -- Светлана  → Английски
    (26, 1),   -- Бояна     → Математика
    (26, 2);   -- Бояна     → Физика


-- -------------------------------------------------------------
-- 10. SCHEDULES  (SchoolId, ClassId, SubjectId, TeacherId, Term)
-- -------------------------------------------------------------
INSERT INTO Schedules (Id, SchoolId, ClassId, SubjectId, TeacherId, Term) VALUES
    -- School 1 — 10А
    (1,  1, 1, 1, 5, 1),   -- Математика    / Стефан
    (2,  1, 1, 2, 5, 1),   -- Физика        / Стефан
    (3,  1, 1, 3, 6, 1),   -- Български     / Елена

    -- School 1 — 11Б
    (4,  1, 2, 1, 5, 1),   -- Математика    / Стефан
    (5,  1, 2, 3, 6, 1),   -- Български     / Елена

    -- School 2 — 9А
    (6,  2, 3, 1, 8, 1),   -- Математика    / Христо
    (7,  2, 3, 4, 7, 1),   -- История       / Теодора

    -- School 2 — 10А
    (8,  2, 4, 1, 8,  1),   -- Математика    / Христо
    (9,  2, 4, 6, 8,  1),   -- Химия         / Христо
    (10, 2, 4, 5, 7,  1),   -- География     / Теодора

    -- School 3 — 8В
    (11, 3, 5, 1, 23, 1),   -- Математика    / Ростислав
    (12, 3, 5, 7, 24, 1),   -- Биология      / Надежда

    -- School 3 — 9Б
    (13, 3, 6, 1, 23, 1),   -- Математика    / Ростислав
    (14, 3, 6, 6, 24, 1),   -- Химия         / Надежда

    -- School 4 — 10В
    (15, 4, 7, 4, 25, 1),   -- История       / Светлана
    (16, 4, 7, 8, 25, 1),   -- Английски     / Светлана
    (17, 4, 7, 1, 26, 1),   -- Математика    / Бояна

    -- School 4 — 11А
    (18, 4, 8, 8, 25, 1),   -- Английски     / Светлана
    (19, 4, 8, 1, 26, 1),   -- Математика    / Бояна
    (20, 4, 8, 2, 26, 1);   -- Физика        / Бояна


-- -------------------------------------------------------------
-- 11. GRADES  (Value range: 2.00–6.00)
-- -------------------------------------------------------------
INSERT INTO Grades (StudentId, ScheduleId, Value, CreatedAt) VALUES
    -- Ангел (10А, School 1)
    (9,  1, 5.50, '2024-11-15 09:00:00'),   -- Математика
    (9,  2, 4.75, '2024-11-20 10:00:00'),   -- Физика
    (9,  3, 6.00, '2024-11-25 11:00:00'),   -- Български

    -- Борислава (10А, School 1)
    (10, 1, 3.50, '2024-11-15 09:05:00'),   -- Математика
    (10, 2, 4.00, '2024-11-20 10:05:00'),   -- Физика
    (10, 3, 5.25, '2024-11-25 11:05:00'),   -- Български

    -- Виктор (11Б, School 1)
    (11, 4, 4.50, '2024-11-15 09:00:00'),   -- Математика
    (11, 5, 5.75, '2024-11-22 11:00:00'),   -- Български

    -- Габриела (11Б, School 1)
    (12, 4, 2.50, '2024-11-15 09:05:00'),   -- Математика
    (12, 5, 3.00, '2024-11-22 11:05:00'),   -- Български

    -- Диана (9А, School 2)
    (13, 6, 5.00, '2024-11-18 09:00:00'),   -- Математика
    (13, 7, 4.50, '2024-11-21 10:00:00'),   -- История

    -- Емил (9А, School 2)
    (14, 6, 6.00, '2024-11-18 09:10:00'),   -- Математика
    (14, 7, 5.50, '2024-11-21 10:10:00'),   -- История

    -- Живко (10А, School 2)
    (15, 8, 4.00, '2024-11-19 09:00:00'),   -- Математика
    (15, 9, 3.75, '2024-11-22 10:00:00'),   -- Химия
    (15, 10, 4.25,'2024-11-26 11:00:00'),   -- География

    -- Ивелина (10А, School 2)
    (16, 8,  5.50, '2024-11-19 09:10:00'),   -- Математика
    (16, 9,  5.00, '2024-11-22 10:10:00'),   -- Химия
    (16, 10, 6.00, '2024-11-26 11:10:00'),   -- География

    -- Красимир (8В, School 3)
    (27, 11, 5.00, '2024-11-15 09:00:00'),   -- Математика
    (27, 12, 4.50, '2024-11-20 10:00:00'),   -- Биология

    -- Маргарита (8В, School 3)
    (28, 11, 4.25, '2024-11-15 09:05:00'),   -- Математика
    (28, 12, 5.50, '2024-11-20 10:05:00'),   -- Биология

    -- Николай (9Б, School 3)
    (29, 13, 3.75, '2024-11-17 09:00:00'),   -- Математика
    (29, 14, 4.00, '2024-11-24 10:00:00'),   -- Химия

    -- Петя (9Б, School 3)
    (30, 13, 5.25, '2024-11-17 09:05:00'),   -- Математика
    (30, 14, 5.75, '2024-11-24 10:05:00'),   -- Химия

    -- Радослав (10В, School 4)
    (31, 15, 4.50, '2024-11-18 09:00:00'),   -- История
    (31, 16, 5.00, '2024-11-25 10:00:00'),   -- Английски
    (31, 17, 3.50, '2024-11-28 11:00:00'),   -- Математика

    -- Симона (10В, School 4)
    (32, 15, 5.50, '2024-11-18 09:05:00'),   -- История
    (32, 16, 6.00, '2024-11-25 10:05:00'),   -- Английски
    (32, 17, 5.00, '2024-11-28 11:05:00'),   -- Математика

    -- Тодор (11А, School 4)
    (33, 18, 4.00, '2024-11-19 09:00:00'),   -- Английски
    (33, 19, 2.50, '2024-11-26 10:00:00'),   -- Математика
    (33, 20, 3.50, '2024-11-29 11:00:00'),   -- Физика

    -- Фани (11А, School 4)
    (34, 18, 5.75, '2024-11-19 09:05:00'),   -- Английски
    (34, 19, 4.75, '2024-11-26 10:05:00'),   -- Математика
    (34, 20, 5.25, '2024-11-29 11:05:00');   -- Физика


-- -------------------------------------------------------------
-- 12. ABSENCES
-- -------------------------------------------------------------
INSERT INTO Absences (StudentId, ScheduleId, Date, IsExcused) VALUES
    -- Ангел
    (9,  1, '2024-10-05', FALSE),   -- Математика — неизвинено
    (9,  3, '2024-10-12', TRUE),    -- Български  — извинено

    -- Борислава
    (10, 2, '2024-10-08', FALSE),   -- Физика — неизвинено

    -- Виктор
    (11, 4, '2024-10-10', TRUE),    -- Математика — извинено (болест)
    (11, 5, '2024-10-10', TRUE),    -- Български  — извинено (болест)

    -- Габриела
    (12, 4, '2024-10-15', FALSE),   -- Математика — неизвинено
    (12, 4, '2024-10-22', FALSE),   -- Математика — неизвинено
    (12, 5, '2024-10-17', FALSE),   -- Български  — неизвинено

    -- Диана
    (13, 6, '2024-10-07', FALSE),   -- Математика — неизвинено

    -- Емил
    (14, 7, '2024-10-14', TRUE),    -- История — извинено

    -- Живко
    (15, 8, '2024-10-09', FALSE),   -- Математика — неизвинено
    (15, 10,'2024-10-16', TRUE),    -- География  — извинено

    -- Ивелина
    (16, 9,  '2024-10-11', FALSE),   -- Химия — неизвинено

    -- Красимир (8В, School 3)
    (27, 11, '2024-10-07', FALSE),   -- Математика — неизвинено

    -- Маргарита (8В, School 3)
    (28, 12, '2024-10-14', TRUE),    -- Биология — извинено

    -- Николай (9Б, School 3)
    (29, 13, '2024-10-09', FALSE),   -- Математика — неизвинено
    (29, 14, '2024-10-16', FALSE),   -- Химия — неизвинено

    -- Петя (9Б, School 3)
    (30, 14, '2024-10-21', TRUE),    -- Химия — извинено

    -- Радослав (10В, School 4)
    (31, 17, '2024-10-08', FALSE),   -- Математика — неизвинено
    (31, 15, '2024-10-22', TRUE),    -- История — извинено

    -- Симона (10В, School 4)
    (32, 16, '2024-10-11', FALSE),   -- Английски — неизвинено

    -- Тодор (11А, School 4)
    (33, 19, '2024-10-05', FALSE),   -- Математика — неизвинено
    (33, 20, '2024-10-12', FALSE),   -- Физика — неизвинено
    (33, 18, '2024-10-19', FALSE),   -- Английски — неизвинено

    -- Фани (11А, School 4)
    (34, 19, '2024-10-08', TRUE);    -- Математика — извинено


-- -------------------------------------------------------------
-- 13. SCHOOL SCHEDULE ENTRIES  (Id, SchoolId, Type, Label, StartTime, EndTime, EventDate, SortOrder)
-- -------------------------------------------------------------
INSERT INTO school_schedule_entries (id, schoolid, type, label, start_time, end_time, event_date, sort_order) VALUES

    -- ── School 1 — СОУ "Христо Ботев", София (7 периода) ──────────────
    (1,  1, 'LECTURE',       'Час 1',                    '08:00', '08:45', NULL,         1),
    (2,  1, 'BREAK',         'Малко междучасие',         '08:45', '08:55', NULL,         2),
    (3,  1, 'LECTURE',       'Час 2',                    '08:55', '09:40', NULL,         3),
    (4,  1, 'BREAK',         'Малко междучасие',         '09:40', '09:50', NULL,         4),
    (5,  1, 'LECTURE',       'Час 3',                    '09:50', '10:35', NULL,         5),
    (6,  1, 'BREAK',         'Голямо междучасие',        '10:35', '10:55', NULL,         6),
    (7,  1, 'LECTURE',       'Час 4',                    '10:55', '11:40', NULL,         7),
    (8,  1, 'BREAK',         'Малко междучасие',         '11:40', '11:50', NULL,         8),
    (9,  1, 'LECTURE',       'Час 5',                    '11:50', '12:35', NULL,         9),
    (10, 1, 'BREAK',         'Малко междучасие',         '12:35', '12:45', NULL,         10),
    (11, 1, 'LECTURE',       'Час 6',                    '12:45', '13:30', NULL,         11),
    (12, 1, 'BREAK',         'Малко междучасие',         '13:30', '13:40', NULL,         12),
    (13, 1, 'LECTURE',       'Час 7',                    '13:40', '14:25', NULL,         13),
    (14, 1, 'SPECIAL_EVENT', 'Ден на народните будители','08:00', '14:00', '2024-11-01', 14),

    -- ── School 2 — СОУ "Иван Вазов", Пловдив (7 периода) ─────────────
    (15, 2, 'LECTURE',       'Час 1',                    '08:00', '08:45', NULL,         1),
    (16, 2, 'BREAK',         'Малко междучасие',         '08:45', '08:55', NULL,         2),
    (17, 2, 'LECTURE',       'Час 2',                    '08:55', '09:40', NULL,         3),
    (18, 2, 'BREAK',         'Малко междучасие',         '09:40', '09:50', NULL,         4),
    (19, 2, 'LECTURE',       'Час 3',                    '09:50', '10:35', NULL,         5),
    (20, 2, 'BREAK',         'Голямо междучасие',        '10:35', '10:55', NULL,         6),
    (21, 2, 'LECTURE',       'Час 4',                    '10:55', '11:40', NULL,         7),
    (22, 2, 'BREAK',         'Малко междучасие',         '11:40', '11:50', NULL,         8),
    (23, 2, 'LECTURE',       'Час 5',                    '11:50', '12:35', NULL,         9),
    (24, 2, 'BREAK',         'Малко междучасие',         '12:35', '12:45', NULL,         10),
    (25, 2, 'LECTURE',       'Час 6',                    '12:45', '13:30', NULL,         11),
    (26, 2, 'BREAK',         'Малко междучасие',         '13:30', '13:40', NULL,         12),
    (27, 2, 'LECTURE',       'Час 7',                    '13:40', '14:25', NULL,         13),
    (28, 2, 'SPECIAL_EVENT', 'Коледен концерт',          '10:00', '13:00', '2024-12-20', 14),

    -- ── School 3 — ОУ "Васил Левски", Варна (6 периода, начален курс) ─
    (29, 3, 'LECTURE',       'Час 1',                    '08:00', '08:40', NULL,         1),
    (30, 3, 'BREAK',         'Малко междучасие',         '08:40', '08:50', NULL,         2),
    (31, 3, 'LECTURE',       'Час 2',                    '08:50', '09:30', NULL,         3),
    (32, 3, 'BREAK',         'Голямо междучасие',        '09:30', '09:50', NULL,         4),
    (33, 3, 'LECTURE',       'Час 3',                    '09:50', '10:30', NULL,         5),
    (34, 3, 'BREAK',         'Малко междучасие',         '10:30', '10:40', NULL,         6),
    (35, 3, 'LECTURE',       'Час 4',                    '10:40', '11:20', NULL,         7),
    (36, 3, 'BREAK',         'Малко междучасие',         '11:20', '11:30', NULL,         8),
    (37, 3, 'LECTURE',       'Час 5',                    '11:30', '12:10', NULL,         9),
    (38, 3, 'BREAK',         'Малко междучасие',         '12:10', '12:20', NULL,         10),
    (39, 3, 'LECTURE',       'Час 6',                    '12:20', '13:00', NULL,         11),
    (40, 3, 'SPECIAL_EVENT', 'Спортен ден',              '09:00', '14:00', '2024-10-15', 12),

    -- ── School 4 — ПГЕЕ "Константин Фотинов", Бургас (7 периода + обедна почивка) ─
    (41, 4, 'LECTURE',       'Час 1',                    '08:00', '08:45', NULL,         1),
    (42, 4, 'BREAK',         'Малко междучасие',         '08:45', '08:55', NULL,         2),
    (43, 4, 'LECTURE',       'Час 2',                    '08:55', '09:40', NULL,         3),
    (44, 4, 'BREAK',         'Малко междучасие',         '09:40', '09:50', NULL,         4),
    (45, 4, 'LECTURE',       'Час 3',                    '09:50', '10:35', NULL,         5),
    (46, 4, 'BREAK',         'Голямо междучасие',        '10:35', '10:55', NULL,         6),
    (47, 4, 'LECTURE',       'Час 4',                    '10:55', '11:40', NULL,         7),
    (48, 4, 'BREAK',         'Малко междучасие',         '11:40', '11:50', NULL,         8),
    (49, 4, 'LECTURE',       'Час 5',                    '11:50', '12:35', NULL,         9),
    (50, 4, 'BREAK',         'Обедна почивка',           '12:35', '13:05', NULL,         10),
    (51, 4, 'LECTURE',       'Час 6',                    '13:05', '13:50', NULL,         11),
    (52, 4, 'BREAK',         'Малко междучасие',         '13:50', '14:00', NULL,         12),
    (53, 4, 'LECTURE',       'Час 7',                    '14:00', '14:45', NULL,         13),
    (54, 4, 'SPECIAL_EVENT', 'Открити врати',            '10:00', '15:00', '2025-03-08', 14);


-- -------------------------------------------------------------
-- Reset sequences so future INSERTs get correct next IDs
-- (Only needed if tables use SERIAL / GENERATED AS IDENTITY)
-- -------------------------------------------------------------
SELECT setval(pg_get_serial_sequence('roles',     'id'), 5);
SELECT setval(pg_get_serial_sequence('users',     'id'), 38);
SELECT setval(pg_get_serial_sequence('schools',   'id'), 4);
SELECT setval(pg_get_serial_sequence('classes',   'id'), 8);
SELECT setval(pg_get_serial_sequence('subjects',  'id'), 8);
SELECT setval(pg_get_serial_sequence('schedules', 'id'), 20);
SELECT setval(pg_get_serial_sequence('grades',    'id'), 40);
SELECT setval(pg_get_serial_sequence('absences',  'id'), 25);
SELECT setval(pg_get_serial_sequence('school_schedule_entries', 'id'), 54);
