-- Сброс схемы
DROP SCHEMA IF EXISTS public CASCADE;
CREATE SCHEMA public;

-- Создание таблицы colors
CREATE TABLE public.colors (
    id SERIAL PRIMARY KEY,
    hash_code VARCHAR(6),
    name VARCHAR(50)
);

-- Создание таблицы users
CREATE TABLE public.users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(100) UNIQUE NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(60) NOT NULL,
    telegram_id BIGINT,
    created_at TIMESTAMPTZ DEFAULT now(),
    updated_at TIMESTAMPTZ
);

-- Создание таблицы roles
CREATE TABLE public.roles (
    id SERIAL PRIMARY KEY,
    name VARCHAR(60) UNIQUE NOT NULL
);

-- Создание таблицы projects
CREATE TABLE public.projects (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    owner_id BIGINT NOT NULL REFERENCES public.users(id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ DEFAULT now(),
    updated_at TIMESTAMPTZ DEFAULT now()
);

-- Создание таблицы kanban_groups
CREATE TABLE public.kanban_groups (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    order_position VARCHAR(8),
    project_id BIGINT NOT NULL REFERENCES public.projects(id) ON DELETE CASCADE
);

-- Создание таблицы tasks
CREATE TABLE public.tasks (
    id BIGSERIAL PRIMARY KEY,
    kanban_group_id BIGINT NOT NULL REFERENCES public.kanban_groups(id) ON DELETE CASCADE,
    author_id BIGINT NOT NULL REFERENCES public.users(id),
    title VARCHAR(100) NOT NULL,
    description TEXT,
    order_position VARCHAR(8),
    due_date TIMESTAMPTZ,
    priority INT,
    is_completed bool not null DEFAULT(false), 
    color INT REFERENCES public.colors(id),
    created_at TIMESTAMPTZ DEFAULT now(),
    updated_at TIMESTAMPTZ DEFAULT now()
);

-- Создание таблицы users_roles
CREATE TABLE public.users_roles (
    id SERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES public.users(id) ON DELETE CASCADE,
    role_id INT NOT NULL REFERENCES public.roles(id) ON DELETE CASCADE,
    UNIQUE (user_id, role_id)
);

-- Создание таблицы notifications
CREATE TABLE public.notifications (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES public.users(id),
    message TEXT NOT NULL,
    is_sent bool DEFAULT(false),
    send_at TIMESTAMPTZ not null
);

-- Вставка тестовых данных

INSERT INTO public.colors (hash_code, name) VALUES
('FF0000', 'Red'),
('00FF00', 'Green'),
('0000FF', 'Blue');

INSERT INTO public.users (username, email, password_hash) VALUES
('admin', 'admin@example.com', 'hashedpassword1'),
('user1', 'user1@example.com', 'hashedpassword2'),
('user2', 'user2@example.com', 'hashedpassword3');

INSERT INTO public.roles (name) VALUES
('ROLE_ADMIN'),
('ROLE_USER');

INSERT INTO public.projects (name, description, owner_id) VALUES
('Project Alpha', 'Description for Alpha', 1),
('Project Beta', 'Description for Beta', 2);

INSERT INTO public.kanban_groups (name, order_position, project_id) VALUES
('To Do', '1', 1),
('In Progress', '2', 1),
('Done', '3', 1);

INSERT INTO public.tasks (kanban_group_id, author_id, title, description, priority, color) VALUES
(1, 1, 'Task 1', 'Description of Task 1', 1, 1),
(2, 2, 'Task 2', 'Description of Task 2', 2, 2);


INSERT INTO public.users_roles (user_id, role_id) VALUES
(1, 1),
(2, 2);


INSERT INTO public.notifications (user_id, message, is_sent, send_at) VALUES
(1, 'You have a new task assigned.', false, now()),
(2, 'Your task has been completed.', false, now());
