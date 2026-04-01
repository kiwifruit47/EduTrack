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

    -- Parents
    (17, 'n.angelova@parents.bg',      '$2a$10$xh6NdTDAHBbT5c3CoQ.DQevUFcnMgxsYFxBFluURaaxPDCZrxoZlu', 'Надя',     'Ангелова',  5),  -- parent of 8, 9
    (18, 'k.vasilev@parents.bg',       '$2a$10$xh6NdTDAHBbT5c3CoQ.DQevUFcnMgxsYFxBFluURaaxPDCZrxoZlu', 'Калоян',   'Василев',   5),  -- parent of 10, 11
    (19, 'l.dimitrova@parents.bg',     '$2a$10$xh6NdTDAHBbT5c3CoQ.DQevUFcnMgxsYFxBFluURaaxPDCZrxoZlu', 'Лиляна',   'Димитрова', 5),  -- parent of 12, 13
    (20, 'm.zhelev@parents.bg',        '$2a$10$xh6NdTDAHBbT5c3CoQ.DQevUFcnMgxsYFxBFluURaaxPDCZrxoZlu', 'Методи',   'Желев',     5);  -- parent of 14, 15


-- -------------------------------------------------------------
-- 3. SCHOOLS
-- DirectorId set after Users are inserted
-- -------------------------------------------------------------
INSERT INTO Schools (Id, Name, Type, Address, DirectorId) VALUES
    (1, 'СОУ "Христо Ботев"', NULL, 'ул. „Шипка" 34, София',    3),
    (2, 'СОУ "Иван Вазов"',   NULL, 'бул. „Марица" 12, Пловдив', 4);


-- -------------------------------------------------------------
-- 4. CLASSES
-- -------------------------------------------------------------
INSERT INTO Classes (Id, Name, SchoolYear, SchoolId) VALUES
    (1, '10А', '2024/2025', 1),
    (2, '11Б', '2024/2025', 1),
    (3, '9А',  '2024/2025', 2),
    (4, '10А', '2024/2025', 2);


-- -------------------------------------------------------------
-- 5. TEACHERS
-- -------------------------------------------------------------
INSERT INTO Teachers (UserId, SchoolId) VALUES
    (5, 1),   -- Стефан Димитров  → School 1
    (6, 1),   -- Елена Иванова    → School 1
    (7, 2),   -- Теодора Стоянова → School 2
    (8, 2);   -- Христо Христов   → School 2


-- -------------------------------------------------------------
-- 6. STUDENTS
-- -------------------------------------------------------------
INSERT INTO Students (UserId, SchoolId, ClassId) VALUES
    (9,  1, 1),   -- Ангел    → School 1, 10А
    (10, 1, 1),   -- Борислава→ School 1, 10А
    (11, 1, 2),   -- Виктор   → School 1, 11Б
    (12, 1, 2),   -- Габриела → School 1, 11Б
    (13, 2, 3),   -- Диана    → School 2, 9А
    (14, 2, 3),   -- Емил     → School 2, 9А
    (15, 2, 4),   -- Живко    → School 2, 10А
    (16, 2, 4);   -- Ивелина  → School 2, 10А


-- -------------------------------------------------------------
-- 7. PARENT–STUDENT
-- -------------------------------------------------------------
INSERT INTO Parent_Student (ParentId, StudentId) VALUES
    (17, 9),    -- Надя     → Ангел
    (17, 10),   -- Надя     → Борислава
    (18, 11),   -- Калоян   → Виктор
    (18, 12),   -- Калоян   → Габриела
    (19, 13),   -- Лиляна   → Диана
    (19, 14),   -- Лиляна   → Емил
    (20, 15),   -- Методи   → Живко
    (20, 16);   -- Методи   → Ивелина


-- -------------------------------------------------------------
-- 8. SUBJECTS
-- -------------------------------------------------------------
INSERT INTO Subjects (Id, Name) VALUES
    (1, 'Математика'),
    (2, 'Физика и астрономия'),
    (3, 'Български език и литература'),
    (4, 'История и цивилизации'),
    (5, 'География и икономика'),
    (6, 'Химия и опазване на околната среда');


-- -------------------------------------------------------------
-- 9. TEACHER QUALIFICATIONS
-- -------------------------------------------------------------
INSERT INTO Teacher_Qualifications (TeacherId, SubjectId) VALUES
    (5, 1),   -- Стефан  → Математика
    (5, 2),   -- Стефан  → Физика
    (6, 3),   -- Елена   → Български
    (7, 4),   -- Теодора → История
    (7, 5),   -- Теодора → География
    (8, 1),   -- Христо  → Математика
    (8, 6);   -- Христо  → Химия


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
    (8,  2, 4, 1, 8, 1),   -- Математика    / Христо
    (9,  2, 4, 6, 8, 1),   -- Химия         / Христо
    (10, 2, 4, 5, 7, 1);   -- География     / Теодора


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
    (16, 8, 5.50, '2024-11-19 09:10:00'),   -- Математика
    (16, 9, 5.00, '2024-11-22 10:10:00'),   -- Химия
    (16, 10, 6.00,'2024-11-26 11:10:00');   -- География


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
    (16, 9, '2024-10-11', FALSE);   -- Химия — неизвинено


-- -------------------------------------------------------------
-- Reset sequences so future INSERTs get correct next IDs
-- (Only needed if tables use SERIAL / GENERATED AS IDENTITY)
-- -------------------------------------------------------------
SELECT setval(pg_get_serial_sequence('roles',     'id'), 5);
SELECT setval(pg_get_serial_sequence('users',     'id'), 20);
SELECT setval(pg_get_serial_sequence('schools',   'id'), 2);
SELECT setval(pg_get_serial_sequence('classes',   'id'), 4);
SELECT setval(pg_get_serial_sequence('subjects',  'id'), 6);
SELECT setval(pg_get_serial_sequence('schedules', 'id'), 10);
SELECT setval(pg_get_serial_sequence('grades',    'id'), 20);
SELECT setval(pg_get_serial_sequence('absences',  'id'), 13);
