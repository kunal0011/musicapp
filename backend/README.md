# Music App - Backend Setup Guide

## Project Overview
This is a Spring Boot REST API for the Music App that uses:
- **PostgreSQL** for relational data storage
- **MinIO** for S3-compatible object storage (for music files, images, etc.)
- **Spring Data JPA** for database access
- **Lombok** for reducing boilerplate code

## Prerequisites
- Java 17+ (Currently using Java 25)
- Docker & Docker Compose
- Git

## Local Development Setup

### 1. Start Services with Docker Compose

From the project root directory (`/Users/kunalkumar/Desktop/musicapp`):

```bash
docker-compose up -d
```

This will start:
- **PostgreSQL**: Available at `localhost:5432`
  - Username: `musicapp_user`
  - Password: `musicapp_password`
  - Database: `musicapp`

- **MinIO**: 
  - API: `http://localhost:9000`
  - Console: `http://localhost:9001`
  - Username: `minioadmin`
  - Password: `minioadmin`

### 2. Build the Backend

```bash
cd backend
./gradlew clean build
```

### 3. Run the Application Locally

```bash
./gradlew bootRun
```

The API will be available at `http://localhost:8080`

## Configuration Files

### `src/main/resources/application.properties`
**Used for local development** - Connects to PostgreSQL at `localhost:5432`

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/musicapp
spring.datasource.username=musicapp_user
spring.datasource.password=musicapp_password
minio.url=http://localhost:9000
```

### `src/main/resources/application-docker.properties`
**Used when running in Docker Compose** - Connects to services by container name

```properties
spring.datasource.url=jdbc:postgresql://db:5432/musicapp
minio.url=http://minio:9000
```

### `src/test/resources/application.properties`
**Used for running tests** - Uses H2 in-memory database (no external services needed)

## Docker Compose Services

The `docker-compose.yml` file in the root directory defines:

### PostgreSQL Service (`db`)
- Container name: `musicapp_db`
- Port: `5432`
- Volume: `postgres_data` (persistent storage)

### MinIO Service (`minio`)
- Container name: `musicapp_storage`
- API Port: `9000`
- Console Port: `9001`
- Volume: `minio_data` (persistent storage)

### API Service (`api`)
- Container name: `musicapp_api`
- Port: `8080`
- Depends on: `db` and `minio`
- Uses `application-docker.properties` profile

## Building and Running with Docker

### Build the Docker Image
```bash
docker-compose build
```

### Run All Services
```bash
docker-compose up -d
```

### View Logs
```bash
docker-compose logs -f api
```

### Stop Services
```bash
docker-compose down
```

### Remove All Data (Reset)
```bash
docker-compose down -v
```

## Database Migration

The application uses Hibernate with `ddl-auto=update`, which means:
- Tables are automatically created/updated based on JPA entities
- Existing data is preserved during updates

For production, consider using Flyway or Liquibase for versioned migrations.

## MinIO Setup

### Access MinIO Console
1. Open browser: `http://localhost:9001`
2. Login with: `minioadmin` / `minioadmin`
3. Create buckets as needed (e.g., `musicapp`)

### S3-compatible API
Use the MinIO Java client to interact with object storage:
- Endpoint: `http://localhost:9000` (local) or `http://minio:9000` (Docker)
- Access Key: `minioadmin`
- Secret Key: `minioadmin`

## Testing

Run all tests:
```bash
./gradlew test
```

Tests use an H2 in-memory database and don't require external services.

## Project Dependencies

### Core
- `spring-boot-starter-web`: REST API support
- `spring-boot-starter-data-jpa`: Database ORM
- `postgresql`: PostgreSQL JDBC driver
- `minio`: S3-compatible object storage client
- `lombok`: Reduce boilerplate code

### Testing
- `spring-boot-starter-test`: Testing framework
- `h2`: In-memory database for tests

## Common Issues

### Cannot connect to PostgreSQL
- Ensure `docker-compose up -d` has been run
- Check if the `db` service is running: `docker-compose ps`
- Verify the connection URL matches the docker-compose configuration

### MinIO connection issues
- Check if `minio` service is running: `docker-compose ps`
- Ensure port 9000 is not blocked by firewall

### Build failures
- Clear gradle cache: `./gradlew clean`
- Re-run: `./gradlew build`

## Environment Variables

When running in Docker Compose, these are set automatically:
- `SPRING_DATASOURCE_URL`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`
- `SPRING_JPA_HIBERNATE_DDL_AUTO`

You can override these by modifying the `docker-compose.yml` file.

## Next Steps

1. Define JPA entities for your music app domain model
2. Create repositories extending `JpaRepository`
3. Implement REST controllers with `@RestController`
4. Configure MinIO client for file uploads/downloads
5. Add authentication/authorization (Spring Security)

## References

- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Spring Data JPA](https://spring.io/projects/spring-data-jpa)
- [PostgreSQL Documentation](https://www.postgresql.org/docs/)
- [MinIO Documentation](https://docs.min.io/)
- [Docker Compose Documentation](https://docs.docker.com/compose/)

