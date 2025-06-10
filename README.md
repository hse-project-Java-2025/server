# "TimeTamer" SmartCalendar Server

Spring Boot backend for the "TimeTamer" SmartCalendar application. Provides REST API for task/event management, user statistics, and OpenAI integration.

---

## Key Features
- **User Management**: Registration, authentication, and profile updates
- **Task & Event Operations**: Full CRUD functionality with status tracking
- **JWT Authentication**: Secure token-based access control
- **OpenAI Integration**: 
  - ChatGPT for natural language processing
  - Whisper for speech-to-text
- **Automated Documentation**: Swagger UI for interactive API exploration
- **CI/CD Pipeline**: GitHub Actions for automated testing and deployment

---

## Tech Stack
- **Language**: Java 21
- **Framework**: Spring Boot 3.1+
- **Database**: 
  - PostgreSQL (Production)
  - H2 (Development/Testing)
- **Build Tool**: Gradle 8+

---

## Prerequisites
- Java 21 JDK
- Gradle 8+
- PostgreSQL 15+ (for production)
- OpenAI API key

---

## Quick Start (Development)
1. Clone repository:
   ```bash
   git clone https://github.com/hse-project-Java-2025/server.git
   cd smartcalendar-server
   ```
2. Set environment variables (create `.env` file):
   ```ini
   JWT_SECRET=your_strong_secret_here
   CHATGPT_API_KEY=your_openai_api_key
   ```
3. Build and run:
   ```bash
   ./gradlew bootRun
   ```
4. Access resources:
   - **Swagger UI**: `http://localhost:8080/swagger-ui.html` (complete API documentation)
   - H2 Console: `http://localhost:8080/h2-console` (JDBC URL: `jdbc:h2:mem:testdb`)

---

## Configuration
### Essential Environment Variables
| Variable          | Description                         | Example                     |
|-------------------|-------------------------------------|-----------------------------|
| `JWT_SECRET`      | Secret for JWT token signing       | `A$ecretKey!123`            |
| `CHATGPT_API_KEY` | OpenAI API key                     | `sk-...`                    |
| `DB_URL`          | Production DB URL (optional)       | `jdbc:postgresql://db:5432` |

### Production Setup
1. Create `application-prod.properties`:
   ```properties
   spring.datasource.url=jdbc:postgresql://your-db-host:5432/smartcalendar
   spring.datasource.username=dbuser
   spring.datasource.password=dbpassword
   spring.jpa.hibernate.ddl-auto=update
   ```
2. Build executable JAR:
   ```bash
   ./gradlew clean build
   ```
3. Run with production profile:
   ```bash
   java -Dspring.profiles.active=prod -jar build/libs/smartcalendar-*.jar
   ```

---

## API Reference
### Core Endpoints Overview
Below are representative examples of available API endpoints. Complete and always up-to-date definitive API reference is automatically generated at runtime:
```http
http://localhost:8080/swagger-ui.html
```


### User Management
| Endpoint                          | Method | Description                  |
|-----------------------------------|--------|------------------------------|
| `/api/users`                      | GET    | List all users               |
| `/api/users`                      | POST   | Register new user            |
| `/api/users/{id}`                 | GET    | Get user details             |
| `/api/users/{id}/email`           | PUT    | Update user email            |
| `/api/users/{userId}/statistics`  | GET    | Get user statistics          |

### Task Management
| Endpoint                              | Method | Description                  |
|---------------------------------------|--------|------------------------------|
| `/api/users/{userId}/tasks`           | GET    | Get user's tasks             |
| `/api/users/{userId}/tasks`           | POST   | Create new task              |
| `/api/users/tasks/{taskId}/status`    | PATCH  | Update task status           |
| `/api/users/tasks/{taskId}`           | DELETE | Delete task                  |

### Event Management
| Endpoint                              | Method | Description                  |
|---------------------------------------|--------|------------------------------|
| `/api/users/{userId}/events`          | GET    | Get user's events            |
| `/api/users/{userId}/events`          | POST   | Create new event             |
| `/api/users/events/{eventId}`         | PATCH  | Update event                 |
| `/api/users/events/{eventId}`         | DELETE | Delete event                 |

### OpenAI Integration
| Endpoint                      | Method | Description                          |
|-------------------------------|--------|--------------------------------------|
| `/api/chatgpt/ask`            | POST   | Get ChatGPT response                 |
| `/api/chatgpt/generate`       | POST   | Generate calendar events/tasks       |
| `/api/chatgpt/generate/entities` | POST | Generate entities from natural language |

---

## Testing
Run tests with:
```bash
./gradlew test
```
- Uses separate in-memory H2 database
- External services (OpenAI) are mocked
- Test coverage reports: `build/reports/tests`

---

## CI/CD Pipeline
GitHub Actions workflow (`.github/workflows/ci.yml`):
1. Build with JDK 21
2. Run all tests
3. Generate dependency license report
4. Upload test reports as artifacts

---

## License
MIT License - see [LICENSE](LICENSE.txt) file

---

## Contributors
- [Dmitry Rusanov](https://github.com/DimaRus05)
- [Mikhail Minaev](https://github.com/minmise)
