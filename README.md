# Task Manager

A JDBC MySQL-based task management system with functionality similar to Trello. Users can manage tasks with priorities, due dates, assignments, and track their progress through different statuses.

## Features

### Task Management
- ✅ **Create tasks** with title, description, priority, and due date
- ✅ **Edit tasks** - update title, description, priority, due date
- ✅ **Delete tasks** - remove tasks from the system
- ✅ **Assign tasks** to users
- ✅ **Task status tracking** - To-Do, In Progress, Completed
- ✅ **Priority levels** - Low, Medium, High, Urgent
- ✅ **Due date management** with overdue detection

### Task Views
- ✅ **View all tasks** in a formatted table
- ✅ **Filter by status** (To-Do, In Progress, Completed)
- ✅ **Filter by priority** (Low, Medium, High, Urgent)
- ✅ **View assigned tasks** for a specific user
- ✅ **View overdue tasks** with alerts
- ✅ **Detailed task view** with complete information

### User Management
- ✅ **Create users** with username, email, and full name
- ✅ **User authentication** via username login
- ✅ **List all users** in the system
- ✅ **Input validation** for user data

### Dashboard & Statistics
- ✅ **Task statistics** - counts by status and overdue tasks
- ✅ **Visual indicators** with emojis for status and priority
- ✅ **User-friendly CLI interface** with menu navigation

## Technology Stack

- **Java 11** - Core programming language
- **MySQL 8.0+** - Database for data persistence
- **JDBC** - Database connectivity
- **Maven** - Build and dependency management
- **JUnit 5** - Unit testing framework

## Project Structure

```
WorkSphere/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── worksphere/
│   │   │           ├── WorkSphereApp.java            # Main application
│   │   │           ├── cli/
│   │   │           │   └── WorkSphereCLI.java        # Command-line interface
│   │   │           ├── dao/
│   │   │           │   ├── TaskDAO.java              # Task data access
│   │   │           │   └── UserDAO.java              # User data access
│   │   │           ├── model/
│   │   │           │   ├── Priority.java             # Priority enum
│   │   │           │   ├── Task.java                 # Task entity
│   │   │           │   ├── TaskStatus.java           # Status enum
│   │   │           │   └── User.java                 # User entity
│   │   │           ├── service/
│   │   │           │   ├── TaskService.java          # Task business logic
│   │   │           │   └── UserService.java          # User business logic
│   │   │           └── util/
│   │   │               └── DatabaseConnection.java   # DB connection utility
│   │   └── resources/
│   │       ├── application.properties                # Database configuration
│   │       └── schema.sql                            # Database schema
│   └── test/
│       └── java/
│           └── com/
│               └── worksphere/                        # Test classes
├── pom.xml                                           # Maven configuration
└── README.md                                         # This file
```

## Database Schema

### Users Table
- `id` (INT, AUTO_INCREMENT, PRIMARY KEY)
- `username` (VARCHAR(50), UNIQUE, NOT NULL)
- `email` (VARCHAR(100), UNIQUE, NOT NULL)
- `full_name` (VARCHAR(100), NOT NULL)
- `created_at` (TIMESTAMP, DEFAULT CURRENT_TIMESTAMP)
- `updated_at` (TIMESTAMP, DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP)

### Tasks Table
- `id` (INT, AUTO_INCREMENT, PRIMARY KEY)
- `title` (VARCHAR(200), NOT NULL)
- `description` (TEXT)
- `priority` (ENUM: 'LOW', 'MEDIUM', 'HIGH', 'URGENT', DEFAULT 'MEDIUM')
- `status` (ENUM: 'TODO', 'IN_PROGRESS', 'COMPLETED', DEFAULT 'TODO')
- `due_date` (DATE, nullable)
- `assigned_to` (INT, FOREIGN KEY to users.id, nullable)
- `created_by` (INT, FOREIGN KEY to users.id, NOT NULL)
- `created_at` (TIMESTAMP, DEFAULT CURRENT_TIMESTAMP)
- `updated_at` (TIMESTAMP, DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP)

## Setup Instructions

### Prerequisites
- Java 11 or higher
- MySQL 8.0 or higher
- Maven 3.6 or higher

### 1. Database Setup

1. **Install MySQL** and start the MySQL service

2. **Create the database and tables**:
   ```sql
   -- Run the SQL script in src/main/resources/schema.sql
   mysql -u root -p < src/main/resources/schema.sql
   ```

3. **Update database configuration**:
   Edit `src/main/resources/application.properties`:
   ```properties
   # Update these with your MySQL settings
   db.url=jdbc:mysql://localhost:3306/worksphere_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
   db.username=root
   db.password=your_mysql_password
   ```

### 2. Application Setup

1. **Clone/Download** the project to your local machine

2. **Navigate to project directory**:
   ```bash
   cd WorkSphere
   ```

3. **Build the project**:
   ```bash
   mvn clean compile
   ```

4. **Run the application**:
   ```bash
   mvn exec:java
   ```

   Or alternatively:
   ```bash
   mvn clean package
   java -jar target/task-manager-1.0.0.jar
   ```

### 3. First Run

1. The application will test the database connection on startup
2. If successful, you'll be prompted to login or create a new user
3. Sample data is already included in the database (4 users and 6 sample tasks)

## Usage Guide

### User Authentication
- **Login**: Enter an existing username
- **Create User**: Provide username, email, and full name
- **Switch User**: Logout and login as a different user

### Managing Tasks

#### Creating a Task
1. Select "Create new task" from the main menu
2. Enter task title (required)
3. Enter description (optional)
4. Select priority (Low/Medium/High/Urgent)
5. Set due date (optional, format: YYYY-MM-DD)
6. Assign to a user (optional)

#### Viewing Tasks
- **All Tasks**: Shows complete task list
- **By Status**: Filter by To-Do, In Progress, or Completed
- **My Tasks**: Shows tasks assigned to current user
- **By Priority**: Filter by priority level
- **Overdue**: Shows past-due incomplete tasks

#### Updating Tasks
- **Edit Task**: Modify title, description, priority, due date
- **Update Status**: Change between To-Do, In Progress, Completed
- **Assign Task**: Assign/reassign task to users
- **Delete Task**: Remove task (with confirmation)

### Task Status Workflow
```
📝 To-Do → 🔄 In Progress → ✅ Completed
```

### Priority Levels
- 🟢 **Low**: Nice to have
- 🟡 **Medium**: Normal priority (default)
- 🟠 **High**: Important
- 🔴 **Urgent**: Critical/time-sensitive

## Sample Data

The database includes sample users and tasks:

**Users:**
- admin (System Administrator)
- john_doe (John Doe)
- jane_smith (Jane Smith)
- mike_wilson (Mike Wilson)

**Tasks:**
- Various tasks with different priorities and statuses
- Some with due dates and assignments

## Running Tests

```bash
# Run all tests
mvn test

# Run tests with coverage
mvn test jacoco:report
```

## API/Service Layer

The application follows a clean architecture pattern:

- **CLI Layer**: User interface and input handling
- **Service Layer**: Business logic and validation
- **DAO Layer**: Data access and SQL operations
- **Model Layer**: Entity classes and enums
- **Util Layer**: Database connection and utilities

## Error Handling

The application includes comprehensive error handling:
- Database connection failures
- Invalid input validation
- User-friendly error messages
- Transaction rollback on failures

## Future Enhancements

Potential improvements for future versions:
- [ ] Web-based UI using Spring Boot
- [ ] Task comments and attachments
- [ ] Task categories/projects
- [ ] Email notifications for due dates
- [ ] Task time tracking
- [ ] User roles and permissions
- [ ] REST API endpoints
- [ ] Task search functionality
- [ ] Export tasks to CSV/Excel

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests for new functionality
5. Submit a pull request

## License

This project is open source and available under the MIT License.

## Support

For issues or questions:
1. Check the database connection configuration
2. Verify MySQL service is running
3. Ensure Java 11+ is installed
4. Check the logs for detailed error messages

---

**Happy Task Managing! 📋✅**