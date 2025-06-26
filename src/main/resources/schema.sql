CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    device_token VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS events (
    id UUID PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description VARCHAR(255),
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NOT NULL,
    event_location VARCHAR(255) NOT NULL,
    completed BOOLEAN NOT NULL,
    is_shared BOOLEAN NOT NULL,
    organizer_id BIGSERIAL NOT NULL,
    FOREIGN KEY (organizer_id) REFERENCES users (id)
);

CREATE TABLE IF NOT EXISTS tasks (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description VARCHAR(255) NOT NULL,
    is_completed BOOLEAN,
    FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE TABLE IF NOT EXISTS tags (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS group_chats (
    id BIGSERIAL PRIMARY KEY,
    FOREIGN KEY (admin_id) REFERENCES users (id)
);

CREATE TABLE IF NOT EXISTS group_messages (
    id BIGSERIAL PRIMARY KEY,
    message_text VARCHAR(255) NOT NULL,
    time_sent TIMESTAMP NOT NULL,
    FOREIGN KEY (chat_id) REFERENCES group_chats (id),
    FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE TABLE IF NOT EXISTS private_chats (
    id BIGSERIAL PRIMARY KEY,
    FOREIGN KEY (user_id1) REFERENCES users (id),
    FOREIGN KEY (user_id2) REFERENCES users (id)
);

CREATE TABLE IF NOT EXISTS private_messages (
    id BIGSERIAL PRIMARY KEY,
    message_text VARCHAR(255) NOT NULL,
    time_sent TIMESTAMP NOT NULL,
    FOREIGN KEY (chat_id) REFERENCES private_chats (id),
    FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE TABLE IF NOT EXISTS friendships (
    id BIGSERIAL PRIMARY KEY,
    FOREIGN KEY (user_id1) REFERENCES users (id),
    FOREIGN KEY (user_id2) REFERENCES users (id)
);

CREATE TABLE IF NOT EXISTS statistics (
    id BIGSERIAL PRIMARY KEY,
    FOREIGN KEY (user_id) REFERENCES users (id)
);