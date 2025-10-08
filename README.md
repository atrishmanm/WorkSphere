# 🌟 WorkSphere - Advanced Task Management System

<div align="center">
  <img src="https://img.shields.io/badge/Node.js-339933?style=for-the-badge&logo=nodedotjs&logoColor=white" alt="Node.js">
  <img src="https://img.shields.io/badge/Express.js-000000### Sta## 🎮 Detailed FeaturesUser Account
- **Username**: `user`
- **Password**: `user123`
- **Access Level**: Personal task management
- **Capabilities**:
  - Create and manage personal tasks
  - View assigned tasks
  - Access personal analytics
  - Export personal reports

## 🎯 What's Next? (First Steps Guide)

### **Just logged in? Here's what to do:**

#### **1. Explore the Dashboard (30 seconds)**
- Check out the **analytics charts** showing task distribution
- Notice the **task statistics** at the top (Total, Completed, Overdue)
- See the **modern glassmorphism design** in action

#### **2. Create Your First Task (2 minutes)**
- Click **"Add Task"** button (top-right corner)
- Fill in: Title, Description, Priority, Due Date
- Click **"Create Task"** and watch it appear!

#### **3. Try the Kanban Board (1 minute)**
- Click **"Board View"** toggle button
- **Drag and drop** tasks between columns:
  - 📝 **To Do** → 🔄 **In Progress** → ✅ **Done**
- Switch back to **"List View"** to see the table format

#### **4. Test the Filtering (1 minute)**
- Use the **search box** to find tasks
- Try **priority filters** (High, Medium, Low)
- Filter by **status** (To Do, In Progress, Done)

#### **5. Export Your Data (30 seconds)**
- Click **"Export PDF"** to generate a professional report
- See how it includes **assigned user information**

### **🎉 Congratulations!** 
You've just experienced all the major features of WorkSphere! Ready to manage your real projects?

---

## 📡 API Documentationthe-badge&logo=express&logoColor=white" alt="Express.js">
  <img src="https://img.shields.io/badge/Tailwind_CSS-38B2AC?style=for-the-badge&logo=tailwind-css&logoColor=white" alt="Tailwind CSS">
  <img src="https://img.shields.io/badge/Chart.js-FF6384?style=for-the-badge&logo=chartdotjs&logoColor=white" alt="Chart.js">
  <img src="https://img.shields.io/badge/JavaScript-F7DF1E?style=for-the-badge&logo=javascript&logoColor=black" alt="JavaScript">
</div>

<div align="center">
  <h3>Where Ideas Take Flight, Tasks Find Their Purpose</h3>
  <p><em>✨ Elevate your productivity to new heights</em></p>
</div>

---

A comprehensive, full-featured task management system built with modern web technologies. WorkSphere combines powerful functionality with an elegant, glassmorphism-inspired UI design, featuring role-based authentication, interactive kanban boards, real-time analytics, and advanced task management capabilities—all running locally without external dependencies.

## 🎯 Table of Contents

- [Quick Setup & Installation](#-quick-setup--first-time-installation)
- [Key Features](#-key-features)
- [Tech Stack](#-tech-stack)
- [Detailed Features](#-detailed-features)
- [API Documentation](#-api-documentation)
- [File Structure](#-file-structure)
- [Demo Credentials](#-demo-credentials)
- [Contributing](#-contributing)

## 🚀 Quick Setup & First-Time Installation

### **🎯 Super Easy Setup (Recommended)**

#### **Windows Users - One-Click Start:**
1. **Download/Clone** the WorkSphere folder to your computer
2. **Find** the `start.bat` file in the main folder
3. **Double-click** `start.bat` file
4. **That's it!** The batch script will automatically:
   - ✅ Check if Node.js is installed (shows download link if needed)
   - ✅ Install all required dependencies 
   - ✅ Start the WorkSphere server
   - ✅ Open your browser to the application
   - ✅ Display login credentials on screen

> **💡 First time?** The batch file will guide you through everything!

#### **Manual Setup (All Platforms):**

**Prerequisites:** [Node.js](https://nodejs.org/) (Download the LTS version)

```bash
# 1. Get WorkSphere
git clone https://github.com/yourusername/worksphere.git
cd worksphere

# 2. Install and start (one command)
npm install express cors && node server.js
```

**✅ Success!** Open browser to: `http://localhost:3000`

### **🔑 Login Credentials**
- **Admin Access:** `admin` / `admin` (full system control)
- **User Access:** `user` / `user123` (personal tasks)

### **⚡ Alternative Quick Commands**

```bash
# For experienced developers
git clone [repo] && cd worksphere && npm install express cors && node server.js

# Just start server (after setup)
node server.js

# Install dependencies only
npm install express cors
```

### **� Troubleshooting**

**Node.js not installed?**
- Download from [nodejs.org](https://nodejs.org/)
- Choose LTS version → Install → Restart terminal

**Port 3000 busy?**
```bash
# Windows: Kill existing processes
taskkill /F /IM node.exe
netstat -ano | findstr :3000

# Alternative: Use different port
# Edit server.js: change port 3000 to 3001
```

**Can't access localhost:3000?**
- Try: `http://127.0.0.1:3000`
- Check Windows Firewall settings
- Make sure you see "Server running on http://localhost:3000" message

---

## ✨ Key Features

### 🔐 **Authentication & User Management**
- **Role-based Access Control** - Admin and User roles with different permissions
- **Secure Login System** - Session management with modern glassmorphism UI
- **Password Visibility Toggle** - Enhanced user experience
- **Automatic Session Handling** - Seamless login/logout functionality

### 📊 **Dashboard & Analytics**
- **Real-time Analytics** - Interactive charts powered by Chart.js
- **Visual Data Representation** - Pie charts, bar charts, and progress indicators
- **Comprehensive Metrics** - Task completion rates, priority distribution, overdue alerts
- **Centered Chart Layout** - Professional dashboard presentation

### 📋 **Advanced Task Management**
- **Complete CRUD Operations** - Create, Read, Update, Delete with full validation
- **Rich Task Properties** - Title, description, priority, due dates, assignments
- **Smart Filtering System** - Multiple filter combinations with real-time search
- **Priority Levels** - Low, Medium, High, Critical with visual color coding
- **Status Tracking** - To Do, In Progress, Done with progress indicators

### 🎯 **Interactive Kanban Board**
- **Trello-style Layout** - Three-column visual task management
- **Drag & Drop Functionality** - Intuitive task movement between columns
- **Real-time Updates** - Automatic synchronization across all views
- **Visual Task Cards** - Color-coded by priority with complete information

### 🔄 **Multiple View Modes**
- **List View** - Traditional table format with sorting capabilities
- **Board View** - Visual kanban-style management
- **Seamless View Switching** - Toggle between views instantly
- **View State Persistence** - Remembers user preferences

### 📄 **Export & Reporting**
- **PDF Export** - Professional task reports with assigned user details
- **CSV Export** - Data export for external analysis
- **Comprehensive Reports** - All task information and statistics included

## � Tech Stack

### Backend
- **Node.js** - Runtime environment
- **Express.js** - Web application framework  
- **File System (fs)** - JSON-based data persistence
- **CORS** - Cross-origin resource sharing

### Frontend
- **Vanilla JavaScript** - Core functionality and DOM manipulation
- **Tailwind CSS** - Utility-first CSS framework
- **Chart.js** - Interactive data visualization
- **jsPDF** - Client-side PDF generation
- **HTML5 & CSS3** - Modern web standards

### Design & UX
- **Glassmorphism** - Modern UI design with backdrop blur effects
- **Responsive Layout** - Mobile-first design approach
- **CSS Animations** - Smooth transitions and micro-interactions
- **Google Fonts** - Inter typography for professional appearance

## 🚀 Quick Start

> **Already have Node.js?** Skip to [Installation Steps](#installation-steps)  
> **New to development?** Check our [First-Time Setup](#-first-time-setup) guide above!

### Prerequisites
- **Node.js** (v14 or higher) - [Download here](https://nodejs.org/)
- **npm** (comes with Node.js)
- **Web browser** (Chrome, Firefox, Safari, Edge)

### Installation Steps

1. **Get the code**
   ```bash
   # Option A: Clone with Git
   git clone https://github.com/yourusername/worksphere.git
   cd worksphere
   
   # Option B: Download ZIP and extract, then:
   cd path/to/extracted/worksphere
   ```

2. **Install required packages**
   ```bash
   npm install express cors
   ```

3. **Start the application**
   ```bash
   node server.js
   ```
   
   **✅ Success!** You should see:
   ```
   Server running on http://localhost:3000
   ```

4. **Open in your browser**
   - Go to: `http://localhost:3000`
   - Login with: **admin** / **admin** or **user** / **user123**

### ⚡ One-Line Setup (For Experienced Users)

```bash
# Clone, install, and start in one command
git clone https://github.com/yourusername/worksphere.git && cd worksphere && npm install express cors && node server.js
```

### 🛑 Troubleshooting Quick Fixes

**Server won't start?**
```bash
# Check if Node.js is installed
node --version

# Check if port 3000 is busy (Windows)
netstat -ano | findstr :3000

# Kill any existing Node processes (Windows)
taskkill /F /IM node.exe
```

**Can't access the site?**
- Make sure you see "Server running on http://localhost:3000" message
- Try `http://127.0.0.1:3000` instead
- Check Windows Firewall isn't blocking the connection

## � Detailed Features

### 🔐 Authentication System
- **Modern Login Interface** - Glassmorphism design with animated background
- **Role-based Access** - Separate permissions for Admin and User roles
- **Session Management** - Persistent login state with localStorage
- **Security Features** - Input validation and error handling

### 📊 Dashboard Analytics
- **Task Distribution Charts** - Visual breakdown by status using pie charts
- **Priority Analysis** - Bar charts showing task priority distribution
- **Progress Tracking** - Real-time completion statistics
- **Performance Metrics** - Weekly/monthly progress indicators
- **Overdue Alerts** - Visual warnings for tasks past due date

### 📋 Task Management System
- **Rich Task Creation** - Comprehensive form with all necessary fields
- **Smart Assignments** - Assign tasks to team members with user selection
- **Priority System** - Four levels (Low, Medium, High, Critical) with color coding
- **Status Workflow** - Three-stage process (To Do → In Progress → Done)
- **Due Date Management** - Calendar integration with overdue detection
- **Bulk Operations** - Multiple task selection and batch actions

### 🎯 Kanban Board Features
- **Visual Task Management** - Three-column layout inspired by Trello
- **Drag & Drop Interface** - Smooth task movement between columns
- **Real-time Synchronization** - Instant updates across all views
- **Color-coded Cards** - Priority-based visual distinction
- **Task Details on Cards** - Essential information display
- **Column Statistics** - Task count per column

### 🔍 Advanced Filtering & Search
- **Multi-parameter Filtering** - Combine status, priority, and user filters
- **Real-time Search** - Instant results as you type
- **Filter Persistence** - Maintains filter state between sessions
- **Clear Filter Options** - Easy reset functionality
- **Search Highlighting** - Visual emphasis on matching terms

### 📄 Export & Reporting
- **PDF Report Generation** - Professional formatting with jsPDF
- **Comprehensive Data Export** - All task details and user information
- **CSV Export** - Compatible with Excel and Google Sheets
- **Custom Report Filtering** - Export only selected data
- **Print-friendly Format** - Optimized for physical documents

## 🔑 Demo Credentials

### Administrator Account
- **Username**: `admin`
- **Password**: `admin`
- **Access Level**: Full system control
- **Capabilities**: 
  - Manage all users and tasks
  - View system-wide analytics
  - Export comprehensive reports
  - User management functions

### Standard User Account
- **Username**: `user`
- **Password**: `user123`
- **Access Level**: Personal task management
- **Capabilities**:
  - Create and manage personal tasks
  - View assigned tasks
  - Access personal analytics
  - Export personal reports

## � API Documentation

### Authentication Endpoints
```http
POST /api/login
Content-Type: application/json
Body: { "username": "string", "password": "string" }
Response: { "success": boolean, "user": object, "message": "string" }
```

### Task Management Endpoints
```http
GET /api/tasks
Query Parameters: ?status=string&priority=string&assignedTo=string&search=string
Response: Array of task objects

POST /api/tasks
Content-Type: application/json
Body: { "title": "string", "description": "string", "priority": "string", "status": "string", "dueDate": "string", "assignedTo": "string" }
Response: Created task object

PUT /api/tasks/:id
Content-Type: application/json
Body: Task update object
Response: Updated task object

DELETE /api/tasks/:id
Response: { "success": boolean, "message": "string" }
```

### User Management Endpoints
```http
GET /api/users
Response: Array of user objects (admin only)
```

### Data Models

#### Task Object Structure
```json
{
  "id": "unique-uuid",
  "title": "Task Title",
  "description": "Detailed task description",
  "priority": "Low|Medium|High|Critical", 
  "status": "To Do|In Progress|Done",
  "dueDate": "YYYY-MM-DD",
  "assignedTo": "user-id",
  "createdBy": "user-id",
  "createdAt": "2025-10-08T12:00:00.000Z",
  "updatedAt": "2025-10-08T12:00:00.000Z"
}
```

#### User Object Structure
```json
{
  "id": "unique-uuid",
  "username": "string",
  "password": "string",
  "role": "admin|user",
  "name": "Display Name",
  "email": "user@example.com",
  "createdAt": "2025-10-08T12:00:00.000Z"
}
```

## � File Structure

```
WorkSphere/
├── 📄 server.js              # Express server & RESTful API
├── 🌐 login.html             # Authentication interface
├── 🌐 dashboard.html         # Main application interface  
├── 📁 js/
│   └── 📄 dashboard.js       # Frontend logic & API calls
├── 📄 tasks.json             # Task database (JSON)
├── 📄 users.json             # User database (JSON)
├── 📄 package.json           # Project dependencies
└── � README.md              # Documentation
```

### Key Files Description

- **server.js** - Node.js/Express backend with full API implementation
- **login.html** - Modern glassmorphism login interface with animations
- **dashboard.html** - Main app interface with kanban board and analytics
- **dashboard.js** - Frontend JavaScript handling all user interactions
- **tasks.json** - File-based task storage with full CRUD support
- **users.json** - User accounts with role-based access control

## 🎨 Customization Guide

### Adding New Users
Edit `users.json` and restart the server:
```json
{
  "id": "newuser-uuid",
  "username": "newuser",
  "password": "securepassword", 
  "role": "user",
  "name": "New User Name",
  "email": "user@example.com",
  "createdAt": "2025-10-08T12:00:00.000Z"
}
```

### Customizing Priority Colors
Modify CSS classes in `dashboard.html`:
```css
.priority-critical { border-left: 4px solid #dc2626; }  /* Red */
.priority-high { border-left: 4px solid #ea580c; }      /* Orange */
.priority-medium { border-left: 4px solid #ca8a04; }    /* Yellow */
.priority-low { border-left: 4px solid #16a34a; }       /* Green */
```

### Modifying Chart Colors
Update Chart.js configuration in `dashboard.js`:
```javascript
backgroundColor: ['#3b82f6', '#10b981', '#f59e0b', '#ef4444']
```

## 🌟 Key Highlights

- ✅ **Zero External Dependencies** - Runs completely locally
- ✅ **Modern Architecture** - Clean, scalable code structure  
- ✅ **Production Ready** - Robust error handling and validation
- ✅ **Mobile Responsive** - Works perfectly on all screen sizes
- ✅ **Intuitive UX** - Drag-and-drop kanban functionality
- ✅ **Extensible Design** - Easy to add new features
- ✅ **Well Documented** - Comprehensive code comments and API docs

## 🤝 Contributing

We welcome contributions! Here's how to get started:

### Development Setup
1. **Fork the repository**
2. **Create feature branch** (`git checkout -b feature/AmazingFeature`)
3. **Install dependencies** (`npm install`)
4. **Start development server** (`node server.js`)
5. **Make your changes** and test thoroughly
6. **Commit changes** (`git commit -m 'Add AmazingFeature'`)
7. **Push to branch** (`git push origin feature/AmazingFeature`)
8. **Open Pull Request**

### Contribution Guidelines
- Follow existing code style and conventions
- Add comprehensive comments for complex functionality
- Test all features before submitting
- Update documentation for new features
- Ensure mobile responsiveness

### Areas for Contribution
- 🔧 Backend API enhancements
- 🎨 UI/UX improvements
- 📱 Mobile optimization
- 🔒 Security enhancements
- 📊 Additional chart types
- 🌐 Internationalization

## 🔮 Roadmap & Future Enhancements

### Short Term (v2.0)
- [ ] **Real-time Collaboration** - WebSocket integration for live updates
- [ ] **Advanced Notifications** - Email/browser notifications for due dates
- [ ] **Task Templates** - Predefined task structures for common workflows
- [ ] **Time Tracking** - Built-in time logging for tasks
- [ ] **Advanced Search** - Full-text search with filters and operators

### Medium Term (v3.0)
- [ ] **Database Integration** - PostgreSQL/MongoDB support
- [ ] **Team Management** - Multi-team organization and permissions
- [ ] **Custom Fields** - User-defined task properties
- [ ] **Automation Rules** - Trigger-based task automation
- [ ] **Integration APIs** - Connect with popular productivity tools

### Long Term (v4.0+)
- [ ] **Mobile Applications** - iOS and Android native apps
- [ ] **Advanced Analytics** - Machine learning insights and predictions
- [ ] **Enterprise Features** - SSO, audit logs, compliance features
- [ ] **Plugin System** - Third-party extension support
- [ ] **Multi-language Support** - Internationalization framework

## � Security & Privacy

### Current Implementation
- **Local Storage Only** - All data stays on your machine
- **No External Calls** - Zero data transmission to external servers
- **Session-based Auth** - Simple localStorage session management
- **Input Validation** - Frontend and backend validation

### Security Best Practices
For production deployment:
- Implement **password hashing** (bcrypt)
- Use **JWT tokens** for authentication
- Add **HTTPS/SSL** certificates
- Implement **rate limiting** 
- Add **CSRF protection**
- Use **environment variables** for sensitive data

## 📞 Support & Community

### Getting Help
- 📖 **Documentation** - Comprehensive guides and API docs
- � **Bug Reports** - Use GitHub issues for bug reports
- 💡 **Feature Requests** - Suggest new features via GitHub discussions
- 💬 **Community** - Join discussions and share your experience

### Troubleshooting
Common issues and solutions:

**Server won't start:**
- Ensure Node.js is installed (`node --version`)
- Check if port 3000 is available (`netstat -ano | findstr :3000`)
- Verify all dependencies are installed (`npm install`)

**Login fails:**
- Check browser console for errors
- Verify `users.json` file exists and is valid JSON
- Ensure server is running on correct port

---
Edited by Divyanshu Kant


**Tasks not loading:**
- Check `tasks.json` file permissions and validity
- Verify API endpoints are responding (`http://localhost:3000/api/tasks`)
- Check browser network tab for failed requests

---

<div align="center">
  <h3>🌟 Star this repository if you found it helpful! 🌟</h3>
  <p>Made with ❤️ for the developer community</p>
  <br>
  <strong>WorkSphere</strong> - <em>Where Ideas Take Flight, Tasks Find Their Purpose</em>
  <br><br>
  <sub>Professional Task Management • Modern UI/UX • Zero Dependencies</sub>
</div>
