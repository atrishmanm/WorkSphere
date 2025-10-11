# Trello Task Manager - Setup Instructions

## Quick Start Guide

### 1. Database Setup (MySQL)

1. **Install MySQL** (version 8.0 or higher)
   - Download from: https://dev.mysql.com/downloads/mysql/
   - Or use package manager:
     - Windows: `choco install mysql`
     - macOS: `brew install mysql`
     - Ubuntu: `sudo apt-get install mysql-server`

2. **Start MySQL Service**
   ```bash
   # Windows (Command Prompt as Administrator)
   net start mysql

   # macOS/Linux
   sudo systemctl start mysql
   # or
   sudo service mysql start
   ```

3. **Create Database and Tables**
   ```bash
   # Login to MySQL
   mysql -u root -p

   # Run the schema file
   source src/main/resources/schema.sql

   # Or copy-paste the SQL from schema.sql file
   ```

### 2. Configure Database Connection

Edit `src/main/resources/application.properties`:

```properties
# Database Configuration
db.url=jdbc:mysql://localhost:3306/trello_task_manager?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
db.username=root
db.password=YOUR_MYSQL_PASSWORD
db.driver=com.mysql.cj.jdbc.Driver
```

**Replace `YOUR_MYSQL_PASSWORD` with your actual MySQL root password**

### 3. Build and Run

```bash
# Navigate to project directory
cd trello-task-manager

# Compile the project
mvn clean compile

# Run the application
mvn exec:java

# Alternative: Create JAR and run
mvn clean package
java -cp target/task-manager-1.0.0.jar com.trello.TrelloTaskManagerApp
```

### 4. First Time Usage

1. **Test Connection**: The app will test database connectivity on startup
2. **Login/Create User**: 
   - Use existing sample users: `admin`, `john_doe`, `jane_smith`, `mike_wilson`
   - Or create a new user
3. **Explore Features**: Navigate through the menu to manage tasks

### 5. Troubleshooting

#### Connection Issues
- **"Access denied"**: Check MySQL username/password in application.properties
- **"Connection refused"**: Ensure MySQL service is running
- **"Unknown database"**: Run the schema.sql script to create the database

#### Common Commands
```bash
# Check MySQL status
# Windows
sc query mysql

# Linux/macOS
systemctl status mysql

# Reset MySQL password (if needed)
# Windows
mysqladmin -u root password newpassword

# Connect to MySQL with specific user
mysql -u root -p -h localhost -P 3306
```

#### Java Issues
- **"Class not found"**: Ensure Java 11+ is installed
- **"Package does not exist"**: Run `mvn clean compile`
- **Out of memory**: Add JVM args: `java -Xmx512m -jar ...`

### 6. Sample Data

The database comes pre-loaded with:

**Users:**
- `admin` - System Administrator
- `john_doe` - John Doe  
- `jane_smith` - Jane Smith
- `mike_wilson` - Mike Wilson

**Tasks:**
- 6 sample tasks with various priorities and statuses
- Some assigned to users, some with due dates

### 7. Development Setup

For development/testing:

```bash
# Run tests
mvn test

# Generate test coverage report
mvn test jacoco:report

# Run with debug mode
mvn exec:java -Dexec.args="-debug"

# Package for distribution
mvn clean package
```

### 8. IDE Setup (Optional)

**IntelliJ IDEA:**
1. File → Open → Select project folder
2. Trust the Maven project
3. Wait for dependencies to download
4. Run TrelloTaskManagerApp.main()

**Eclipse:**
1. File → Import → Existing Maven Projects
2. Select project folder
3. Right-click project → Run As → Java Application
4. Select TrelloTaskManagerApp

**VS Code:**
1. Open project folder
2. Install Java Extension Pack
3. Trust the workspace
4. Press F5 to run/debug

---

## Need Help?

- Check README.md for detailed documentation
- Review application.properties for configuration
- Examine schema.sql for database structure
- Look at sample data for usage examples

**Happy Task Managing! 📋✅**