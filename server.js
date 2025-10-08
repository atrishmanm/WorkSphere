const express = require('express');
const fs = require('fs').promises;
const path = require('path');
const cors = require('cors');

const app = express();
const PORT = 3000;
const DATA_FILE = path.join(__dirname, 'tasks.json');
const USERS_FILE = path.join(__dirname, 'users.json');

// Middleware
app.use(cors());
app.use(express.json());

// Redirect root to login page (before static files)
app.get('/', (req, res) => {
    res.redirect('/login.html');
});

app.use(express.static(__dirname)); // Serve static files from current directory

// Initialize data files if they don't exist
async function initializeDataFiles() {
    try {
        await fs.access(DATA_FILE);
    } catch {
        await fs.writeFile(DATA_FILE, JSON.stringify([], null, 2));
        console.log('Created tasks.json file');
    }
    
    try {
        await fs.access(USERS_FILE);
    } catch {
        const defaultUsers = [
            {
                id: 'admin',
                username: 'admin',
                password: 'admin123',
                role: 'admin',
                name: 'Administrator',
                email: 'admin@worksphere.local',
                createdAt: new Date().toISOString()
            },
            {
                id: 'user1',
                username: 'user1',
                password: 'user123',
                role: 'user',
                name: 'Demo User',
                email: 'user1@worksphere.local',
                createdAt: new Date().toISOString()
            }
        ];
        await fs.writeFile(USERS_FILE, JSON.stringify(defaultUsers, null, 2));
        console.log('Created users.json file with default users');
    }
}

// Helper function to read users
async function readUsers() {
    try {
        const data = await fs.readFile(USERS_FILE, 'utf8');
        return JSON.parse(data);
    } catch (error) {
        console.error('Error reading users:', error);
        return [];
    }
}

// Helper function to write users
async function writeUsers(users) {
    try {
        await fs.writeFile(USERS_FILE, JSON.stringify(users, null, 2));
        return true;
    } catch (error) {
        console.error('Error writing users:', error);
        return false;
    }
}

// Helper function to read tasks
async function readTasks() {
    try {
        const data = await fs.readFile(DATA_FILE, 'utf8');
        return JSON.parse(data);
    } catch (error) {
        console.error('Error reading tasks:', error);
        return [];
    }
}

// Helper function to write tasks
async function writeTasks(tasks) {
    try {
        await fs.writeFile(DATA_FILE, JSON.stringify(tasks, null, 2));
        return true;
    } catch (error) {
        console.error('Error writing tasks:', error);
        return false;
    }
}

// Generate unique ID
function generateId() {
    return Date.now().toString(36) + Math.random().toString(36).substr(2);
}

// Routes

// Authentication routes
app.post('/api/login', async (req, res) => {
    try {
        console.log('Login attempt:', req.body);
        const { username, password } = req.body;
        const users = await readUsers();
        console.log('Available users:', users.map(u => u.username));
        const user = users.find(u => u.username === username && u.password === password);
        
        if (user) {
            console.log('Login successful for:', username);
            const { password: _, ...userWithoutPassword } = user;
            res.json({ success: true, user: userWithoutPassword });
        } else {
            console.log('Login failed for:', username);
            res.status(401).json({ success: false, message: 'Invalid credentials' });
        }
    } catch (error) {
        console.error('Login error:', error);
        res.status(500).json({ success: false, message: 'Login error' });
    }
});

// User management routes (Admin only)
app.get('/api/users', async (req, res) => {
    try {
        const users = await readUsers();
        const usersWithoutPasswords = users.map(({ password, ...user }) => user);
        res.json(usersWithoutPasswords);
    } catch (error) {
        res.status(500).json({ error: 'Failed to fetch users' });
    }
});

app.post('/api/users', async (req, res) => {
    try {
        const { username, password, name, email, role } = req.body;
        
        if (!username || !password || !name || !role) {
            return res.status(400).json({ error: 'Username, password, name, and role are required' });
        }

        const users = await readUsers();
        
        // Check if username already exists
        if (users.find(u => u.username === username)) {
            return res.status(400).json({ error: 'Username already exists' });
        }

        const newUser = {
            id: generateId(),
            username,
            password,
            name,
            email: email || '',
            role,
            createdAt: new Date().toISOString()
        };

        users.push(newUser);
        const success = await writeUsers(users);
        
        if (success) {
            const { password: _, ...userWithoutPassword } = newUser;
            res.status(201).json(userWithoutPassword);
        } else {
            res.status(500).json({ error: 'Failed to create user' });
        }
    } catch (error) {
        res.status(500).json({ error: 'Failed to create user' });
    }
});

app.put('/api/users/:id', async (req, res) => {
    try {
        const { id } = req.params;
        const { username, password, name, email, role } = req.body;
        
        const users = await readUsers();
        const userIndex = users.findIndex(user => user.id === id);
        
        if (userIndex === -1) {
            return res.status(404).json({ error: 'User not found' });
        }
        
        // Check if new username conflicts with existing user (excluding current user)
        if (username && users.find(u => u.username === username && u.id !== id)) {
            return res.status(400).json({ error: 'Username already exists' });
        }
        
        // Update user data
        const updates = {};
        if (username) updates.username = username;
        if (password) updates.password = password;
        if (name) updates.name = name;
        if (email !== undefined) updates.email = email;
        if (role) updates.role = role;
        
        users[userIndex] = { ...users[userIndex], ...updates };
        const success = await writeUsers(users);
        
        if (success) {
            const { password: _, ...userWithoutPassword } = users[userIndex];
            res.json(userWithoutPassword);
        } else {
            res.status(500).json({ error: 'Failed to update user' });
        }
    } catch (error) {
        res.status(500).json({ error: 'Failed to update user' });
    }
});

app.delete('/api/users/:id', async (req, res) => {
    try {
        const { id } = req.params;
        
        const users = await readUsers();
        const filteredUsers = users.filter(user => user.id !== id);
        
        if (users.length === filteredUsers.length) {
            return res.status(404).json({ error: 'User not found' });
        }
        
        const success = await writeUsers(filteredUsers);
        
        if (success) {
            res.json({ message: 'User deleted successfully' });
        } else {
            res.status(500).json({ error: 'Failed to delete user' });
        }
    } catch (error) {
        res.status(500).json({ error: 'Failed to delete user' });
    }
});

// Get all tasks (with filtering)
app.get('/api/tasks', async (req, res) => {
    try {
        const { userId, assignedTo, status, priority, search, startDate, endDate, userRole } = req.query;
        let tasks = await readTasks();
        
        // Filter by user role and permissions
        if (userId && userRole !== 'admin') {
            tasks = tasks.filter(task => 
                task.userId === userId || task.assignedTo === userId
            );
        }
        
        // Filter by assigned user
        if (assignedTo) {
            tasks = tasks.filter(task => task.assignedTo === assignedTo);
        }
        
        // Filter by status
        if (status) {
            tasks = tasks.filter(task => task.status === status);
        }
        
        // Filter by priority
        if (priority) {
            tasks = tasks.filter(task => task.priority === priority);
        }
        
        // Filter by date range
        if (startDate) {
            tasks = tasks.filter(task => {
                const taskDate = task.dueDate !== 'No Date' ? new Date(task.dueDate) : new Date(task.createdAt);
                return taskDate >= new Date(startDate);
            });
        }
        
        if (endDate) {
            tasks = tasks.filter(task => {
                const taskDate = task.dueDate !== 'No Date' ? new Date(task.dueDate) : new Date(task.createdAt);
                return taskDate <= new Date(endDate);
            });
        }
        
        // Search filter
        if (search) {
            const searchLower = search.toLowerCase();
            tasks = tasks.filter(task => 
                task.title.toLowerCase().includes(searchLower) ||
                (task.description && task.description.toLowerCase().includes(searchLower)) ||
                task.assignedTo.toLowerCase().includes(searchLower)
            );
        }
        
        res.json(tasks);
    } catch (error) {
        res.status(500).json({ error: 'Failed to fetch tasks' });
    }
});

// Get dashboard statistics
app.get('/api/dashboard/:userId', async (req, res) => {
    try {
        const { userId } = req.params;
        const tasks = await readTasks();
        
        let userTasks = tasks;
        if (userId !== 'admin') {
            userTasks = tasks.filter(task => 
                task.userId === userId || task.assignedTo === userId
            );
        }
        
        const now = new Date();
        const oneWeekAgo = new Date(now.getTime() - 7 * 24 * 60 * 60 * 1000);
        const oneMonthAgo = new Date(now.getTime() - 30 * 24 * 60 * 60 * 1000);
        
        const stats = {
            total: userTasks.length,
            completed: userTasks.filter(t => t.status === 'Complete').length,
            inProgress: userTasks.filter(t => t.status === 'In Progress').length,
            pending: userTasks.filter(t => t.status === 'Pending').length,
            completedThisWeek: userTasks.filter(t => 
                t.status === 'Complete' && new Date(t.completedAt || t.createdAt) >= oneWeekAgo
            ).length,
            completedThisMonth: userTasks.filter(t => 
                t.status === 'Complete' && new Date(t.completedAt || t.createdAt) >= oneMonthAgo
            ).length,
            overdue: userTasks.filter(t => 
                t.dueDate !== 'No Date' && 
                new Date(t.dueDate) < now && 
                t.status !== 'Complete'
            ).length,
            highPriority: userTasks.filter(t => t.priority === 'High').length
        };
        
        res.json(stats);
    } catch (error) {
        res.status(500).json({ error: 'Failed to fetch dashboard stats' });
    }
});

// Export tasks to CSV
app.get('/api/export/csv', async (req, res) => {
    try {
        const { userId } = req.query;
        let tasks = await readTasks();
        
        if (userId && userId !== 'admin') {
            tasks = tasks.filter(task => 
                task.userId === userId || task.assignedTo === userId
            );
        }
        
        const csv = [
            'Title,Description,Assigned To,Due Date,Status,Priority,Created At',
            ...tasks.map(task => 
                `"${task.title}","${task.description || ''}","${task.assignedTo}","${task.dueDate}","${task.status}","${task.priority}","${task.createdAt}"`
            )
        ].join('\n');
        
        res.setHeader('Content-Type', 'text/csv');
        res.setHeader('Content-Disposition', 'attachment; filename=tasks.csv');
        res.send(csv);
    } catch (error) {
        res.status(500).json({ error: 'Failed to export tasks' });
    }
});

// Get all tasks (original route for backward compatibility)
app.get('/api/tasks', async (req, res) => {
    try {
        const tasks = await readTasks();
        res.json(tasks);
    } catch (error) {
        res.status(500).json({ error: 'Failed to fetch tasks' });
    }
});

// Create new task
app.post('/api/tasks', async (req, res) => {
    try {
        const { title, description, assignedTo, dueDate, status, priority, userId } = req.body;
        
        if (!title) {
            return res.status(400).json({ error: 'Title is required' });
        }

        const tasks = await readTasks();
        const newTask = {
            id: generateId(),
            title,
            description: description || '',
            assignedTo: assignedTo || 'Unassigned',
            dueDate: dueDate || 'No Date',
            status: status || 'To Do',
            priority: priority || 'Medium',
            userId: userId || 'anonymous',
            createdAt: new Date().toISOString(),
            completedAt: status === 'Completed' ? new Date().toISOString() : null
        };

        tasks.push(newTask);
        const success = await writeTasks(tasks);
        
        if (success) {
            res.status(201).json(newTask);
        } else {
            res.status(500).json({ error: 'Failed to save task' });
        }
    } catch (error) {
        res.status(500).json({ error: 'Failed to create task' });
    }
});

// Update task
app.put('/api/tasks/:id', async (req, res) => {
    try {
        const { id } = req.params;
        const updates = req.body;
        
        const tasks = await readTasks();
        const taskIndex = tasks.findIndex(task => task.id === id);
        
        if (taskIndex === -1) {
            return res.status(404).json({ error: 'Task not found' });
        }
        
        // If status is being updated to Complete, add completedAt timestamp
        if (updates.status === 'Complete' && tasks[taskIndex].status !== 'Complete') {
            updates.completedAt = new Date().toISOString();
        } else if (updates.status !== 'Complete') {
            updates.completedAt = null;
        }
        
        tasks[taskIndex] = { ...tasks[taskIndex], ...updates };
        const success = await writeTasks(tasks);
        
        if (success) {
            res.json(tasks[taskIndex]);
        } else {
            res.status(500).json({ error: 'Failed to update task' });
        }
    } catch (error) {
        res.status(500).json({ error: 'Failed to update task' });
    }
});

// Delete task
app.delete('/api/tasks/:id', async (req, res) => {
    try {
        const { id } = req.params;
        
        const tasks = await readTasks();
        const filteredTasks = tasks.filter(task => task.id !== id);
        
        if (tasks.length === filteredTasks.length) {
            return res.status(404).json({ error: 'Task not found' });
        }
        
        const success = await writeTasks(filteredTasks);
        
        if (success) {
            res.json({ message: 'Task deleted successfully' });
        } else {
            res.status(500).json({ error: 'Failed to delete task' });
        }
    } catch (error) {
        res.status(500).json({ error: 'Failed to delete task' });
    }
});

// Start server
async function startServer() {
    await initializeDataFiles();
    app.listen(PORT, () => {
        console.log(`WorkSphere server running on http://localhost:${PORT}`);
        console.log(`Data stored in: ${DATA_FILE}`);
        console.log(`Users stored in: ${USERS_FILE}`);
        console.log('Default login: admin/admin123 or user1/user123');
    });
}

startServer();