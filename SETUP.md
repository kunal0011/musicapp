# Music App - Full Setup Guide

## Architecture Overview

```
musicapp/
├── backend/          # Spring Boot REST API
├── android/          # Android mobile app
└── docker-compose.yml # Orchestrates all services
```

## Quick Start

### Prerequisites
- Docker & Docker Compose installed
- Java 17+ installed (for local development)
- Git

### Step 1: Start All Services

```bash
cd /Users/kunalkumar/Desktop/musicapp
docker-compose up -d
```

This will start:
- **PostgreSQL** (Port 5432)
- **MinIO** (Ports 9000, 9001)
- **Spring Boot API** (Port 8080)

### Step 2: Verify Services are Running

```bash
docker-compose ps
```

Expected output:
```
NAME                COMMAND                  SERVICE
musicapp_db         postgres:15-alpine       db
musicapp_storage    minio server /data       minio
musicapp_api        java -jar app.jar        api
```

### Step 3: Access Services

| Service | URL | Credentials |
|---------|-----|-------------|
| PostgreSQL | localhost:5432 | musicapp_user / musicapp_password |
| MinIO API | http://localhost:9000 | minioadmin / minioadmin |
| MinIO Console | http://localhost:9001 | minioadmin / minioadmin |
| REST API | http://localhost:8080 | N/A |

## Service Details

### PostgreSQL (`db`)
- **Image**: postgres:15-alpine
- **Container**: musicapp_db
- **Port**: 5432
- **Volume**: postgres_data (persistent)
- **Database**: musicapp
- **User**: musicapp_user
- **Password**: musicapp_password

```yaml
# Connection string for external tools
postgresql://musicapp_user:musicapp_password@localhost:5432/musicapp
```

### MinIO (`minio`)
- **Image**: minio/minio
- **Container**: musicapp_storage
- **API Port**: 9000
- **Console Port**: 9001
- **Volume**: minio_data (persistent)
- **Access Key**: minioadmin
- **Secret Key**: minioadmin

```
# S3-compatible endpoint
http://localhost:9000
```

### Spring Boot API (`api`)
- **Build Context**: ./backend
- **Port**: 8080
- **Depends On**: db, minio
- **Active Profile**: docker (when running in container)

## Local Development (Without Docker)

### Build Backend Only

```bash
cd backend
./gradlew clean build
```

### Run Backend Locally

For this, you need PostgreSQL running locally:

```bash
# Install PostgreSQL locally or use docker
docker run -d \
  --name postgres-local \
  -e POSTGRES_USER=musicapp_user \
  -e POSTGRES_PASSWORD=musicapp_password \
  -e POSTGRES_DB=musicapp \
  -p 5432:5432 \
  postgres:15-alpine

# Then run the app
cd backend
./gradlew bootRun
```

### Run Tests

```bash
cd backend
./gradlew test
```

Tests use H2 in-memory database - no external services needed.

## Docker Compose Commands

### View Running Containers
```bash
docker-compose ps
```

### View Logs
```bash
# All services
docker-compose logs -f

# Specific service
docker-compose logs -f api
docker-compose logs -f db
docker-compose logs -f minio
```

### Stop Services
```bash
# Stop but keep volumes
docker-compose stop

# Stop and remove containers
docker-compose down

# Stop, remove containers, and delete volumes (CAREFUL!)
docker-compose down -v
```

### Restart Services
```bash
docker-compose restart
```

### Rebuild Services
```bash
docker-compose build --no-cache
docker-compose up -d
```

## Database Management

### Connect to PostgreSQL

Using `psql`:
```bash
psql -h localhost -U musicapp_user -d musicapp -p 5432
```

Using Docker:
```bash
docker-compose exec db psql -U musicapp_user -d musicapp
```

### Useful SQL Commands

```sql
-- List all tables
\dt

-- View table schema
\d table_name

-- Create a new table
CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    username VARCHAR(255) UNIQUE NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL
);
```

## MinIO Management

### Create a Bucket

Using MinIO Console:
1. Open http://localhost:9001
2. Login with minioadmin / minioadmin
3. Click "Create Bucket"
4. Enter bucket name (e.g., `musicapp`)

Using MinIO Client CLI:
```bash
# Install mc (MinIO Client)
curl https://dl.min.io/client/mc/release/macos/mc -o mc
chmod +x mc

# Add MinIO server
./mc alias set minio http://localhost:9000 minioadmin minioadmin

# Create bucket
./mc mb minio/musicapp
```

### Upload Files

```bash
./mc cp music.mp3 minio/musicapp/
```

### List Files

```bash
./mc ls minio/musicapp/
```

## Environment Variables

### For Docker Compose
The `docker-compose.yml` defines all required environment variables automatically.

### For Local Development
Create `.env` file in root directory:

```env
POSTGRES_USER=musicapp_user
POSTGRES_PASSWORD=musicapp_password
POSTGRES_DB=musicapp
MINIO_ROOT_USER=minioadmin
MINIO_ROOT_PASSWORD=minioadmin
```

Then reference in docker-compose:
```yaml
environment:
  POSTGRES_USER: ${POSTGRES_USER}
  POSTGRES_PASSWORD: ${POSTGRES_PASSWORD}
```

## Troubleshooting

### Port Already in Use

If ports 5432, 9000, 9001, or 8080 are already in use:

```bash
# Find what's using the port
lsof -i :5432

# Either stop that service or change ports in docker-compose.yml
```

### Database Connection Refused

```bash
# Check if postgres service is running
docker-compose ps db

# Restart the database
docker-compose restart db

# Wait 10 seconds for database to start
sleep 10

# Check logs
docker-compose logs db
```

### MinIO Not Responding

```bash
# Restart MinIO
docker-compose restart minio

# Check logs
docker-compose logs minio
```

### Docker Daemon Not Running

On macOS:
```bash
# Start Docker Desktop from Applications folder
# Or from terminal:
open /Applications/Docker.app
```

### Permission Denied Errors

```bash
# Add current user to docker group
sudo usermod -aG docker $USER

# Apply new permissions
newgrp docker

# Verify
docker ps
```

## Development Workflow

1. **Start Services**: `docker-compose up -d`
2. **Develop**: Edit code in `backend/src`
3. **Build**: `cd backend && ./gradlew build`
4. **Test**: `./gradlew test`
5. **Run**: `docker-compose restart api` or `./gradlew bootRun`
6. **Commit**: Push changes to git

## CI/CD Considerations

For production deployment:
1. Build images without `docker-compose`
2. Use environment variables for secrets
3. Implement database migrations (Flyway/Liquibase)
4. Add health checks to docker-compose
5. Use persistent volumes with backups

## Additional Resources

- [Docker Compose Documentation](https://docs.docker.com/compose/)
- [PostgreSQL Documentation](https://www.postgresql.org/docs/)
- [MinIO Documentation](https://docs.min.io/)
- [Spring Boot Guide](https://spring.io/guides/gs/spring-boot/)

## Support

For issues or questions:
1. Check the logs: `docker-compose logs`
2. Verify services are running: `docker-compose ps`
3. Try restarting: `docker-compose restart`
4. Reset (CAREFUL!): `docker-compose down -v && docker-compose up -d`

