# Task Management System

A comprehensive Task Management System built with Spring Boot, providing role-based access control for Admin, HR, Manager, and Employee users. This system includes features for task assignment, tracking, employee management, and notifications.

## Table of Contents

- [Features](#features)
- [Technologies Used](#technologies-used)
- [Architecture](#architecture)
- [Database Schema](#database-schema)
- [API Endpoints](#api-endpoints)
  - [Authentication](#authentication)
  - [Employee](#employee)
  - [Admin](#admin)
  - [HR](#hr)
  - [Manager](#manager)
- [Security](#security)
- [Setup and Installation](#setup-and-installation)
- [Configuration](#configuration)
- [Usage](#usage)
- [Project Structure](#project-structure)
- [Contributing](#contributing)
- [License](#license)

## Features

- **User Authentication & Authorization**: JWT-based authentication with role-based access control
- **Role-based Access**: Different functionalities for Admin, HR, Manager, and Employee roles
- **Employee Management**: Registration, approval workflow, profile management
- **Task Management**: Create, assign, update, and track tasks with due dates and attachments
- **Notification System**: Real-time notifications for task assignments and updates
- **Password Management**: Secure password reset with OTP verification
- **File Upload**: Attachment support for tasks
- **Performance Tracking**: Employee performance metrics
- **Email Integration**: Automated email notifications

## Technologies Used

- **Backend**: Spring Boot 3.5.0
- **Java**: Java 17
- **Database**: PostgreSQL
- **Security**: Spring Security with JWT
- **Build Tool**: Maven
- **Email**: Spring Mail (Gmail SMTP)
- **ORM**: Spring Data JPA
- **Validation**: Spring Validation
- **Logging**: Spring Boot Logging (Logback)

## Architecture

The application follows a layered architecture pattern:

1. **Controller Layer**: REST API endpoints (`/api/*`)
2. **Service Layer**: Business logic implementation
3. **Repository Layer**: Data access layer using Spring Data JDBC/JPA
4. **Entity Layer**: Domain models representing database tables
5. **Security Layer**: JWT authentication and authorization
6. **Utility Layer**: Helper classes for JWT, email, etc.

## Database Schema

### Main Entities

1. **Employee**: User entity with roles (ADMIN, HR, MANAGER, USER)
2. **Task**: Task entity with status tracking (PENDING, IN_PROGRESS, COMPLETED)
3. **Notification**: Notification system for user updates
4. **Team**: Team management (if applicable)

### Key Relationships

- Employee 1:N Task (One employee can have multiple tasks)
- Employee 1:N Notification (One employee can have multiple notifications)

## API Endpoints

### Authentication

- `POST /api/auth/login` - User login with email and password

### Employee

- `POST /api/employee/registration` - Employee registration
- `POST /api/employee/verify-email` - Email verification with OTP
- `GET /api/employee/notifications` - Get user notifications
- `PATCH /api/employee/updateInfo` - Update employee information
- `GET /api/employee/details` - Get employee details
- `DELETE /api/employee/deleteEmployee` - Delete employee account
- `POST /api/employee/changePassword` - Change password
- `POST /api/employee/forgotPassword` - Forgot password request
- `POST /api/employee/verifyOtpAndResetPassword` - Verify OTP and reset password
- `POST /api/employee/resetOtp` - Reset OTP
- `GET /api/employee/assignedTasks` - Get tasks assigned to employee
- `PATCH /api/employee/updateTaskStatus` - Update task status
- `GET /api/employee/searchTasks` - Search tasks by keyword
- `GET /api/employee/filterTasksByStatus` - Filter tasks by status
- `POST /api/employee/uploadAttachment` - Upload attachment for task

### Admin

- `POST /api/admin/createTask` - Create new task
- `POST /api/admin/assignEmployeeToTask` - Assign employee to task
- `PUT /api/admin/updateTask` - Update task details
- `GET /api/admin/all-tasks` - Get all tasks
- `GET /api/admin/all-employees` - Get all employees
- `DELETE /api/admin/deleteTask/{taskId}` - Delete task
- `DELETE /api/admin/terminateEmployee/{employeeId}` - Terminate employee
- `PUT /api/admin/approveEmployee/{employeeId}` - Approve employee registration
- `GET /api/admin/getUnapprovedEmployees` - Get unapproved employee registrations
- `GET /api/admin/employee/{employeeId}/performance` - Get employee performance metrics

### HR

- `GET /api/hr/dashboard` - HR dashboard statistics
- `GET /api/hr/employees` - Get all employees
- `GET /api/hr/tasks` - Get all tasks
- `GET /api/hr/employee/{employeeId}` - Get specific employee details
- `PUT /api/hr/updateEmployee/{employeeId}` - Update employee information
- `GET /api/hr/task/{taskId}` - Get specific task details
- `GET /api/hr/teamPerformance` - Get team performance metrics

### Manager

- `GET /api/manager/dashboard` - Manager dashboard
- `GET /api/manager/team` - Get manager's team members
- `GET /api/manager/teamTasks` - Get team tasks
- `POST /api/manager/createTeamTask` - Create task for team member
- `PUT /api/manager/updateTeamTask/{taskId}` - Update team task
- `DELETE /api/manager/deleteTeamTask/{taskId}` - Delete team task
- `GET /api/manager/teamMember/{employeeId}` - Get team member details
- `GET /api/manager/teamPerformance` - Get team performance metrics

## Security

### Authentication Flow

1. User sends credentials to `/api/auth/login`
2. Server validates credentials and generates JWT token
3. Client stores token and includes in `Authorization: Bearer <token>` header for subsequent requests
4. JWT filter validates token on each request and sets security context

### Authorization

- JWT tokens contain user role information
- Role-based access control using Spring Security
- Endpoints protected based on user roles:
  - ADMIN: Full access to all endpoints
  - HR: HR-specific endpoints
  - MANAGER: Team management endpoints
  - USER: Employee-specific endpoints

### JWT Token Structure

Tokens contain the following claims:
- `id`: Employee ID
- `name`: Employee name
- `email`: Employee email
- `role`: User role
- `job_title`: Employee job title

## Setup and Installation

### Prerequisites

- Java 17 or higher
- Maven 3.6+
- PostgreSQL database
- Gmail account for email notifications (optional)

### Installation Steps

1. Clone the repository:
   ```bash
   git clone <repository-url>
   cd Project
   ```

2. Configure database settings in `src/main/resources/application.properties`

3. Build the project:
   ```bash
   ./mvnw clean install
   ```

4. Run the application:
   ```bash
   ./mvnw spring-boot:run
   ```

   Or run the JAR file:
   ```bash
   java -jar target/Project-0.0.1-SNAPSHOT.jar
   ```

## Configuration

### Database Configuration

Update the following properties in `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/task-management-app
spring.datasource.username=postgres
spring.datasource.password=root
```

### Email Configuration

For email notifications, update these properties:

```properties
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=tracklyn.team@gmail.com
spring.mail.password=auuh eqqh russ zpss
```

### File Upload Configuration

```properties
file.upload-dir=C:/Users/Shahriar Zaman/Desktop/IntelliJ IDEA Codes/Main_Project/project/Project/uploads
```

### Logging Configuration

```properties
logging.level.root=INFO
logging.level.org.springframework.web=DEBUG
logging.level.com.example=DEBUG
logging.file.name=application.log
logging.file.path=logs
```

## Usage

### Starting the Application

1. Ensure PostgreSQL is running
2. Run the application using one of the methods described in Installation
3. Access the API at `http://localhost:8080`

### User Registration and Login Flow

1. Employee registers via `/api/employee/registration`
2. Employee receives email with OTP for verification
3. Employee verifies email via `/api/employee/verify-email`
4. Admin approves employee registration via `/api/admin/approveEmployee/{employeeId}`
5. Employee logs in via `/api/auth/login` to receive JWT token
6. Employee uses token for authenticated requests

### Task Management Flow

1. Admin creates tasks via `/api/admin/createTask`
2. Admin assigns tasks to employees via `/api/admin/assignEmployeeToTask`
3. Employee receives notification about assigned task
4. Employee updates task status as work progresses
5. Admin/Manager can track task progress and completion

## Project Structure

```
src/
├── main/
│   ├── java/com/example/Project/
│   │   ├── ProjectApplication.java          # Main application class
│   │   ├── api/                              # REST controllers
│   │   ├── aspect/                           # Logging aspect
│   │   ├── config/                           # Security configuration
│   │   ├── entity/                           # Domain entities
│   │   ├── enums/                            # Enumerations
│   │   ├── filter/                           # JWT filter
│   │   ├── repository/                       # Data repositories
│   │   ├── service/                         # Business logic services
│   │   └── util/                             # Utility classes
│   └── resources/
│       ├── application.properties            # Configuration
│       └── logback.xml                       # Logging configuration
└── test/                                     # Test files
```

## API Documentation

### Authentication

```bash
# Login
POST /api/auth/login
{
  "email": "user@example.com",
  "password": "password123"
}

# Response
{
  "token": "eyJhbGciOiJIUzUx...",
  "role": "USER",
  "name": "John Doe"
}
```

### Employee Endpoints

```bash
# Get assigned tasks
GET /api/employee/assignedTasks
Authorization: Bearer <token>

# Update task status
PATCH /api/employee/updateTaskStatus
Authorization: Bearer <token>
{
  "taskId": 1,
  "status": "IN_PROGRESS"
}
```

### Admin Endpoints

```bash
# Create new task
POST /api/admin/createTask
Authorization: Bearer <token>
{
  "title": "New Task",
  "description": "Task description",
  "projectType": "Development",
  "dueDate": "2023-12-31T23:59:59"
}

# Assign employee to task
POST /api/admin/assignEmployeeToTask?employeeId=1&taskId=1
Authorization: Bearer <token>
```
