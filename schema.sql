-- Documents table
CREATE TABLE documents (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    category VARCHAR(100),
    file_path VARCHAR(500),
    file_type VARCHAR(50),
    file_size BIGINT,
    extracted_text TEXT,
    document_date DATE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tags table
CREATE TABLE tags (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) UNIQUE NOT NULL
);

-- Many-to-many relationship
CREATE TABLE document_tags (
    document_id BIGINT REFERENCES documents(id),
    tag_id BIGINT REFERENCES tags(id),
    PRIMARY KEY (document_id, tag_id)
);