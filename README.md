# Yootiful Vector DBs

Spring Boot 3.2 (Java 21) demo that ingests product rows from PostgreSQL into a pgvector-backed `vector_store` using Spring AI and Ollama embeddings, then runs a similarity search for "green trousers for winter" on startup.

## Prerequisites
- Java 21
- Maven (or the included `mvnw` / `mvnw.cmd` wrappers)
- Docker + Docker Compose (for pgvector)
- Python 3 with `psycopg2` (`pip install psycopg2-binary` works for local testing)
- Ollama running locally with the embedding model `rjmalagon/gte-qwen2-1.5b-instruct-embed-f16:latest` pulled (`ollama pull rjmalagon/gte-qwen2-1.5b-instruct-embed-f16:latest`)
- Free ports: 5432 (Postgres) and 11434 (Ollama default)

## Quick start (TL;DR)
```bash
# 1) Start pgvector
docker compose up -d pgvector

# 2) (Optional) Initialize schema
# Replace <pgcontainer> with the running container name, e.g., yootiful-vectordbs-pgvector-1
docker exec -i <pgcontainer> psql -U admin -d clothing_store < data/database.sql

# 3) Load sample data into the product table
pip install psycopg2-binary
python data/read_csv_and_populate_database.py

# 4) Run the app (Windows)
.\mvnw.cmd spring-boot:run
# Or build + run
.\mvnw.cmd package
java -jar target/yootiful-vectordbs-0.0.1-SNAPSHOT.jar
```

## Configure the application
Align the datasource with the compose DB (name `clothing_store`, user `admin`, password `admin`). Either edit `src/main/resources/application.properties` or set env vars. Example env override:
```bash
set SPRING_DATASOURCE_URL=jdbc:postgresql://localhost/clothing_store
set SPRING_DATASOURCE_USERNAME=admin
set SPRING_DATASOURCE_PASSWORD=admin
```
Optional toggles (disabled by default) in `application.properties`:
- `spring.ai.vectorstore.pgvector.initialize-schema=true` to let Spring AI create the vector table
- `spring.datasource.initialize=true` to let Spring Boot run SQL init scripts

## Database and data setup (detail)
1) Bring up Postgres/pgvector
```bash
docker compose up -d pgvector
```
The compose file provisions `clothing_store` with user `admin` and password `admin` on localhost:5432.

2) Create tables (if not using the optional Spring init flags)
```bash
# Use the running container name from `docker ps`
docker exec -i <pgcontainer> psql -U admin -d clothing_store < data/database.sql
```

3) Load sample products
- The repo includes `data/clothing_combinations.csv` with sample rows. The loader script expects this path by default.
- To regenerate data, run `python data/create_data_and_write_to_file.py` (writes `data/clothing_items.csv`); then either update the loader path or rename the file before loading.

Run the loader:
```bash
pip install psycopg2-binary
python data/read_csv_and_populate_database.py
```

## Run the application
```bash
# From the repo root
# Windows
.\mvnw.cmd spring-boot:run
# macOS/Linux
./mvnw spring-boot:run
```
Alternatively, build a jar and run:
```bash
./mvnw package
java -jar target/yootiful-vectordbs-0.0.1-SNAPSHOT.jar
```

## What to expect at startup
- Logs show ingestion of all rows from the `product` table into the `vector_store`.
- A similarity search for "green trousers for winter" runs; matching product metadata is printed to the console.

## Troubleshooting
- **DB connection refused/auth failed**: ensure `docker compose up -d pgvector` is running; confirm env vars/`application.properties` use `clothing_store`/`admin`/`admin` and port 5432 is free.
- **Missing tables**: run `data/database.sql` or enable the init flags in `application.properties`.
- **No search results or zero count**: verify the loader succeeded; check `select count(*) from product;` inside the database and that `vector_store` has rows.
- **Ollama errors/model not found**: start Ollama locally and pull the configured model; adjust `spring.ai.ollama.embedding.model` if you use a different one.
- **psycopg2 install issues**: use `pip install psycopg2-binary` on local dev machines.

## Useful file map
- `compose.yaml` — pgvector container config (DB name `clothing_store`, credentials `admin`/`admin`)
- `data/database.sql` — schema for `product` and `vector_store`
- `data/create_data_and_write_to_file.py` — generates sample CSV data
- `data/read_csv_and_populate_database.py` — loads CSV rows into `product`
- `src/main/resources/application.properties` — datasource and Spring AI settings
- `src/main/java/com/yootiful/vectordbs/YootifulVectordbsApplication.java` — startup ingestion and similarity search
