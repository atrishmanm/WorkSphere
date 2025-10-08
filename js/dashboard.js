// Dashboard JavaScript

// Global variables
let user = null;
let allUsers = [];
let allTasks = [];
let dashboardStats = {};
let statusChart = null;
let priorityChart = null;
let currentView = 'kanban'; // 'list' or 'kanban' - default to kanban view

// API Request utility function
async function apiRequest(url, method = 'GET', data = null) {
    const config = {
        method,
        headers: {
            'Content-Type': 'application/json',
        }
    };
    
    if (data) {
        config.body = JSON.stringify(data);
    }
    
    const response = await fetch(url, config);
    
    if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
    }
    
    return response.json();
}

// Initialize dashboard
document.addEventListener('DOMContentLoaded', async () => {
    console.log('Dashboard loading...');
    
    // Load user from localStorage
    try {
        const userData = localStorage.getItem('currentUser');
        console.log('Raw user data from localStorage:', userData);
        
        if (userData) {
            user = JSON.parse(userData);
            console.log('Parsed user data:', user);
        }
    } catch (error) {
        console.error('Error parsing user data:', error);
    }
    
    if (!user) {
        console.log('No user found, redirecting to login');
        window.location.href = '/login.html';
        return;
    }
    
    console.log('User found:', user);
    
    // Set user info in UI
    document.getElementById('userName').textContent = user.name;
    document.getElementById('userRole').textContent = user.role.toUpperCase();
    
    // Show admin filters if user is admin
    if (user.role === 'admin') {
        const assignedFilter = document.getElementById('assignedFilter');
        if (assignedFilter) {
            assignedFilter.classList.remove('hidden');
        }
    }
    
    // Load initial data
    console.log('Loading users...');
    await loadUsers();
    console.log('Loading dashboard stats...');
    await loadDashboardStats();
    console.log('Loading tasks...');
    await loadTasks();
    
    // Initialize charts
    initializeCharts();
    
    // Load all tasks for chart data (admin sees all, users see their tasks)
    await loadAllTasksForCharts();
    
    // Set up event listeners
    setupEventListeners();
    
    // Initialize view - show kanban board by default
    initializeDefaultView();
});

// Setup event listeners
function setupEventListeners() {
    // Search and filter events
    const searchInput = document.getElementById('searchInput');
    const statusFilter = document.getElementById('statusFilter');
    const priorityFilter = document.getElementById('priorityFilter');
    const assignedToFilter = document.getElementById('assignedToFilter');
    
    if (searchInput) searchInput.addEventListener('input', debounce(filterTasks, 300));
    if (statusFilter) statusFilter.addEventListener('change', filterTasks);
    if (priorityFilter) priorityFilter.addEventListener('change', filterTasks);
    if (assignedToFilter) assignedToFilter.addEventListener('change', filterTasks);
    
    // Date range filters
    const startDateFilter = document.getElementById('startDateFilter');
    const endDateFilter = document.getElementById('endDateFilter');
    if (startDateFilter) startDateFilter.addEventListener('change', filterTasks);
    if (endDateFilter) endDateFilter.addEventListener('change', filterTasks);
    
    // Show admin features if user is admin
    if (user && user.role === 'admin') {
        const userMgmtBtn = document.querySelector('[onclick="openUserManagement()"]');
        if (userMgmtBtn) {
            userMgmtBtn.classList.remove('hidden');
        }
    }
}

// Debounce function for search
function debounce(func, wait) {
    let timeout;
    return function executedFunction(...args) {
        const later = () => {
            clearTimeout(timeout);
            func(...args);
        };
        clearTimeout(timeout);
        timeout = setTimeout(later, wait);
    };
}

// Load users
async function loadUsers() {
    try {
        allUsers = await apiRequest('/api/users');
        
        // Populate assign dropdowns
        const assignSelects = ['taskAssigned', 'editTaskAssigned', 'assignedToFilter'];
        assignSelects.forEach(selectId => {
            const select = document.getElementById(selectId);
            if (select) {
                // Clear existing options except first
                while (select.children.length > 1) {
                    select.removeChild(select.lastChild);
                }
                
                allUsers.forEach(user => {
                    const option = document.createElement('option');
                    option.value = user.id;
                    option.textContent = user.name;
                    select.appendChild(option);
                });
            }
        });
    } catch (error) {
        console.error('Failed to load users:', error);
    }
}

// Load dashboard statistics
async function loadDashboardStats() {
    try {
        dashboardStats = await apiRequest(`/api/dashboard/${user.id}`);
        
        // Update stats in UI
        document.getElementById('totalTasks').textContent = dashboardStats.total || 0;
        document.getElementById('completedTasks').textContent = dashboardStats.completed || 0;
        document.getElementById('weeklyTasks').textContent = dashboardStats.completedThisWeek || 0;
        document.getElementById('overdueTasks').textContent = dashboardStats.overdue || 0;
    } catch (error) {
        console.error('Failed to load dashboard stats:', error);
    }
}

// Load and filter tasks
async function loadTasks() {
    try {
        const params = new URLSearchParams();
        
        // Add user filter for non-admin users
        if (user.role !== 'admin') {
            params.append('userId', user.id);
        }
        
        // Add user role for server-side filtering
        params.append('userRole', user.role);
        
        const tasks = await apiRequest(`/api/tasks?${params}`);
        allTasks = tasks; // Store for charts and kanban
        currentTasks = tasks;
        renderTasks(tasks);
    } catch (error) {
        console.error('Failed to load tasks:', error);
        displayMessage('Failed to load tasks', 'error');
    }
}

// Load all tasks for chart data (shows all tasks for admin, user's tasks for regular users)
async function loadAllTasksForCharts() {
    try {
        const params = new URLSearchParams();
        
        // For admin, get all tasks for charts
        if (user.role === 'admin') {
            // Don't filter by userId for admin charts - show all tasks
            params.append('userRole', user.role);
        } else {
            // For regular users, show their tasks in charts
            params.append('userId', user.id);
            params.append('userRole', user.role);
        }
        
        const allTasksForCharts = await apiRequest(`/api/tasks?${params}`);
        
        // Update charts with all tasks data
        if (allTasksForCharts && allTasksForCharts.length > 0) {
            // Calculate status distribution for all tasks
            const statusCounts = {
                'To Do': allTasksForCharts.filter(task => task.status === 'To Do').length,
                'In Progress': allTasksForCharts.filter(task => task.status === 'In Progress').length,
                'Completed': allTasksForCharts.filter(task => task.status === 'Completed').length
            };

            // Calculate priority distribution for all tasks
            const priorityCounts = {
                'High': allTasksForCharts.filter(task => task.priority === 'High').length,
                'Medium': allTasksForCharts.filter(task => task.priority === 'Medium').length,
                'Low': allTasksForCharts.filter(task => task.priority === 'Low').length
            };

            // Update charts if they exist
            if (statusChart && priorityChart) {
                statusChart.data.datasets[0].data = [statusCounts['To Do'], statusCounts['In Progress'], statusCounts['Completed']];
                priorityChart.data.datasets[0].data = [priorityCounts['High'], priorityCounts['Medium'], priorityCounts['Low']];
                
                statusChart.update();
                priorityChart.update();
            }
        }
    } catch (error) {
        console.error('Failed to load tasks for charts:', error);
    }
}

// Filter tasks based on current filter values
async function filterTasks() {
    try {
        const params = new URLSearchParams();
        
        // Add user filter for non-admin users
        if (user.role !== 'admin') {
            params.append('userId', user.id);
        }
        
        // Add user role for server-side filtering
        params.append('userRole', user.role);
        
        // Add search filter
        const search = document.getElementById('searchInput').value.trim();
        if (search) {
            params.append('search', search);
        }
        
        // Add status filter
        const status = document.getElementById('statusFilter').value;
        if (status) {
            params.append('status', status);
        }
        
        // Add priority filter
        const priority = document.getElementById('priorityFilter').value;
        if (priority) {
            params.append('priority', priority);
        }
        
        // Add assigned user filter (admin only)
        const assignedTo = document.getElementById('assignedToFilter')?.value;
        if (assignedTo && user.role === 'admin') {
            params.append('assignedTo', assignedTo);
        }
        
        // Add date range filters
        const startDate = document.getElementById('startDateFilter')?.value;
        const endDate = document.getElementById('endDateFilter')?.value;
        if (startDate) {
            params.append('startDate', startDate);
        }
        if (endDate) {
            params.append('endDate', endDate);
        }
        
        const tasks = await apiRequest(`/api/tasks?${params}`);
        allTasks = tasks; // Store for charts and kanban
        renderTasks(tasks);
    } catch (error) {
        console.error('Failed to filter tasks:', error);
    }
}

// Add new task
async function addTask() {
    const title = document.getElementById('taskTitle').value.trim();
    const description = document.getElementById('taskDescription').value.trim();
    const assignedTo = document.getElementById('taskAssigned').value;
    const dueDate = document.getElementById('taskDueDate').value;
    const status = document.getElementById('taskStatus').value;
    const priority = document.getElementById('taskPriority').value;

    if (!title) {
        displayMessage("Task title cannot be empty.", 'error');
        return;
    }

    const taskData = {
        title,
        description,
        assignedTo: assignedTo || user.id,
        dueDate: dueDate || 'No Date',
        status,
        priority,
        userId: user.id
    };

    try {
        await apiRequest('/api/tasks', 'POST', taskData);
        
        displayMessage("Task added successfully.", 'success');
        
        // Clear form
        document.getElementById('taskTitle').value = '';
        document.getElementById('taskDescription').value = '';
        document.getElementById('taskAssigned').value = '';
        document.getElementById('taskDueDate').value = '';
        document.getElementById('taskStatus').value = 'To Do';
        document.getElementById('taskPriority').value = 'Medium';
        
        // Reload data
        await loadDashboardStats();
        await filterTasks();
    } catch (error) {
        displayMessage("Error adding task.", 'error');
        console.error('Error adding task:', error);
    }
}

// Update task status
async function updateTaskStatus(taskId, newStatus) {
    try {
        await apiRequest(`/api/tasks/${taskId}`, 'PUT', { status: newStatus });
        
        displayMessage(`Task status updated to ${newStatus}.`, 'success');
        await loadDashboardStats();
        await filterTasks();
    } catch (error) {
        displayMessage("Error updating task status.", 'error');
        console.error('Error updating task status:', error);
    }
}

// Open edit modal
function openEditModal(taskId) {
    const task = currentTasks.find(t => t.id === taskId);
    if (!task) {
        displayMessage("Task not found.", 'error');
        return;
    }

    document.getElementById('editTaskTitle').value = task.title;
    document.getElementById('editTaskDescription').value = task.description || '';
    document.getElementById('editTaskAssigned').value = task.assignedTo === 'Unassigned' ? '' : task.assignedTo;
    document.getElementById('editTaskDueDate').value = task.dueDate === 'No Date' ? '' : task.dueDate;
    document.getElementById('editTaskPriority').value = task.priority;
    
    const saveBtn = document.getElementById('editSaveBtn');
    saveBtn.onclick = () => editTask(taskId);

    document.getElementById('editModal').classList.remove('hidden');
}

// Close edit modal
function closeEditModal() {
    document.getElementById('editModal').classList.add('hidden');
}

// Edit task
async function editTask(taskId) {
    const title = document.getElementById('editTaskTitle').value.trim();
    const description = document.getElementById('editTaskDescription').value.trim();
    const assignedTo = document.getElementById('editTaskAssigned').value;
    const dueDate = document.getElementById('editTaskDueDate').value;
    const priority = document.getElementById('editTaskPriority').value;

    if (!title) {
        displayMessage("Task title cannot be empty.", 'error');
        return;
    }

    const updatedData = {
        title,
        description,
        assignedTo: assignedTo || 'Unassigned',
        dueDate: dueDate || 'No Date',
        priority
    };

    try {
        await apiRequest(`/api/tasks/${taskId}`, 'PUT', updatedData);
        
        closeEditModal();
        displayMessage("Task updated successfully.", 'success');
        await loadDashboardStats();
        await filterTasks();
    } catch (error) {
        displayMessage("Error updating task details.", 'error');
        console.error('Error updating task details:', error);
    }
}

// Delete task
async function deleteTask(taskId) {
    showCustomConfirmation('Are you sure you want to delete this task?', async () => {
        try {
            await apiRequest(`/api/tasks/${taskId}`, 'DELETE');
            
            displayMessage("Task deleted successfully.", 'success');
            await loadDashboardStats();
            await filterTasks();
        } catch (error) {
            displayMessage("Error deleting task.", 'error');
            console.error('Error deleting task:', error);
        }
    });
}

// Get priority color class
function getPriorityColor(priority) {
    switch (priority) {
        case 'High': return 'bg-red-100 text-red-800 border-red-300';
        case 'Medium': return 'bg-yellow-100 text-yellow-800 border-yellow-300';
        case 'Low': return 'bg-green-100 text-green-800 border-green-300';
        default: return 'bg-gray-100 text-gray-800 border-gray-300';
    }
}

// Get status color class
function getStatusColor(status) {
    switch (status) {
        case 'Complete': return 'bg-green-100 text-green-800 border-green-300';
        case 'In Progress': return 'bg-blue-100 text-blue-800 border-blue-300';
        case 'Pending': return 'bg-red-100 text-red-800 border-red-300';
        default: return 'bg-gray-100 text-gray-800 border-gray-300';
    }
}

// Get user name by ID
function getUserName(userId) {
    const foundUser = allUsers.find(u => u.id === userId);
    return foundUser ? foundUser.name : userId;
}

// Render tasks
function renderTasks(tasks) {
    const container = document.getElementById('tasksContainer');
    container.innerHTML = '';
    
    if (tasks.length === 0) {
        container.innerHTML = '<p class="text-center text-gray-500 py-8">No tasks found. Try adjusting your filters or add a new task!</p>';
        return;
    }
    
    // Sort tasks by priority and status
    tasks.sort((a, b) => {
        const priorityOrder = { 'High': 3, 'Medium': 2, 'Low': 1 };
        const statusOrder = { 'To Do': 3, 'In Progress': 2, 'Completed': 1 };
        
        if (priorityOrder[a.priority] !== priorityOrder[b.priority]) {
            return priorityOrder[b.priority] - priorityOrder[a.priority];
        }
        
        return statusOrder[b.status] - statusOrder[a.status];
    });

    const tasksGrid = document.createElement('div');
    tasksGrid.className = 'grid grid-cols-1 lg:grid-cols-2 gap-6';

    tasks.forEach(task => {
        const isCreator = task.userId === user.id;
        const canEdit = isCreator || user.role === 'admin';
        const statusColor = getStatusColor(task.status);
        const priorityColor = getPriorityColor(task.priority);
        const priorityClass = `priority-${task.priority.toLowerCase()}`;
        
        const taskCard = document.createElement('div');
        taskCard.className = `p-6 border rounded-xl shadow-lg transition duration-200 ease-in-out transform hover:scale-[1.02] ${priorityClass} ${task.status === 'Complete' ? 'bg-green-50 border-green-200' : 'bg-white border-gray-200'}`;
        
        // Status change buttons
        const statusOptions = ['Pending', 'In Progress', 'Complete'].filter(s => s !== task.status);
        const statusButtons = statusOptions.map(status => `
            <button 
                onclick="updateTaskStatus('${task.id}', '${status}')" 
                class="text-xs font-medium px-2 py-1 rounded-full border transition hover:shadow-md ${getStatusColor(status)} hover:opacity-80"
            >
                ${status}
            </button>
        `).join('');

        // Check if task is overdue
        const isOverdue = task.dueDate !== 'No Date' && new Date(task.dueDate) < new Date() && task.status !== 'Complete';

        taskCard.innerHTML = `
            <div class="flex justify-between items-start mb-3">
                <div class="flex-1">
                    <h3 class="text-lg font-bold text-gray-800 mb-1">${task.title}</h3>
                    ${task.description ? `<p class="text-sm text-gray-600 mb-2">${task.description}</p>` : ''}
                </div>
                <div class="flex gap-2">
                    <span class="text-xs font-semibold px-2 py-1 rounded-full ${priorityColor}">${task.priority}</span>
                    <span class="text-xs font-semibold px-2 py-1 rounded-full ${statusColor}">${task.status}</span>
                </div>
            </div>
            
            <div class="space-y-2 mb-4">
                <p class="text-sm text-gray-600">
                    <span class="font-medium">Assigned:</span> ${getUserName(task.assignedTo)}
                </p>
                <p class="text-sm text-gray-600 ${isOverdue ? 'text-red-600 font-semibold' : ''}">
                    <span class="font-medium">Due Date:</span> ${task.dueDate} ${isOverdue ? '(OVERDUE)' : ''}
                </p>
                <p class="text-sm text-gray-600">
                    <span class="font-medium">Created by:</span> ${getUserName(task.userId)}
                </p>
            </div>
            
            <div class="flex flex-wrap gap-2 items-center pt-4 border-t border-gray-100">
                ${statusButtons}
                <div class="ml-auto flex items-center gap-2">
                    ${canEdit ? `
                        <button 
                            onclick="openEditModal('${task.id}')" 
                            class="p-2 text-indigo-500 hover:text-indigo-700 hover:bg-indigo-50 rounded-lg transition"
                            title="Edit Task"
                        >
                            <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4" viewBox="0 0 20 20" fill="currentColor">
                                <path d="M13.586 3.586a2 2 0 112.828 2.828l-.793.793-2.828-2.828.793-.793zm-6.207 7.104l-2.828 2.828a1 1 0 00-.288.399l-.364 1.455a1 1 0 00.737 1.155l1.455-.364a1 1 0 00.399-.288l2.828-2.828-2.586-2.586z" />
                            </svg>
                        </button>
                        <button 
                            onclick="deleteTask('${task.id}')" 
                            class="p-2 text-red-500 hover:text-red-700 hover:bg-red-50 rounded-lg transition"
                            title="Delete Task"
                        >
                            <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4" viewBox="0 0 20 20" fill="currentColor">
                                <path fill-rule="evenodd" d="M9 2a1 1 0 00-.894.553L7.382 4H4a1 1 0 000 2v10a2 2 0 002 2h8a2 2 0 002-2V6a1 1 0 000-2h-3.382l-.724-1.447A1 1 0 0011 2H9zM7 7a1 1 0 011 1v7a1 1 0 11-2 0V8a1 1 0 011-1zm5 0a1 1 0 011 1v7a1 1 0 11-2 0V8a1 1 0 011-1z" clip-rule="evenodd" />
                            </svg>
                        </button>
                    ` : ''}
                </div>
            </div>
        `;
        tasksGrid.appendChild(taskCard);
    });
    
    container.appendChild(tasksGrid);
    
    // Update charts and kanban board
    updateCharts();
    if (currentView === 'kanban') {
        updateKanbanBoard();
    }
}

// Export to CSV
async function exportCSV() {
    try {
        const params = user.role !== 'admin' ? `?userId=${user.id}` : '';
        window.open(`/api/export/csv${params}`, '_blank');
        displayMessage("CSV export started", 'success');
    } catch (error) {
        displayMessage("Export failed", 'error');
    }
}

// Export to PDF
function exportPDF() {
    try {
        const { jsPDF } = window.jspdf;
        const doc = new jsPDF();
        
        // Add title
        doc.setFontSize(20);
        doc.text('WorkSphere Tasks Report', 20, 30);
        
        // Add date
        doc.setFontSize(12);
        doc.text(`Generated on: ${new Date().toLocaleDateString()}`, 20, 45);
        doc.text(`User: ${user.name} (${user.role})`, 20, 55);
        
        // Add statistics
        doc.setFontSize(14);
        doc.text('Statistics:', 20, 75);
        doc.setFontSize(11);
        doc.text(`Total Tasks: ${dashboardStats.total || 0}`, 25, 85);
        doc.text(`Completed: ${dashboardStats.completed || 0}`, 25, 95);
        doc.text(`Completed This Week: ${dashboardStats.completedThisWeek || 0}`, 25, 105);
        doc.text(`Overdue: ${dashboardStats.overdue || 0}`, 25, 115);
        
        // Add tasks
        let yPosition = 135;
        doc.setFontSize(14);
        doc.text('Tasks:', 20, yPosition);
        yPosition += 15;
        
        doc.setFontSize(10);
        currentTasks.forEach((task, index) => {
            if (yPosition > 270) {
                doc.addPage();
                yPosition = 30;
            }
            
            doc.text(`${index + 1}. ${task.title}`, 25, yPosition);
            yPosition += 8;
            doc.text(`   Status: ${task.status} | Priority: ${task.priority} | Due: ${task.dueDate}`, 25, yPosition);
            yPosition += 8;
            doc.text(`   Assigned to: ${getUserName(task.assignedTo)}`, 25, yPosition);
            yPosition += 8;
            if (task.description) {
                doc.text(`   Description: ${task.description.substring(0, 80)}${task.description.length > 80 ? '...' : ''}`, 25, yPosition);
                yPosition += 8;
            }
            yPosition += 5;
        });
        
        doc.save('worksphere-tasks.pdf');
        displayMessage("PDF export completed", 'success');
    } catch (error) {
        displayMessage("PDF export failed", 'error');
        console.error('PDF export error:', error);
    }
}

// Custom confirmation modal
function showCustomConfirmation(message, onConfirm) {
    const modal = document.getElementById('confirmationModal');
    const confirmBtn = document.getElementById('modalConfirmBtn');
    const cancelBtn = document.getElementById('modalCancelBtn');
    const modalMessage = document.getElementById('modalMessage');

    modalMessage.textContent = message;
    modal.classList.remove('hidden');

    const handleConfirm = () => {
        onConfirm();
        modal.classList.add('hidden');
        confirmBtn.removeEventListener('click', handleConfirm);
        cancelBtn.removeEventListener('click', handleCancel);
    };

    const handleCancel = () => {
        modal.classList.add('hidden');
        confirmBtn.removeEventListener('click', handleConfirm);
        cancelBtn.removeEventListener('click', handleCancel);
    };

    confirmBtn.addEventListener('click', handleConfirm);
    cancelBtn.addEventListener('click', handleCancel);
}

// Display status messages
let messageTimeout;
function displayMessage(msg, type, duration = 3000) {
    const msgEl = document.getElementById('statusMessageForm');
    const color = type === 'success' ? 'text-green-600' : 'text-red-600';
    msgEl.innerHTML = `<span class="font-semibold ${color}">${msg}</span>`;
    
    clearTimeout(messageTimeout);
    messageTimeout = setTimeout(() => {
        msgEl.textContent = '';
    }, duration);
}

// Logout function
function logout() {
    localStorage.removeItem('currentUser');
    window.location.href = '/login.html';
}

// Make all functions globally accessible for HTML onclick handlers
window.logout = logout;
window.toggleView = toggleView;
window.switchToView = switchToView;
window.exportCSV = exportCSV;
window.exportPDF = exportPDF;
window.addTask = addTask;
window.closeEditModal = closeEditModal;
window.openUserManagement = openUserManagement;
window.closeUserManagement = closeUserManagement;
window.addUser = addUser;
window.closeEditUser = closeEditUser;
window.openEditModal = openEditModal;
window.editTask = editTask;
window.deleteTask = deleteTask;
window.updateTaskStatus = updateTaskStatus;

// Initialize Charts
function initializeCharts() {
    // Status Chart
    const statusCtx = document.getElementById('statusChart').getContext('2d');
    statusChart = new Chart(statusCtx, {
        type: 'pie',
        data: {
            labels: ['To Do', 'In Progress', 'Completed'],
            datasets: [{
                data: [0, 0, 0],
                backgroundColor: [
                    '#EF4444', // Red for To Do
                    '#F59E0B', // Yellow for In Progress
                    '#10B981'  // Green for Completed
                ],
                borderWidth: 2,
                borderColor: '#ffffff'
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: true,
            plugins: {
                legend: {
                    position: 'bottom',
                    align: 'center'
                },
                title: {
                    display: false
                }
            },
            layout: {
                padding: {
                    top: 10,
                    bottom: 10,
                    left: 10,
                    right: 10
                }
            }
        }
    });

    // Priority Chart
    const priorityCtx = document.getElementById('priorityChart').getContext('2d');
    priorityChart = new Chart(priorityCtx, {
        type: 'bar',
        data: {
            labels: ['High', 'Medium', 'Low'],
            datasets: [{
                label: 'Tasks',
                data: [0, 0, 0],
                backgroundColor: [
                    '#DC2626', // Red for High
                    '#F59E0B', // Yellow for Medium
                    '#059669'  // Green for Low
                ],
                borderWidth: 1,
                borderColor: '#374151'
            }]
        },
        options: {
            responsive: true,
            scales: {
                y: {
                    beginAtZero: true,
                    ticks: {
                        stepSize: 1
                    }
                }
            },
            plugins: {
                legend: {
                    display: false
                },
                title: {
                    display: false
                }
            }
        }
    });
}

// Update charts with current data (for admin, always show all tasks; for users, show their tasks)
function updateCharts() {
    if (!statusChart || !priorityChart) return;

    // For charts, we want to show comprehensive data, not just filtered results
    // So we'll reload all tasks for chart data
    loadAllTasksForCharts();
}

// Switch to specific view
function switchToView(viewType) {
    currentView = viewType;
    
    const listView = document.getElementById('listView');
    const boardView = document.getElementById('boardView');
    const listBtn = document.getElementById('listViewBtn');
    const boardBtn = document.getElementById('boardViewBtn');
    
    if (currentView === 'kanban') {
        // Show kanban board, hide list view
        listView.classList.add('hidden');
        boardView.classList.remove('hidden');
        
        // Update button styles
        listBtn.classList.remove('active');
        listBtn.classList.add('inactive');
        boardBtn.classList.remove('inactive');
        boardBtn.classList.add('active');
        
        updateKanbanBoard();
    } else {
        // Show list view, hide kanban board
        listView.classList.remove('hidden');
        boardView.classList.add('hidden');
        
        // Update button styles
        boardBtn.classList.remove('active');
        boardBtn.classList.add('inactive');
        listBtn.classList.remove('inactive');
        listBtn.classList.add('active');
    }
}

// Legacy function for backward compatibility
function toggleView() {
    const newView = currentView === 'list' ? 'kanban' : 'list';
    switchToView(newView);
}

// Update kanban board with current tasks
function updateKanbanBoard() {
    const todoTasks = allTasks.filter(task => task.status === 'To Do');
    const inProgressTasks = allTasks.filter(task => task.status === 'In Progress');
    const completedTasks = allTasks.filter(task => task.status === 'Completed');

    // Update counters
    document.getElementById('todoCount').textContent = todoTasks.length;
    document.getElementById('progressCount').textContent = inProgressTasks.length;
    document.getElementById('completedCount').textContent = completedTasks.length;

    // Render tasks in each column
    document.getElementById('todoColumn').innerHTML = todoTasks.length ? todoTasks.map(renderKanbanCard).join('') : '<div class="text-center py-8 text-gray-400">No tasks</div>';
    document.getElementById('progressColumn').innerHTML = inProgressTasks.length ? inProgressTasks.map(renderKanbanCard).join('') : '<div class="text-center py-8 text-gray-400">No tasks</div>';
    document.getElementById('completedColumn').innerHTML = completedTasks.length ? completedTasks.map(renderKanbanCard).join('') : '<div class="text-center py-8 text-gray-400">No tasks</div>';
}

// Render a kanban card
function renderKanbanCard(task) {
    const assignedUser = getUserName(task.assignedTo);
    const priorityColor = getPriorityColor(task.priority);
    const dueDate = task.dueDate && task.dueDate !== 'No Date' ? new Date(task.dueDate).toLocaleDateString() : 'No due date';
    const isCreator = task.userId === user.id;
    const canEdit = isCreator || user.role === 'admin';
    
    return `
        <div class="bg-white rounded-lg p-4 shadow-sm border border-gray-200 hover:shadow-md transition-shadow kanban-card" 
             draggable="true" 
             data-task-id="${task.id}"
             ondragstart="dragStart(event, '${task.id}')"
             ondragend="dragEnd(event)">
            <div class="flex justify-between items-start mb-2">
                <h4 class="font-semibold text-gray-800 text-sm line-clamp-2">${task.title}</h4>
                <span class="px-2 py-1 rounded text-xs font-medium ${priorityColor} ml-2 flex-shrink-0">
                    ${task.priority}
                </span>
            </div>
            ${task.description ? `<p class="text-gray-600 text-sm mb-3 line-clamp-3">${task.description}</p>` : ''}
            <div class="flex justify-between items-center text-xs text-gray-500 mb-3">
                <span class="truncate">${assignedUser}</span>
                <span class="flex-shrink-0">${dueDate}</span>
            </div>
            ${canEdit ? `
            <div class="flex justify-end space-x-2 border-t border-gray-100 pt-2">
                <button onclick="openEditModal('${task.id}')" class="text-indigo-600 hover:text-indigo-800 p-1 rounded hover:bg-indigo-50" title="Edit Task">
                    <svg class="w-4 h-4" fill="currentColor" viewBox="0 0 20 20">
                        <path d="M13.586 3.586a2 2 0 112.828 2.828l-.793.793-2.828-2.828.793-.793zm-6.207 7.104l-2.828 2.828a1 1 0 00-.288.399l-.364 1.455a1 1 0 00.737 1.155l1.455-.364a1 1 0 00.399-.288l2.828-2.828-2.586-2.586z"/>
                    </svg>
                </button>
                <button onclick="deleteTask('${task.id}')" class="text-red-600 hover:text-red-800 p-1 rounded hover:bg-red-50" title="Delete Task">
                    <svg class="w-4 h-4" fill="currentColor" viewBox="0 0 20 20">
                        <path fill-rule="evenodd" d="M9 2a1 1 0 00-.894.553L7.382 4H4a1 1 0 000 2v10a2 2 0 002 2h8a2 2 0 002-2V6a1 1 0 000-2h-3.382l-.724-1.447A1 1 0 0011 2H9zM7 7a1 1 0 011 1v7a1 1 0 11-2 0V8a1 1 0 011-1zm5 0a1 1 0 011 1v7a1 1 0 11-2 0V8a1 1 0 011-1z" clip-rule="evenodd"/>
                    </svg>
                </button>
            </div>
            ` : ''}
        </div>
    `;
}

// Drag and drop functions
let draggedTaskId = null;
let draggedElement = null;

function dragStart(event, taskId) {
    draggedTaskId = taskId;
    draggedElement = event.target;
    event.dataTransfer.effectAllowed = 'move';
    
    // Add dragging class for visual feedback
    setTimeout(() => {
        if (draggedElement) {
            draggedElement.classList.add('dragging');
        }
    }, 0);
}

function dragEnd(event) {
    // Remove dragging class
    if (draggedElement) {
        draggedElement.classList.remove('dragging');
    }
    
    // Remove drag-over class from all drop zones
    const dropZones = document.querySelectorAll('.drop-zone');
    dropZones.forEach(zone => zone.classList.remove('drag-over'));
    
    draggedTaskId = null;
    draggedElement = null;
}

function allowDrop(event) {
    event.preventDefault();
}

function dragEnter(event) {
    event.preventDefault();
    if (draggedTaskId && event.currentTarget.classList.contains('drop-zone')) {
        event.currentTarget.classList.add('drag-over');
    }
}

function dragLeave(event) {
    // Only remove drag-over if we're actually leaving the drop zone
    if (!event.currentTarget.contains(event.relatedTarget)) {
        event.currentTarget.classList.remove('drag-over');
    }
}

function drop(event, newStatus) {
    event.preventDefault();
    event.stopPropagation();
    
    // Remove drag-over class
    event.currentTarget.classList.remove('drag-over');
    
    if (draggedTaskId) {
        const draggedTask = allTasks.find(task => task.id == draggedTaskId);
        
        // Only update if status is actually changing
        if (draggedTask && draggedTask.status !== newStatus) {
            updateTaskStatus(draggedTaskId, newStatus);
        }
        
        draggedTaskId = null;
        draggedElement = null;
    }
}

// Global drag and drop functions for HTML
window.dragStart = dragStart;
window.dragEnd = dragEnd;
window.allowDrop = allowDrop;
window.dragEnter = dragEnter;
window.dragLeave = dragLeave;
window.drop = drop;

// Initialize default view
function initializeDefaultView() {
    // Use the switchToView function to properly initialize
    switchToView(currentView);
}

// User Management Functions
function openUserManagement() {
    if (user.role !== 'admin') {
        alert('Access denied. Admin privileges required.');
        return;
    }
    
    document.getElementById('userManagementModal').classList.remove('hidden');
    loadUsersTable();
}

function closeUserManagement() {
    document.getElementById('userManagementModal').classList.add('hidden');
    // Clear form
    document.getElementById('newUsername').value = '';
    document.getElementById('newPassword').value = '';
    document.getElementById('newName').value = '';
    document.getElementById('newEmail').value = '';
    document.getElementById('newRole').value = 'user';
}

async function loadUsersTable() {
    const tbody = document.getElementById('usersTableBody');
    tbody.innerHTML = '';
    
    allUsers.forEach(user => {
        const row = document.createElement('tr');
        row.innerHTML = `
            <td class="px-4 py-2 border-b">${user.username}</td>
            <td class="px-4 py-2 border-b">${user.name}</td>
            <td class="px-4 py-2 border-b">${user.email}</td>
            <td class="px-4 py-2 border-b">
                <span class="px-2 py-1 rounded text-xs ${user.role === 'admin' ? 'bg-red-100 text-red-800' : 'bg-blue-100 text-blue-800'}">
                    ${user.role.toUpperCase()}
                </span>
            </td>
            <td class="px-4 py-2 border-b">
                <button onclick="editUser(${user.id})" class="text-indigo-600 hover:text-indigo-800 mr-2">
                    <i class="fas fa-edit"></i>
                </button>
                <button onclick="deleteUser(${user.id})" class="text-red-600 hover:text-red-800">
                    <i class="fas fa-trash"></i>
                </button>
            </td>
        `;
        tbody.appendChild(row);
    });
}

async function addUser() {
    const userData = {
        username: document.getElementById('newUsername').value,
        password: document.getElementById('newPassword').value,
        name: document.getElementById('newName').value,
        email: document.getElementById('newEmail').value,
        role: document.getElementById('newRole').value
    };
    
    if (!userData.username || !userData.password || !userData.name || !userData.email) {
        alert('Please fill in all fields');
        return;
    }
    
    try {
        await apiRequest('/api/users', 'POST', userData);
        await loadUsers(); // Reload users
        loadUsersTable(); // Reload table
        
        // Clear form
        document.getElementById('newUsername').value = '';
        document.getElementById('newPassword').value = '';
        document.getElementById('newName').value = '';
        document.getElementById('newEmail').value = '';
        document.getElementById('newRole').value = 'user';
        
        alert('User added successfully');
    } catch (error) {
        alert('Failed to add user: ' + error.message);
    }
}

function editUser(userId) {
    const userToEdit = allUsers.find(u => u.id === userId);
    if (!userToEdit) return;
    
    // Populate edit form
    document.getElementById('editUsername').value = userToEdit.username;
    document.getElementById('editPassword').value = '';
    document.getElementById('editName').value = userToEdit.name;
    document.getElementById('editEmail').value = userToEdit.email;
    document.getElementById('editRole').value = userToEdit.role;
    
    // Show modal and set up save button
    document.getElementById('editUserModal').classList.remove('hidden');
    document.getElementById('saveUserBtn').onclick = () => saveUserChanges(userId);
}

function closeEditUser() {
    document.getElementById('editUserModal').classList.add('hidden');
}

async function saveUserChanges(userId) {
    const userData = {
        username: document.getElementById('editUsername').value,
        name: document.getElementById('editName').value,
        email: document.getElementById('editEmail').value,
        role: document.getElementById('editRole').value
    };
    
    const password = document.getElementById('editPassword').value;
    if (password) {
        userData.password = password;
    }
    
    if (!userData.username || !userData.name || !userData.email) {
        alert('Please fill in all required fields');
        return;
    }
    
    try {
        await apiRequest(`/api/users/${userId}`, 'PUT', userData);
        await loadUsers(); // Reload users
        loadUsersTable(); // Reload table
        closeEditUser();
        alert('User updated successfully');
    } catch (error) {
        alert('Failed to update user: ' + error.message);
    }
}

async function deleteUser(userId) {
    if (!confirm('Are you sure you want to delete this user?')) return;
    
    try {
        await apiRequest(`/api/users/${userId}`, 'DELETE');
        await loadUsers(); // Reload users
        loadUsersTable(); // Reload table
        alert('User deleted successfully');
    } catch (error) {
        alert('Failed to delete user: ' + error.message);
    }
}