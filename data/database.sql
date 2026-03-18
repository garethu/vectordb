CREATE TABLE product (
    id SERIAL PRIMARY KEY,
    uuid UUID,
    description TEXT
);

CREATE EXTENSION IF NOT EXISTS vector;
CREATE EXTENSION IF NOT EXISTS hstore;
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE IF NOT EXISTS vector_store (
	id uuid DEFAULT uuid_generate_v4() PRIMARY KEY,
	content text,
	metadata json,
	embedding vector(1536)
);

// To improve the search's performance, we can try to calculate the distance for only a subset of the vectors.
// Hierarchical Navigable Small Worlds (HNSW)
CREATE INDEX ON vector_store USING HNSW (embedding vector_cosine_ops);

