package com.worksphere.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Database connection utility class for managing SQLite connections
 */
public class DatabaseConnection {
    private static final String PROPERTIES_FILE = "/application.properties";
    private static final String SCHEMA_FILE = "/schema.sql";
    private static Properties properties;
    private static boolean databaseInitialized = false;
    private static final Object initLock = new Object();
    
    static {
        loadProperties();
    }
    
    /**
     * Load database properties from application.properties file
     */
    private static void loadProperties() {
        properties = new Properties();
        try (InputStream input = DatabaseConnection.class.getResourceAsStream(PROPERTIES_FILE)) {
            if (input == null) {
                throw new RuntimeException("Unable to find " + PROPERTIES_FILE);
            }
            properties.load(input);
        } catch (IOException e) {
            throw new RuntimeException("Error loading database properties", e);
        }
    }
    
    /**
     * Get a database connection and initialize database if needed
     * @return Connection object
     * @throws SQLException if connection fails
     */
    public static Connection getConnection() throws SQLException {
        try {
            // Load SQLite JDBC driver
            Class.forName(properties.getProperty("db.driver"));
            
            String url = properties.getProperty("db.url");
            
            // If the URL is relative (just filename), make it absolute in user's home directory
            if (url.startsWith("jdbc:sqlite:") && !url.contains("/") && !url.contains("\\")) {
                String dbFileName = url.substring("jdbc:sqlite:".length());
                String userHome = System.getProperty("user.home");
                String absolutePath = userHome + System.getProperty("file.separator") + ".worksphere" + System.getProperty("file.separator") + dbFileName;
                
                // Create directory if it doesn't exist
                java.io.File dbDir = new java.io.File(userHome + System.getProperty("file.separator") + ".worksphere");
                if (!dbDir.exists()) {
                    dbDir.mkdirs();
                }
                
                url = "jdbc:sqlite:" + absolutePath;
                // Only print database path once during initialization
                if (!databaseInitialized) {
                    System.out.println("📁 Using database: " + absolutePath);
                }
            }
            
            String username = properties.getProperty("db.username");
            String password = properties.getProperty("db.password");
            
            Connection connection;
            if (username != null && !username.trim().isEmpty()) {
                connection = DriverManager.getConnection(url, username, password);
            } else {
                connection = DriverManager.getConnection(url);
            }
            
            // Initialize database if not already done
            synchronized (initLock) {
                if (!databaseInitialized) {
                    System.out.println("🔧 Initializing database...");
                    initializeDatabase(connection);
                    runMigrations(connection);
                    databaseInitialized = true;
                    System.out.println("✅ Database initialization complete.");
                }
            }
            
            return connection;
        } catch (ClassNotFoundException e) {
            throw new SQLException("SQLite JDBC driver not found", e);
        }
    }
    
    /**
     * Initialize the database by executing the schema.sql file
     * @param connection Database connection
     * @throws SQLException if initialization fails
     */
    private static void initializeDatabase(Connection connection) throws SQLException {
        try (InputStream input = DatabaseConnection.class.getResourceAsStream(SCHEMA_FILE)) {
            if (input == null) {
                System.out.println("Warning: " + SCHEMA_FILE + " not found. Database will not be initialized.");
                return;
            }
            
            // Read the entire schema file
            StringBuilder schemaBuilder = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(input))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    // Skip comments and empty lines
                    line = line.trim();
                    if (!line.isEmpty() && !line.startsWith("--")) {
                        schemaBuilder.append(line).append(" ");
                    }
                }
            }
            
            // Split by semicolon to get individual SQL statements
            String[] statements = schemaBuilder.toString().split(";");
            
            try (Statement stmt = connection.createStatement()) {
                for (String sql : statements) {
                    sql = sql.trim();
                    if (!sql.isEmpty()) {
                        try {
                            stmt.execute(sql);
                        } catch (SQLException e) {
                            // Log the error but continue with other statements
                            System.err.println("Warning: Failed to execute SQL statement: " + sql);
                            System.err.println("Error: " + e.getMessage());
                        }
                    }
                }
            }
            
            System.out.println("✅ Database initialized successfully!");
            
        } catch (IOException e) {
            throw new SQLException("Error reading schema file", e);
        }
    }
    
    /**
     * Close database connection safely
     * @param connection Connection to close
     */
    public static void closeConnection(Connection connection) {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                System.err.println("Error closing database connection: " + e.getMessage());
            }
        }
    }
    
    /**
     * Test database connection
     * @return true if connection is successful, false otherwise
     */
    public static boolean testConnection() {
        try (Connection connection = getConnection()) {
            return connection != null && !connection.isClosed();
        } catch (SQLException e) {
            System.err.println("Database connection test failed: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Get database URL from properties
     * @return database URL
     */
    public static String getDatabaseUrl() {
        return properties.getProperty("db.url");
    }
    
    /**
     * Get database username from properties
     * @return database username
     */
    public static String getDatabaseUsername() {
        return properties.getProperty("db.username");
    }
    
    /**
     * Reset the auto-increment counter for tasks table to start from 1
     * This is useful when you want task IDs to start from 1 again
     * @throws SQLException if reset operation fails
     */
    public static void resetTaskAutoIncrement() throws SQLException {
        try (Connection connection = getConnection();
             Statement stmt = connection.createStatement()) {
            
            // Delete the SQLite sequence entry for tasks table
            stmt.execute("DELETE FROM sqlite_sequence WHERE name='tasks'");
            
            System.out.println("✅ Task auto-increment counter has been reset. Next task will start from ID 1.");
        }
    }
    
    /**
     * Get the current auto-increment value for tasks table
     * @return the next auto-increment value that will be used
     * @throws SQLException if operation fails
     */
    public static int getCurrentTaskAutoIncrement() throws SQLException {
        try (Connection connection = getConnection();
             Statement stmt = connection.createStatement()) {
            
            ResultSet rs = stmt.executeQuery("SELECT seq FROM sqlite_sequence WHERE name='tasks'");
            if (rs.next()) {
                return rs.getInt("seq") + 1;
            } else {
                return 1; // No sequence entry means next will be 1
            }
        }
    }
    
    /**
     * Check if database is SQLite
     * @return true if using SQLite, false otherwise
     */
    public static boolean isSQLite() {
        String url = properties.getProperty("db.url");
        return url != null && url.startsWith("jdbc:sqlite:");
    }
    
    /**
     * Run database migrations to update existing databases
     * @param connection Database connection
     * @throws SQLException if migration fails
     */
    private static void runMigrations(Connection connection) throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            // Migration 1: Add is_admin column to users table if it doesn't exist
            try {
                // First, check if the column already exists
                stmt.execute("SELECT is_admin FROM users LIMIT 1");
                System.out.println("✅ is_admin column already exists in users table");
            } catch (SQLException e) {
                // Column doesn't exist, add it
                if (e.getMessage().contains("no such column: is_admin") || 
                    e.getMessage().contains("table users has no column named is_admin")) {
                    
                    System.out.println("🔄 Adding is_admin column to users table...");
                    stmt.execute("ALTER TABLE users ADD COLUMN is_admin BOOLEAN DEFAULT 0");
                    
                    // Make the first user (typically admin) an admin
                    stmt.execute("UPDATE users SET is_admin = 1 WHERE id = 1");
                    
                    System.out.println("✅ Successfully added is_admin column and set first user as admin");
                } else {
                    // Some other error, re-throw it
                    throw e;
                }
            }
            
            // Migration 2: Add enhanced task fields
            System.out.println("🔄 Checking and adding enhanced task fields...");
            
            // Add completed_at column
            try {
                stmt.execute("SELECT completed_at FROM tasks LIMIT 1");
                System.out.println("✅ completed_at column already exists");
            } catch (SQLException e) {
                if (e.getMessage().contains("no such column: completed_at")) {
                    System.out.println("🔄 Adding completed_at column to tasks table...");
                    stmt.execute("ALTER TABLE tasks ADD COLUMN completed_at DATETIME");
                    System.out.println("✅ completed_at column added");
                } else {
                    throw e;
                }
            }
            
            // Add estimated_minutes column
            try {
                stmt.execute("SELECT estimated_minutes FROM tasks LIMIT 1");
                System.out.println("✅ estimated_minutes column already exists");
            } catch (SQLException e) {
                if (e.getMessage().contains("no such column: estimated_minutes")) {
                    System.out.println("🔄 Adding estimated_minutes column to tasks table...");
                    stmt.execute("ALTER TABLE tasks ADD COLUMN estimated_minutes INTEGER DEFAULT 0");
                    System.out.println("✅ estimated_minutes column added");
                } else {
                    throw e;
                }
            }
            
            // Add actual_minutes column
            try {
                stmt.execute("SELECT actual_minutes FROM tasks LIMIT 1");
                System.out.println("✅ actual_minutes column already exists");
            } catch (SQLException e) {
                if (e.getMessage().contains("no such column: actual_minutes")) {
                    System.out.println("🔄 Adding actual_minutes column to tasks table...");
                    stmt.execute("ALTER TABLE tasks ADD COLUMN actual_minutes INTEGER DEFAULT 0");
                    System.out.println("✅ actual_minutes column added");
                } else {
                    throw e;
                }
            }
            
            // Add category_id column
            try {
                stmt.execute("SELECT category_id FROM tasks LIMIT 1");
                System.out.println("✅ category_id column already exists");
            } catch (SQLException e) {
                if (e.getMessage().contains("no such column: category_id")) {
                    System.out.println("🔄 Adding category_id column to tasks table...");
                    stmt.execute("ALTER TABLE tasks ADD COLUMN category_id INTEGER");
                    System.out.println("✅ category_id column added");
                } else {
                    throw e;
                }
            }
            
            // Add recurrence_rule column
            try {
                stmt.execute("SELECT recurrence_rule FROM tasks LIMIT 1");
                System.out.println("✅ recurrence_rule column already exists");
            } catch (SQLException e) {
                if (e.getMessage().contains("no such column: recurrence_rule")) {
                    System.out.println("🔄 Adding recurrence_rule column to tasks table...");
                    stmt.execute("ALTER TABLE tasks ADD COLUMN recurrence_rule TEXT");
                    System.out.println("✅ recurrence_rule column added");
                } else {
                    throw e;
                }
            }
            
            // Add parent_task_id column
            try {
                stmt.execute("SELECT parent_task_id FROM tasks LIMIT 1");
                System.out.println("✅ parent_task_id column already exists");
            } catch (SQLException e) {
                if (e.getMessage().contains("no such column: parent_task_id")) {
                    System.out.println("🔄 Adding parent_task_id column to tasks table...");
                    stmt.execute("ALTER TABLE tasks ADD COLUMN parent_task_id INTEGER");
                    System.out.println("✅ parent_task_id column added");
                } else {
                    throw e;
                }
            }
            
            // Add last_worked_at column
            try {
                stmt.execute("SELECT last_worked_at FROM tasks LIMIT 1");
                System.out.println("✅ last_worked_at column already exists");
            } catch (SQLException e) {
                if (e.getMessage().contains("no such column: last_worked_at")) {
                    System.out.println("🔄 Adding last_worked_at column to tasks table...");
                    stmt.execute("ALTER TABLE tasks ADD COLUMN last_worked_at DATETIME");
                    System.out.println("✅ last_worked_at column added");
                } else {
                    throw e;
                }
            }
            
            // Add tags column
            try {
                stmt.execute("SELECT tags FROM tasks LIMIT 1");
                System.out.println("✅ tags column already exists");
            } catch (SQLException e) {
                if (e.getMessage().contains("no such column: tags")) {
                    System.out.println("🔄 Adding tags column to tasks table...");
                    stmt.execute("ALTER TABLE tasks ADD COLUMN tags TEXT DEFAULT ''");
                    System.out.println("✅ tags column added");
                } else {
                    throw e;
                }
            }
            
            // Migration 3: Create categories table if it doesn't exist
            System.out.println("🔄 Checking and creating categories table...");
            try {
                stmt.execute("SELECT COUNT(*) FROM categories LIMIT 1");
                System.out.println("✅ categories table already exists");
            } catch (SQLException e) {
                if (e.getMessage().contains("no such table: categories")) {
                    System.out.println("🔄 Creating categories table...");
                    stmt.execute("CREATE TABLE categories (" +
                                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                                "name TEXT UNIQUE NOT NULL, " +
                                "description TEXT, " +
                                "color TEXT DEFAULT '#007ACC', " +
                                "created_at DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                                "updated_at DATETIME DEFAULT CURRENT_TIMESTAMP" +
                                ")");
                    
                    // Insert default categories
                    System.out.println("🔄 Inserting default categories...");
                    stmt.execute("INSERT OR IGNORE INTO categories (name, description, color) VALUES " +
                                "('Work', 'Work-related tasks', '#FF6B35')");
                    stmt.execute("INSERT OR IGNORE INTO categories (name, description, color) VALUES " +
                                "('Personal', 'Personal tasks and activities', '#4ECDC4')");
                    stmt.execute("INSERT OR IGNORE INTO categories (name, description, color) VALUES " +
                                "('Learning', 'Educational and skill development', '#45B7D1')");
                    stmt.execute("INSERT OR IGNORE INTO categories (name, description, color) VALUES " +
                                "('Health', 'Health and wellness activities', '#96CEB4')");
                    stmt.execute("INSERT OR IGNORE INTO categories (name, description, color) VALUES " +
                                "('Finance', 'Financial planning and tasks', '#FFEAA7')");
                    stmt.execute("INSERT OR IGNORE INTO categories (name, description, color) VALUES " +
                                "('Home', 'Household and maintenance tasks', '#DDA0DD')");
                    
                    System.out.println("✅ categories table and default categories created");
                } else {
                    throw e;
                }
            }
            
            // Migration 4: Create task_tags table if it doesn't exist
            System.out.println("🔄 Checking and creating task_tags table...");
            try {
                stmt.execute("SELECT COUNT(*) FROM task_tags LIMIT 1");
                System.out.println("✅ task_tags table already exists");
            } catch (SQLException e) {
                if (e.getMessage().contains("no such table: task_tags")) {
                    System.out.println("🔄 Creating task_tags table...");
                    stmt.execute("CREATE TABLE task_tags (" +
                                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                                "task_id INTEGER NOT NULL, " +
                                "tag_name TEXT NOT NULL, " +
                                "created_at DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                                "FOREIGN KEY (task_id) REFERENCES tasks(id) ON DELETE CASCADE, " +
                                "UNIQUE(task_id, tag_name)" +
                                ")");
                    System.out.println("✅ task_tags table created");
                } else {
                    throw e;
                }
            }
            
            // Migration 5: Create time_logs table if it doesn't exist
            System.out.println("🔄 Checking and creating time_logs table...");
            try {
                stmt.execute("SELECT COUNT(*) FROM time_logs LIMIT 1");
                System.out.println("✅ time_logs table already exists");
            } catch (SQLException e) {
                if (e.getMessage().contains("no such table: time_logs")) {
                    System.out.println("🔄 Creating time_logs table...");
                    stmt.execute("CREATE TABLE time_logs (" +
                                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                                "task_id INTEGER NOT NULL, " +
                                "user_id INTEGER NOT NULL, " +
                                "start_time DATETIME NOT NULL, " +
                                "end_time DATETIME, " +
                                "minutes INTEGER DEFAULT 0, " +
                                "description TEXT, " +
                                "created_at DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                                "FOREIGN KEY (task_id) REFERENCES tasks(id) ON DELETE CASCADE, " +
                                "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE" +
                                ")");
                    System.out.println("✅ time_logs table created");
                } else {
                    throw e;
                }
            }
            
            // Migration 5: Initialize default values for existing tasks
            System.out.println("🔧 Initializing default values for existing tasks...");
            
            // Update tasks with null or empty tags to have empty string
            stmt.execute("UPDATE tasks SET tags = '' WHERE tags IS NULL");
            
            // Set default estimated_minutes for tasks that don't have it set (1 hour default)
            stmt.execute("UPDATE tasks SET estimated_minutes = 60 WHERE estimated_minutes = 0 OR estimated_minutes IS NULL");
            
            // Set default actual_minutes to 0 for tasks that don't have it set  
            stmt.execute("UPDATE tasks SET actual_minutes = 0 WHERE actual_minutes IS NULL");
            
            // Set default category_id to NULL for tasks that don't have a valid category
            stmt.execute("UPDATE tasks SET category_id = NULL WHERE category_id = 0");
            
            // Initialize completed_at for completed tasks that don't have it
            stmt.execute("UPDATE tasks SET completed_at = datetime('now') WHERE status = 'COMPLETED' AND completed_at IS NULL");
            
            // Initialize last_worked_at for in-progress tasks
            stmt.execute("UPDATE tasks SET last_worked_at = datetime('now') WHERE status = 'IN_PROGRESS' AND last_worked_at IS NULL");
            
            System.out.println("✅ Default values initialized for existing tasks");
            
            // Migration 6: Insert sample users if they don't exist
            System.out.println("🔄 Checking and inserting sample users...");
            
            // Insert admin user if not exists
            stmt.execute("INSERT OR IGNORE INTO users (username, email, full_name, is_admin) VALUES " +
                        "('admin', 'admin@trello.com', 'System Administrator', 1)");
            
            // Insert regular users if not exist
            stmt.execute("INSERT OR IGNORE INTO users (username, email, full_name, is_admin) VALUES " +
                        "('john_doe', 'john.doe@example.com', 'John Doe', 0)");
            stmt.execute("INSERT OR IGNORE INTO users (username, email, full_name, is_admin) VALUES " +
                        "('jane_smith', 'jane.smith@example.com', 'Jane Smith', 0)");
            stmt.execute("INSERT OR IGNORE INTO users (username, email, full_name, is_admin) VALUES " +
                        "('mike_wilson', 'mike.wilson@example.com', 'Mike Wilson', 0)");
            
            // Insert sample tasks (if needed) - Check if our specific sample tasks already exist
            System.out.println("🔍 Checking for existing sample tasks...");
            
            // Check for multiple sample task titles to be more robust
            ResultSet existingTasks = stmt.executeQuery(
                "SELECT COUNT(*) FROM tasks WHERE title IN (" +
                "'Setup project environment', " +
                "'Design database schema', " +
                "'Implement user authentication', " +
                "'Create task management UI', " +
                "'Write unit tests'" +
                ")"
            );
            int sampleTaskCount = 0;
            if (existingTasks.next()) {
                sampleTaskCount = existingTasks.getInt(1);
            }
            existingTasks.close();
            
            // Also check total task count for debugging
            ResultSet totalTasks = stmt.executeQuery("SELECT COUNT(*) FROM tasks");
            int totalTaskCount = 0;
            if (totalTasks.next()) {
                totalTaskCount = totalTasks.getInt(1);
            }
            totalTasks.close();
            
            // Debug: Show recent tasks to identify the source of new tasks
            ResultSet recentTasks = stmt.executeQuery(
                "SELECT id, title, created_at FROM tasks ORDER BY id DESC LIMIT 10"
            );
            System.out.println("🔍 Recent tasks (last 10):");
            while (recentTasks.next()) {
                System.out.println("  - ID " + recentTasks.getInt("id") + ": " + recentTasks.getString("title") + " (created: " + recentTasks.getString("created_at") + ")");
            }
            recentTasks.close();
            
            System.out.println("📊 Database status: " + totalTaskCount + " total tasks, " + sampleTaskCount + " sample tasks found");
            
            // Only insert sample tasks if we have fewer than 5 sample tasks (to account for potential partial insertions)
            System.out.println("🔍 Sample task check: sampleTaskCount = " + sampleTaskCount + ", condition: sampleTaskCount < 5 = " + (sampleTaskCount < 5));
            if (sampleTaskCount < 5) {
                System.out.println("🔄 Inserting 10 sample tasks...");
                
                // First ensure we have enough users for the task assignments
                ResultSet userCheck = stmt.executeQuery("SELECT COUNT(*) FROM users");
                int userCount = 0;
                if (userCheck.next()) {
                    userCount = userCheck.getInt(1);
                }
                userCheck.close();
                
                if (userCount >= 4) {
                    try {
                        System.out.println("💾 Starting task insertions...");
                        // Insert only 10 sample tasks WITHOUT specifying IDs to let auto-increment work properly
                        System.out.println("⚡ DIRECT SQL: Inserting Task 1 - Setup project environment");
                        stmt.execute("INSERT INTO tasks (title, description, priority, status, due_date, assigned_to, created_by) VALUES " +
                                    "('Setup project environment', 'Configure development environment and install necessary tools', 'HIGH', 'COMPLETED', '2024-01-15', 2, 1)");
                        System.out.println("  ✓ Task 1 inserted");
                        System.out.println("⚡ DIRECT SQL: Inserting Task 2 - Design database schema");
                        stmt.execute("INSERT INTO tasks (title, description, priority, status, due_date, assigned_to, created_by) VALUES " +
                                    "('Design database schema', 'Create ERD and implement database tables', 'HIGH', 'COMPLETED', '2024-01-20', 2, 1)");
                        System.out.println("  ✓ Task 2 inserted");
                        System.out.println("⚡ DIRECT SQL: Inserting Task 3 - Implement user authentication");
                        stmt.execute("INSERT INTO tasks (title, description, priority, status, due_date, assigned_to, created_by) VALUES " +
                                    "('Implement user authentication', 'Build login and registration functionality', 'MEDIUM', 'IN_PROGRESS', '2024-02-01', 3, 1)");
                        System.out.println("  ✓ Task 3 inserted");
                        System.out.println("⚡ DIRECT SQL: Inserting Task 4 - Create task management UI");
                        stmt.execute("INSERT INTO tasks (title, description, priority, status, due_date, assigned_to, created_by) VALUES " +
                                    "('Create task management UI', 'Design and implement user interface for task management', 'MEDIUM', 'TODO', '2024-02-15', 3, 1)");
                        System.out.println("  ✓ Task 4 inserted");
                        System.out.println("⚡ DIRECT SQL: Inserting Task 5 - Write unit tests");
                        stmt.execute("INSERT INTO tasks (title, description, priority, status, due_date, assigned_to, created_by) VALUES " +
                                    "('Write unit tests', 'Implement comprehensive test suite', 'LOW', 'TODO', '2024-03-01', 4, 1)");
                        System.out.println("  ✓ Task 5 inserted");
                        System.out.println("⚡ DIRECT SQL: Inserting Task 6 - Deploy to production");
                        stmt.execute("INSERT INTO tasks (title, description, priority, status, due_date, assigned_to, created_by) VALUES " +
                                    "('Deploy to production', 'Setup production environment and deploy application', 'URGENT', 'TODO', '2024-03-15', 2, 1)");
                        System.out.println("  ✓ Task 6 inserted");
                        System.out.println("⚡ DIRECT SQL: Inserting Task 7 - Code review process");
                        stmt.execute("INSERT INTO tasks (title, description, priority, status, due_date, assigned_to, created_by) VALUES " +
                                    "('Code review process', 'Establish code review guidelines and implement peer review workflow', 'MEDIUM', 'TODO', '2024-02-10', 3, 1)");
                        System.out.println("  ✓ Task 7 inserted");
                        System.out.println("⚡ DIRECT SQL: Inserting Task 8 - Performance optimization");
                        stmt.execute("INSERT INTO tasks (title, description, priority, status, due_date, assigned_to, created_by) VALUES " +
                                    "('Performance optimization', 'Analyze and optimize application performance bottlenecks', 'HIGH', 'TODO', '2024-02-28', 2, 1)");
                        System.out.println("  ✓ Task 8 inserted");
                        System.out.println("⚡ DIRECT SQL: Inserting Task 9 - Documentation update");
                        stmt.execute("INSERT INTO tasks (title, description, priority, status, due_date, assigned_to, created_by) VALUES " +
                                    "('Documentation update', 'Update user manual and API documentation', 'LOW', 'TODO', '2024-03-10', 4, 1)");
                        System.out.println("  ✓ Task 9 inserted");
                        System.out.println("⚡ DIRECT SQL: Inserting Task 10 - Security audit");
                        stmt.execute("INSERT INTO tasks (title, description, priority, status, due_date, assigned_to, created_by) VALUES " +
                                    "('Security audit', 'Conduct comprehensive security audit and fix vulnerabilities', 'URGENT', 'TODO', '2024-02-20', 2, 1)");
                        System.out.println("  ✓ Task 10 inserted");
                        
                        System.out.println("✅ 10 sample tasks inserted successfully!");
                    } catch (SQLException taskError) {
                        System.err.println("⚠️  Warning: Could not insert sample tasks due to foreign key constraints. Users may not exist yet.");
                        System.err.println("Error details: " + taskError.getMessage());
                    }
                } else {
                    System.out.println("⚠️  Skipping task insertion: insufficient users in database (found " + userCount + ", need at least 4)");
                }
            } else {
                System.out.println("ℹ️  Sample tasks already exist (" + sampleTaskCount + " found), skipping insertion.");
            }
            
            // Migration 7: Clean up tasks for demo (limit to 20 tasks)
            System.out.println("🧹 Cleaning up tasks for demo presentation (limiting to 20 tasks)...");
            
            // First, check current task count
            ResultSet taskCountRs = stmt.executeQuery("SELECT COUNT(*) FROM tasks");
            int currentTaskCount = 0;
            if (taskCountRs.next()) {
                currentTaskCount = taskCountRs.getInt(1);
            }
            taskCountRs.close();
            
            System.out.println("📊 Current task count: " + currentTaskCount);
            
            if (currentTaskCount > 20) {
                System.out.println("🔄 Reducing tasks to 20 for demo purposes...");
                
                // Delete excess tasks, keeping the first 20
                stmt.execute("DELETE FROM task_tags WHERE task_id > 20");
                stmt.execute("DELETE FROM tasks WHERE id > 20");
                
                // Reset auto-increment to prevent large IDs
                stmt.execute("DELETE FROM sqlite_sequence WHERE name='tasks'");
                stmt.execute("INSERT INTO sqlite_sequence (name, seq) VALUES ('tasks', 20)");
                
                System.out.println("✅ Reduced to 20 tasks for clean demo presentation");
            } else {
                System.out.println("ℹ️  Task count already appropriate for demo (" + currentTaskCount + " tasks)");
            }
            
            // Migration 8: Enhance existing tasks with realistic sample data
            System.out.println("🔧 Enhancing existing tasks with realistic sample data...");
            
            // Define arrays of sample data for different categories of tasks
            String[] devTags = {"development", "coding", "frontend", "backend", "database", "testing", "debugging"};
            String[] projectTags = {"planning", "deployment", "production", "infrastructure", "monitoring"};
            String[] docTags = {"documentation", "manual", "api", "guide", "training"};
            String[] securityTags = {"security", "audit", "vulnerability", "compliance", "encryption"};
            String[] performanceTags = {"performance", "optimization", "monitoring", "scalability", "efficiency"};
            
            // Get list of existing tasks to enhance
            ResultSet existingTasksRs = stmt.executeQuery(
                "SELECT id, title, priority, status FROM tasks ORDER BY id"
            );
            
            List<Map<String, Object>> tasksToEnhance = new ArrayList<>();
            while (existingTasksRs.next()) {
                Map<String, Object> task = new HashMap<>();
                task.put("id", existingTasksRs.getInt("id"));
                task.put("title", existingTasksRs.getString("title"));
                task.put("priority", existingTasksRs.getString("priority"));
                task.put("status", existingTasksRs.getString("status"));
                tasksToEnhance.add(task);
            }
            existingTasksRs.close();
            
            System.out.println("📊 Found " + tasksToEnhance.size() + " tasks to enhance");
            
            // Enhance each task with realistic data
            for (Map<String, Object> task : tasksToEnhance) {
                int taskId = (Integer) task.get("id");
                String title = (String) task.get("title");
                String priority = (String) task.get("priority");
                String status = (String) task.get("status");
                
                // Determine category and tags based on task title
                int categoryId = 1; // Default to Work
                String[] tagsToUse = devTags;
                int estimatedMinutes = 120; // Default 2 hours
                int actualMinutes = 0;
                
                if (title.toLowerCase().contains("environment") || title.toLowerCase().contains("setup")) {
                    categoryId = 1; // Work
                    tagsToUse = new String[]{"setup", "environment", "configuration", "tools"};
                    estimatedMinutes = 180; // 3 hours
                    actualMinutes = status.equals("COMPLETED") ? 165 : 45;
                } else if (title.toLowerCase().contains("database") || title.toLowerCase().contains("schema")) {
                    categoryId = 1; // Work
                    tagsToUse = new String[]{"database", "schema", "design", "sql"};
                    estimatedMinutes = 240; // 4 hours
                    actualMinutes = status.equals("COMPLETED") ? 220 : 80;
                } else if (title.toLowerCase().contains("authentication") || title.toLowerCase().contains("login")) {
                    categoryId = 1; // Work
                    tagsToUse = new String[]{"authentication", "security", "login", "backend"};
                    estimatedMinutes = 300; // 5 hours
                    actualMinutes = status.equals("IN_PROGRESS") ? 120 : (status.equals("COMPLETED") ? 285 : 0);
                } else if (title.toLowerCase().contains("ui") || title.toLowerCase().contains("interface")) {
                    categoryId = 1; // Work
                    tagsToUse = new String[]{"ui", "frontend", "design", "user-experience"};
                    estimatedMinutes = 360; // 6 hours
                    actualMinutes = status.equals("TODO") ? 0 : 60;
                } else if (title.toLowerCase().contains("test")) {
                    categoryId = 1; // Work
                    tagsToUse = new String[]{"testing", "quality", "automation", "coverage"};
                    estimatedMinutes = 240; // 4 hours
                    actualMinutes = status.equals("TODO") ? 0 : 30;
                } else if (title.toLowerCase().contains("deploy") || title.toLowerCase().contains("production")) {
                    categoryId = 1; // Work
                    tagsToUse = new String[]{"deployment", "production", "devops", "infrastructure"};
                    estimatedMinutes = 180; // 3 hours
                    actualMinutes = status.equals("TODO") ? 0 : 45;
                } else if (title.toLowerCase().contains("review") || title.toLowerCase().contains("code")) {
                    categoryId = 1; // Work
                    tagsToUse = new String[]{"code-review", "quality", "collaboration", "standards"};
                    estimatedMinutes = 120; // 2 hours
                    actualMinutes = status.equals("TODO") ? 0 : 30;
                } else if (title.toLowerCase().contains("performance") || title.toLowerCase().contains("optimization")) {
                    categoryId = 1; // Work
                    tagsToUse = new String[]{"performance", "optimization", "monitoring", "analysis"};
                    estimatedMinutes = 300; // 5 hours
                    actualMinutes = status.equals("TODO") ? 0 : 90;
                } else if (title.toLowerCase().contains("documentation")) {
                    categoryId = 1; // Work
                    tagsToUse = new String[]{"documentation", "writing", "manual", "guide"};
                    estimatedMinutes = 180; // 3 hours
                    actualMinutes = status.equals("TODO") ? 0 : 45;
                } else if (title.toLowerCase().contains("security") || title.toLowerCase().contains("audit")) {
                    categoryId = 1; // Work
                    tagsToUse = new String[]{"security", "audit", "vulnerability", "compliance"};
                    estimatedMinutes = 240; // 4 hours
                    actualMinutes = status.equals("TODO") ? 0 : 60;
                }
                
                // Adjust actual minutes based on priority and status
                if (priority.equals("URGENT")) {
                    estimatedMinutes = (int)(estimatedMinutes * 0.8); // Urgent tasks get less time
                    if (!status.equals("TODO")) {
                        actualMinutes = (int)(actualMinutes * 1.2); // But often take longer
                    }
                } else if (priority.equals("LOW")) {
                    estimatedMinutes = (int)(estimatedMinutes * 1.5); // Low priority gets more generous time
                }
                
                // Update the task with enhanced data
                stmt.execute("UPDATE tasks SET " +
                            "estimated_minutes = " + estimatedMinutes + ", " +
                            "actual_minutes = " + actualMinutes + ", " +
                            "category_id = " + categoryId + " " +
                            "WHERE id = " + taskId);
                
                // Insert tags for this task
                for (String tag : tagsToUse) {
                    stmt.execute("INSERT OR IGNORE INTO task_tags (task_id, tag_name) VALUES (" + taskId + ", '" + tag + "')");
                }
                
                System.out.println("  ✅ Enhanced task " + taskId + ": " + title.substring(0, Math.min(title.length(), 30)) + "...");
            }
            
            // Add some additional sample tasks with diverse categories (only if we have less than 20 tasks)
            ResultSet finalTaskCountRs = stmt.executeQuery("SELECT COUNT(*) FROM tasks");
            int finalTaskCount = 0;
            if (finalTaskCountRs.next()) {
                finalTaskCount = finalTaskCountRs.getInt(1);
            }
            finalTaskCountRs.close();
            
            if (finalTaskCount < 20) {
                System.out.println("🔄 Adding a few diverse sample tasks for better demonstration...");
                
                // Personal category tasks
                stmt.execute("INSERT OR IGNORE INTO tasks (title, description, priority, status, due_date, assigned_to, created_by, estimated_minutes, category_id) VALUES " +
                            "('Morning workout routine', 'Complete 30-minute cardio and strength training', 'MEDIUM', 'TODO', '2024-12-15', 2, 1, 30, 2)");
                
                // Learning category tasks  
                stmt.execute("INSERT OR IGNORE INTO tasks (title, description, priority, status, due_date, assigned_to, created_by, estimated_minutes, category_id) VALUES " +
                            "('Learn React Hooks', 'Study and practice React Hooks patterns and best practices', 'HIGH', 'IN_PROGRESS', '2024-12-20', 3, 1, 240, 3)");
                
                // Health category tasks
                stmt.execute("INSERT OR IGNORE INTO tasks (title, description, priority, status, due_date, assigned_to, created_by, estimated_minutes, category_id) VALUES " +
                            "('Annual health checkup', 'Schedule and complete yearly medical examination', 'HIGH', 'TODO', '2024-12-30', 4, 1, 120, 4)");
                
                // Add tags for the new diverse tasks
                stmt.execute("INSERT OR IGNORE INTO task_tags (task_id, tag_name) VALUES " +
                            "((SELECT MAX(id) FROM tasks WHERE title = 'Morning workout routine'), 'fitness')");
                stmt.execute("INSERT OR IGNORE INTO task_tags (task_id, tag_name) VALUES " +
                            "((SELECT MAX(id) FROM tasks WHERE title = 'Morning workout routine'), 'health')");
                            
                stmt.execute("INSERT OR IGNORE INTO task_tags (task_id, tag_name) VALUES " +
                            "((SELECT MAX(id) FROM tasks WHERE title = 'Learn React Hooks'), 'learning')");
                stmt.execute("INSERT OR IGNORE INTO task_tags (task_id, tag_name) VALUES " +
                            "((SELECT MAX(id) FROM tasks WHERE title = 'Learn React Hooks'), 'javascript')");
                            
                stmt.execute("INSERT OR IGNORE INTO task_tags (task_id, tag_name) VALUES " +
                            "((SELECT MAX(id) FROM tasks WHERE title = 'Annual health checkup'), 'health')");
                stmt.execute("INSERT OR IGNORE INTO task_tags (task_id, tag_name) VALUES " +
                            "((SELECT MAX(id) FROM tasks WHERE title = 'Annual health checkup'), 'medical')");
                
                System.out.println("✅ Added diverse sample tasks for demo");
            } else {
                System.out.println("ℹ️  Already have enough tasks for demo presentation");
            }
            
            System.out.println("✅ Enhanced tasks with realistic sample data for demo!");
            System.out.println("📊 Demo dataset ready: ~20 tasks with categories, time estimates, and tags");
            
            System.out.println("✅ Database migrations completed successfully!");
            
        } catch (SQLException e) {
            System.err.println("❌ Error running database migrations: " + e.getMessage());
            throw e;
        }
    }
}
