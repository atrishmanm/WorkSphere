
# WorkSphere - Task Management System

<div align="center">

![Java](https://img.shields.io/badge/Java-11+-orange?style=for-the-badge&logo=java)
![SQLite](https://img.shields.io/badge/SQLite-3.44+-blue?style=for-the-badge&logo=sqlite)
![Maven](https://img.shields.io/badge/Maven-3.6+-red?style=for-the-badge&logo=apache-maven)
![License](https://img.shields.io/badge/License-MIT-green?style=for-the-badge)


**A powerful Java & SQLite-based task management system**

*Organize your tasks • Track progress • Boost productivity*

[Features](#-features) • [Installation](#-installation) • [Usage](#-usage) • [Contributing](#-contributing)
[//]: # (Feature Badges)

<div align="center">
   <img src="https://img.shields.io/badge/Leaderboard-Enabled-blueviolet?style=for-the-badge" alt="Leaderboard"/>
   <img src="https://img.shields.io/badge/Analytics-Dashboard-orange?style=for-the-badge" alt="Analytics Dashboard"/>
   <img src="https://img.shields.io/badge/Pomodoro-Timer-red?style=for-the-badge" alt="Pomodoro Timer"/>
   <img src="https://img.shields.io/badge/Recurring-Tasks-teal?style=for-the-badge" alt="Recurring Tasks"/>
   <img src="https://img.shields.io/badge/CSV-Import/Export-yellow?style=for-the-badge" alt="CSV Import/Export"/>
   <img src="https://img.shields.io/badge/Modern-UI/UX-ff69b4?style=for-the-badge" alt="Modern UI/UX"/>
   <img src="https://img.shields.io/badge/Notifications-Enabled-9cf?style=for-the-badge" alt="Notifications"/>
   <img src="https://img.shields.io/badge/Calendar-View-4caf50?style=for-the-badge" alt="Calendar View"/>
   <img src="https://img.shields.io/badge/Task-Notes/Description-2196f3?style=for-the-badge" alt="Task Notes/Description"/>
   <img src="https://img.shields.io/badge/Subtask-Checkboxes-607d8b?style=for-the-badge" alt="Subtask Checkboxes"/>
   <img src="https://img.shields.io/badge/Predictive-Analytics-ff9800?style=for-the-badge" alt="Predictive Analytics"/>
   <img src="https://img.shields.io/badge/Push-Notifications-00bcd4?style=for-the-badge" alt="Push Notifications"/>
   <img src="https://img.shields.io/badge/Task-History-795548?style=for-the-badge" alt="Task History"/>
</div>
[//]: # (Feature Summary Table)

---

## 🗂️ Feature Summary Table

| Feature                | Description                                                                                 |
|------------------------|---------------------------------------------------------------------------------------------|
| Calendar View          | Visualize tasks/events in a monthly calendar, color-coded deadlines, click to edit/add      |
| Task Notes & Subtasks  | Rich notes, subtask checkboxes, progress bar                                                |
| Predictive Analytics   | Deadline prediction, visual alerts for at-risk tasks                                       |
| Push Notifications     | Desktop notifications for deadlines, assignments, overdue tasks                            |
| Task History           | Audit log of edits, status changes, assignments, completions                               |
| Task Management        | Create, edit, delete, assign, track status, priority, due dates, recurring tasks, subtasks  |
| Task Views & Filtering | Table/Kanban, filter/search by status, priority, category, tags, assignee, due date         |
| User Management        | Create users, authentication, roles, validation                                             |
| Leaderboard            | Top performers, rankings, points, badges, team analytics                                   |
| Analytics Dashboard    | Productivity metrics, charts, insights, export analytics                                   |
| Pomodoro Timer         | Integrated timer, customizable intervals, notifications, cycle tracking                     |
| CSV Import/Export      | Bulk import/export tasks with all details                                                   |
| Modern UI/UX           | FlatLaf theming, gradients, rounded corners, animations, dark/light mode                   |
| Notifications          | Desktop/sound alerts for overdue tasks, assignments, Pomodoro, deadline risks              |
| Dashboard & Statistics | Task statistics, visual indicators, user-friendly CLI                                      |

---

</div>

---

## 🎯 Overview


WorkSphere is a comprehensive task management application built with Java and SQLite that provides an intuitive GUI and command-line interface. Users can create, organize, and track tasks with priorities, due dates, assignments, and status updates.

### 🌟 Why WorkSphere?

- **Simple yet powerful** - Clean CLI interface with rich functionality
- **Multi-user support** - Built-in user management and authentication
- **Database-driven** - Reliable SQLite backend, zero setup required
- **Production-ready** - Comprehensive error handling and data validation
- **Extensible** - Clean architecture ready for web UI integration

## ✨ Features
### Calendar View
- ✅ **Calendar view panel** - visualize all tasks, deadlines, and events in a monthly calendar
- ✅ **Click on a date** to see tasks due, add new tasks, or edit existing ones
- ✅ **Color-coded deadlines** and overdue indicators

### Task Notes & Subtasks
- ✅ **Task notes/description** - add rich notes to each task
- ✅ **Subtask checkboxes** - check off subtasks as completed directly inside the task dialog
- ✅ **Progress bar** for subtasks completion

### Predictive Analytics
- ✅ **Deadline prediction** - "At current pace, you'll miss deadline by X days"
- ✅ **Visual alerts** for at-risk tasks and projects

### Push Notifications
- ✅ **Desktop notifications** for upcoming deadlines, overdue tasks, and assignments
- ✅ **Real-time alerts** for critical events

### Task History
- ✅ **Task history tracking** - see who changed what and when for every task
- ✅ **Detailed audit log** for edits, status changes, assignments, and completions

### Task Management

### Task Management
- ✅ **Create tasks** with title, description, priority, due date, category, tags, and subtasks
- ✅ **Edit tasks** - update all fields, including subtasks and recurrence
- ✅ **Delete tasks** - remove tasks from the system
- ✅ **Assign tasks** to users
- ✅ **Task status tracking** - To-Do, In Progress, Completed
- ✅ **Priority levels** - Low, Medium, High, Urgent
- ✅ **Due date management** with overdue detection
- ✅ **Recurring tasks** - set up tasks that repeat on custom schedules
- ✅ **Subtasks** - break down tasks into actionable items
- ✅ **Task history** - view changes and progress over time

### Task Views & Filtering
- ✅ **View all tasks** in a formatted table or Kanban board
- ✅ **Filter by status** (To-Do, In Progress, Completed)
- ✅ **Filter by priority** (Low, Medium, High, Urgent)
- ✅ **Filter by category, tags, assignee, and due date**
- ✅ **Search tasks** by title, description, or tags
- ✅ **View assigned tasks** for a specific user
- ✅ **View overdue tasks** with alerts and notifications
- ✅ **Detailed task view** with complete information

### User Management
- ✅ **Create users** with username, email, and full name
- ✅ **User authentication** via username login
- ✅ **List all users** in the system
- ✅ **Input validation** for user data
- ✅ **User roles** - admin and regular users with different permissions

### Leaderboard & Gamification
- ✅ **Leaderboard panel** - see top performers by completed tasks, productivity, and streaks
- ✅ **User rankings** - motivate users with points and badges
- ✅ **Team performance analytics**

### Analytics Dashboard
- ✅ **Productivity metrics** - total/completed tasks, completion rate, overdue analysis
- ✅ **Visual charts** - completion trends, priority distribution, team performance, time accuracy
- ✅ **Insights** - deadline risk prediction, overdue alerts, time efficiency
- ✅ **Export analytics** - save charts and metrics

### Pomodoro Timer
- ✅ **Integrated Pomodoro timer** for focused work sessions
- ✅ **Customizable work/break intervals**
- ✅ **Task selection** - link timer to specific tasks
- ✅ **Cycle tracking** and notifications

### CSV Import/Export
- ✅ **Import tasks from CSV** - bulk add tasks with all details
- ✅ **Export tasks to CSV** - backup or share your data

### Modern UI/UX
- ✅ **Beautiful, modern interface** with FlatLaf theming, gradients, rounded corners, and smooth animations
- ✅ **Responsive dialogs, panels, and charts**
- ✅ **Dark/light mode support**

### Notifications
- ✅ **Desktop notifications** for overdue tasks, new assignments, and deadline risks
- ✅ **Sound alerts** for Pomodoro and important events

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
- **SQLite 3.44+** - Embedded database for data persistence
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

No setup required! SQLite is embedded and the database file will be created automatically on first run.

**Optional:** You can inspect or reset the database by deleting the `.db` file in your project directory.


### Step 3: Build and Run


1. **Build the project**
   ```bash
   mvn clean package
   ```

2. **Run the application (GUI)**
   ```bash
   java -jar target/worksphere-1.0.0.jar gui
   ```

   **Or run CLI mode:**
   ```bash
   java -jar target/worksphere-1.0.0.jar cli
   ```

### Step 4: First Run Setup

1. **Database connection test** - The app will automatically test connectivity
2. **User creation** - Create your first user account
3. **Sample data** - The database includes demo users and tasks to get started


> **Tip:** Use username `admin` and password `admin123` to access pre-loaded sample data

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


### Task Status Workflow
To-Do → In Progress → Completed

### Priority Levels
- Low: Nice to have
- Medium: Normal priority (default)
- High: Important
- Urgent: Critical/time-sensitive

### 👥 Sample Data

The database includes demo accounts for testing:

**Users (username / password):**
- `admin` / `admin123` - System Administrator
- `john_doe` / `john123` - John Doe  
- `jane_smith` / `jane123` - Jane Smith
- `mike_wilson` / `mike123` - Mike Wilson

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


**Build Failures**
```bash
# Clean and rebuild
mvn clean package

# Check Java version
java -version  # Should be 11+
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
1. ✅ Java 11+ installed
2. ✅ Maven 3.6+ installed
3. ✅ Check application logs for detailed errors

### Contact

- **GitHub**: [@atrishmanm](https://github.com/atrishmanm) ,[@arnav182006](https://github.com/arnav182006),[Divyanshu Kant](https://github.com/Astern-ops)
- **Repository**: [WorkSphere](https://github.com/atrishmanm/WorkSphere)


---
# IMPORTANT NOTICE

This software and its source code are the intellectual property of Atrishman. Copying, modifying, publishing, or redistributing any part of this codebase is strictly prohibited without explicit written permission. See LICENSE for details.


<div align="center">

**Built with ❤️ by [Atrishman Mukherjee](https://github.com/atrishmanm), [Arnav Gupta](https://github.com/arnav182006), [Divyanshu Kant](https://github.com/Astern-ops)**

⭐ **Star this repository if you find it helpful!** ⭐

*Happy Task Managing!* 📋✅

</div>