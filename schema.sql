-- Users table with authentication fields
CREATE TABLE dm_users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    enabled BOOLEAN DEFAULT TRUE,
    account_non_expired BOOLEAN DEFAULT TRUE,
    account_non_locked BOOLEAN DEFAULT TRUE,
    credentials_non_expired BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- User roles table
CREATE TABLE user_roles (
    user_id BIGINT NOT NULL REFERENCES dm_users(id) ON DELETE CASCADE,
    role VARCHAR(50) NOT NULL,
    PRIMARY KEY (user_id, role)
);

-- Documents table (unchanged)
CREATE TABLE dm_documents (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    category VARCHAR(100),
    file_path VARCHAR(500),
    file_type VARCHAR(50),
    file_size BIGINT,
    extracted_text TEXT,
    document_date DATE,
    user_id BIGINT NOT NULL REFERENCES dm_users(id) ON DELETE CASCADE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tags table (unchanged)
CREATE TABLE dm_tags (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) UNIQUE NOT NULL
);

-- Many-to-many relationship (unchanged)
CREATE TABLE document_tags (
    document_id BIGINT REFERENCES dm_documents(id) ON DELETE CASCADE,
    tag_id BIGINT REFERENCES dm_tags(id) ON DELETE CASCADE,
    PRIMARY KEY (document_id, tag_id)
);

-- Refresh tokens table (optional - for storing refresh tokens in DB)
CREATE TABLE refresh_tokens (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES dm_users(id) ON DELETE CASCADE,
    token VARCHAR(500) UNIQUE NOT NULL,
    expiry_date TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for better performance
CREATE INDEX idx_documents_user_id ON dm_documents(user_id);
CREATE INDEX idx_documents_category ON dm_documents(category);
CREATE INDEX idx_documents_document_date ON dm_documents(document_date);
CREATE INDEX idx_user_roles_user_id ON user_roles(user_id);
CREATE INDEX idx_refresh_tokens_user_id ON refresh_tokens(user_id);
CREATE INDEX idx_refresh_tokens_token ON refresh_tokens(token);

-- Insert sample users with encrypted passwords (use BCrypt in application)
-- Password for all users: "password123"
-- These are BCrypt hashed passwords - DO NOT use in production
INSERT INTO dm_users (username, email, password, first_name, last_name, enabled, account_non_expired, account_non_locked, credentials_non_expired)
VALUES
    ('admin', 'admin@example.com', '$2a$10$slYQmyNdGzTn7ZLBXBChFOC9f6kFjAqPhccnP6DxlWXx2lPk1C3G6', 'Admin', 'User', true, true, true, true),
    ('john_doe', 'john@example.com', '$2a$10$slYQmyNdGzTn7ZLBXBChFOC9f6kFjAqPhccnP6DxlWXx2lPk1C3G6', 'John', 'Doe', true, true, true, true),
    ('jane_smith', 'jane@example.com', '$2a$10$slYQmyNdGzTn7ZLBXBChFOC9f6kFjAqPhccnP6DxlWXx2lPk1C3G6', 'Jane', 'Smith', true, true, true, true);

-- Assign roles to users
INSERT INTO user_roles (user_id, role)
VALUES
    (1, 'ROLE_ADMIN'),
    (1, 'ROLE_USER'),
    (2, 'ROLE_USER'),
    (3, 'ROLE_USER');