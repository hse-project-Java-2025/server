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

## Quick Start
1. Clone repository:
   ```bash
   git clone https://github.com/hse-project-Java-2025/server.git
   cd smartcalendar-server
   ```
2. Set environment variables (create `.env` file):
   ```ini
   JWT_SECRET=your_strong_secret_here
   CHATGPT_API_KEY=your_openai_api_key
   MAIL_PASSWORD=your_smtp_app_password
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
| `JWT_SECRET`      | Secret for JWT token signing        | `A$ecretKey!123`            |
| `CHATGPT_API_KEY` | OpenAI API key                      | `sk-...`                    |
| `MAIL_PASSWORD`   | SMTP app password for email sending | `your_app_password`         |
| `DB_URL`          | Production DB URL (optional)        | `jdbc:postgresql://db:5432` |


### SMTP Email Notification Setup

To enable email notifications (for collaborative events, invites, etc.), configure the following SMTP settings in your `application.properties`:

| Property                                      | Example Value                | Description                                                                                 |
|------------------------------------------------|------------------------------|---------------------------------------------------------------------------------------------|
| `spring.mail.host`                             | `smtp.gmail.com`             | SMTP server host (Gmail example)                                                            |
| `spring.mail.port`                             | `587`                        | SMTP server port (587 for TLS)                                                              |
| `spring.mail.username`                         | `your_email@gmail.com`       | Email account used to send notifications                                                    |
| `spring.mail.password`                         | `${MAIL_PASSWORD}`           | App password for the email account (set as environment variable)                            |
| `spring.mail.properties.mail.smtp.auth`        | `true`                       | Enable SMTP authentication                                                                  |
| `spring.mail.properties.mail.smtp.starttls.enable` | `true`                   | Enable STARTTLS encryption                                                                  |
| `spring.mail.from`                             | `noreply@ttsc.com`           | Sender address shown in emails (must match or be an alias for Gmail accounts)               |

**Important notes:**
- For Gmail, you must use an [App Password](https://support.google.com/accounts/answer/185833?hl=en) and have two-factor authentication enabled.
- The value of `spring.mail.from` will only be used if your SMTP provider allows it. Gmail requires this to match your authenticated account or a verified alias.
- For other SMTP providers, adjust the host, port, and credentials accordingly.

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

### Event Management (including Collaborative Events)
| Endpoint                                      | Method | Description                                      |
|-----------------------------------------------|--------|--------------------------------------------------|
| `/api/users/{userId}/events`                  | GET    | Get user's events (including shared/collaborative)|
| `/api/users/{userId}/events`                  | POST   | Create new event                                 |
| `/api/users/events/{eventId}`                 | PATCH  | Update event                                     |
| `/api/users/events/{eventId}`                 | DELETE | Delete event                                     |
| `/api/users/events/{eventId}/invite`          | POST   | Invite user to event (collaboration)             |
| `/api/users/events/{eventId}/accept-invite`   | POST   | Accept event invitation                          |
| `/api/users/events/{eventId}/remove-invite`   | POST   | Remove invitation for user                       |
| `/api/users/events/{eventId}/remove-participant` | POST | Remove participant from event                    |
| `/api/users/me/invites`                       | GET    | Get events you are invited to                    |

### OpenAI Integration
| Endpoint                      | Method | Description                          |
|-------------------------------|--------|--------------------------------------|
| `/api/chatgpt/ask`            | POST   | Get ChatGPT response                 |
| `/api/chatgpt/generate`       | POST   | Generate calendar events/tasks       |
| `/api/chatgpt/generate/entities` | POST | Generate entities from natural language |

---

## Collaborative Events

The SmartCalendar supports full collaboration on events:
- **Invite users** to your events by username or email.
- **Accept or decline invitations** to shared events.
- **Remove participants** or invitations at any time.
- **Automatic email notifications** are sent for all key actions (invitation, joining, removal, updates, deletion) with detailed event info.
- **All collaborative features are available via REST API** (see Event Management section above).

---

## Testing
Run tests with:
```bash
./gradlew test
```
- Uses separate in-memory H2 database
- External services (OpenAI) are mocked
- Test coverage reports: `build/reports/tests`

### Postman Collection

You can also test all endpoints and collaborative event scenarios using our [Postman collection](https://warped-spaceship-772679.postman.co/workspace/Team-Workspace~558e4b04-2021-4e54-894c-0ad8890eda3d/collection/43149440-fdb46307-d6af-4895-bd4b-5b871c1f6962?action=share&creator=43149440&active-environment=43149440-f5aa59ad-f5b0-484f-923a-2d9403843293)

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
