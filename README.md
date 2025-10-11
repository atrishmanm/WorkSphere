# 📋 WorkSphere - Task Management System

<div align="center">

![Java](https://img.shields.io/badge/Java-11+-orange?style=for-the-badge&logo=java)
![MySQL](https://img.shields.io/badge/MySQL-8.0+-blue?style=for-the-badge&logo=mysql)
![Maven](https://img.shields.io/badge/Maven-3.6+-red?style=for-the-badge&logo=apache-maven)
![License](https://img.shields.io/badge/License-MIT-green?style=for-the-badge)

**A powerful JDBC MySQL-based task management system with Trello-like functionality**

*Organize your tasks • Track progress • Boost productivity*

[Features](#-features) • [Installation](#-installation) • [Usage](#-usage) • [Contributing](#-contributing)

</div>

---

## 🎯 Overview

WorkSphere is a comprehensive task management application built with Java and MySQL that provides Trello-like functionality through an intuitive command-line interface. Users can create, organize, and track tasks with priorities, due dates, assignments, and status updates.

### 🌟 Why WorkSphere?

- **Simple yet powerful** - Clean CLI interface with rich functionality
- **Multi-user support** - Built-in user management and authentication
- **Database-driven** - Reliable MySQL backend with proper schema design
- **Production-ready** - Comprehensive error handling and data validation
- **Extensible** - Clean architecture ready for web UI integration

## ✨ Features

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

## 💻 Technology Stack

- **Java 11** - Core programming language
- **MySQL 8.0+** - Database for data persistence
- **JDBC** - Database connectivity
- **Maven** - Build and dependency management
- **JUnit 5** - Unit testing framework

## 📁 Project Structure

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

## 🗄️ Database Schema

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

## 🚀 Installation

### Prerequisites

Before you begin, ensure you have the following installed:

- **Java 11 or higher** - [Download here](https://adoptium.net/)
- **MySQL 8.0 or higher** - [Download here](https://dev.mysql.com/downloads/mysql/)
- **Maven 3.6 or higher** - [Download here](https://maven.apache.org/download.cgi)

### Step 1: Clone the Repository

```bash
git clone https://github.com/atrishmanm/WorkSphere.git
cd WorkSphere
```

### Step 2: Database Setup

1. **Start MySQL service**
   ```bash
   # On Windows
   net start mysql
   
   # On macOS/Linux
   sudo systemctl start mysql
   ```

2. **Create the database and tables**
   ```bash
   mysql -u root -p < src/main/resources/schema.sql
   ```
   
   Or manually run the SQL commands:
   ```sql
   CREATE DATABASE worksphere_db;
   USE worksphere_db;
   -- Run the contents of schema.sql
   ```

3. **Configure database connection**
   
   Edit `src/main/resources/application.properties`:
   ```properties
   # Database Configuration
   db.url=jdbc:mysql://localhost:3306/worksphere_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
   db.username=root
   db.password=your_mysql_password_here
   ```

### Step 3: Build and Run

1. **Build the project**
   ```bash
   mvn clean compile
   ```

2. **Run the application**
   ```bash
   mvn exec:java
   ```
   
   **Alternative methods:**
   ```bash
   # Build JAR and run
   mvn clean package
   java -jar target/worksphere-1.0.0.jar
   
   # Or use the convenience scripts
   # Windows
   run.bat
   
   # PowerShell
   .\run.ps1
   ```

### Step 4: First Run Setup

1. **Database connection test** - The app will automatically test connectivity
2. **User creation** - Create your first user account
3. **Sample data** - The database includes demo users and tasks to get started

> **💡 Tip:** Use username `admin` to access pre-loaded sample data

## 📖 Usage Guide

### Getting Started

1. **Launch the application** using any of the methods above
2. **Login or create account** when prompted
3. **Navigate the menu** using number selections

### 🔐 User Authentication

- **Login**: Enter an existing username
- **Create User**: Provide username, email, and full name
- **Switch User**: Logout and login as a different user

### 📝 Managing Tasks

#### Creating a Task
```
Main Menu → Create new task
```
1. Enter task title (required)
2. Enter description (optional)
3. Select priority (Low/Medium/High/Urgent)
4. Set due date (optional, format: YYYY-MM-DD)
5. Assign to a user (optional)

#### Viewing Tasks
- **All Tasks**: Complete task list with status and priority
- **By Status**: Filter by To-Do, In Progress, or Completed
- **My Tasks**: Tasks assigned to current user
- **By Priority**: Filter by priority level
- **Overdue**: Past-due incomplete tasks with alerts

#### Updating Tasks
- **Edit Task**: Modify title, description, priority, due date
- **Update Status**: Change between To-Do, In Progress, Completed
- **Assign Task**: Assign/reassign task to users
- **Delete Task**: Remove task (with confirmation)

### 🔄 Task Status Workflow
```
📝 To-Do → 🔄 In Progress → ✅ Completed
```

### 🎯 Priority Levels
- 🟢 **Low**: Nice to have
- 🟡 **Medium**: Normal priority (default)
- 🟠 **High**: Important
- 🔴 **Urgent**: Critical/time-sensitive

### 👥 Sample Data

The database includes demo accounts for testing:

**Users:**
- `admin` - System Administrator
- `john_doe` - John Doe  
- `jane_smith` - Jane Smith
- `mike_wilson` - Mike Wilson

**Tasks:**
- Various tasks with different priorities and statuses
- Some with due dates and assignments for testing filters

## 🧪 Running Tests

```bash
# Run all tests
mvn test

# Run tests with coverage report
mvn test jacoco:report

# Clean build with tests
mvn clean test
```

## 🏗️ Architecture

The application follows a clean architecture pattern:

- **CLI Layer**: User interface and input handling
- **Service Layer**: Business logic and validation
- **DAO Layer**: Data access and SQL operations
- **Model Layer**: Entity classes and enums
- **Util Layer**: Database connection and utilities

## ⚠️ Error Handling

The application includes comprehensive error handling:
- **Database connection failures** with clear error messages
- **Input validation** with user-friendly prompts
- **Transaction rollback** on operation failures
- **Graceful degradation** when services are unavailable

## 🚧 Troubleshooting

### Common Issues

**Database Connection Failed**
```bash
# Check MySQL service status
# Windows
net start mysql

# macOS/Linux  
sudo systemctl status mysql
```

**Build Failures**
```bash
# Clean and rebuild
mvn clean compile

# Check Java version
java -version  # Should be 11+
```

**Permission Errors**
```bash
# Ensure proper MySQL permissions
GRANT ALL PRIVILEGES ON worksphere_db.* TO 'root'@'localhost';
FLUSH PRIVILEGES;
```

## 🔮 Future Enhancements

Planned features for future releases:

- [ ] **Web UI** - Spring Boot web interface
- [ ] **REST API** - RESTful endpoints for integration
- [ ] **Task Comments** - Add comments and attachments
- [ ] **Categories/Projects** - Organize tasks into projects
- [ ] **Email Notifications** - Alerts for due dates
- [ ] **Time Tracking** - Track time spent on tasks
- [ ] **User Roles** - Admin, Manager, User permissions
- [ ] **Search & Filter** - Advanced search functionality
- [ ] **Export Features** - CSV/Excel export
- [ ] **Dashboard Analytics** - Visual charts and reports

## 🤝 Contributing

We welcome contributions! Here's how to get started:

### 1. Fork & Clone
```bash
# Fork the repository on GitHub
git clone https://github.com/yourusername/WorkSphere.git
cd WorkSphere
```

### 2. Create Feature Branch
```bash
git checkout -b feature/your-feature-name
```

### 3. Make Changes
- Follow existing code style and patterns
- Add tests for new functionality
- Update documentation as needed

### 4. Test Your Changes
```bash
mvn clean test
# Ensure all tests pass
```

### 5. Submit Pull Request
- Push to your fork
- Create pull request with clear description
- Reference any related issues

### Development Guidelines
- **Code Style**: Follow Java conventions
- **Testing**: Maintain test coverage above 80%
- **Documentation**: Update README for new features
- **Commits**: Use clear, descriptive commit messages

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🆘 Support

### Getting Help

**Documentation**: Check this README and inline code comments

**Issues**: [Create an issue](https://github.com/atrishmanm/WorkSphere/issues) for:
- Bug reports
- Feature requests  
- Documentation improvements

**Quick Checklist for Issues:**
1. ✅ Database connection configured correctly
2. ✅ MySQL service running
3. ✅ Java 11+ installed
4. ✅ Maven 3.6+ installed
5. ✅ Check application logs for detailed errors

### Contact

- **GitHub**: [@atrishmanm](https://github.com/atrishmanm)
- **Repository**: [WorkSphere](https://github.com/atrishmanm/WorkSphere)

---

<div align="center">

**Built with ❤️ by [Atrishman Mukherjee](https://github.com/atrishmanm)**

⭐ **Star this repository if you find it helpful!** ⭐

*Happy Task Managing!* 📋✅

</div>