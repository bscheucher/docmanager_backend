-- Users table
CREATE TABLE dm_users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Documents table (updated with user_id foreign key)
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

-- Tags table
CREATE TABLE dm_tags (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) UNIQUE NOT NULL
);

-- Many-to-many relationship
CREATE TABLE document_tags (
    document_id BIGINT REFERENCES dm_documents(id),
    tag_id BIGINT REFERENCES dm_tags(id),
    PRIMARY KEY (document_id, tag_id)
);

-- Indexes for better performance
CREATE INDEX idx_documents_user_id ON dm_documents(user_id);
CREATE INDEX idx_documents_category ON dm_documents(category);
CREATE INDEX idx_documents_document_date ON dm_documents(document_date);