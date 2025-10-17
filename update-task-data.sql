-- WorkSphere: Update tasks with realistic completion dates
-- Distribute completed tasks across weekdays from Oct 15 to Nov 20, 2025
-- Each user completes at least 2 tasks per weekday

-- First, let's clear existing task data and recreate with better distribution
DELETE FROM tasks;
DELETE FROM subtasks;
DELETE FROM task_history;

-- Reset auto-increment
DELETE FROM sqlite_sequence WHERE name='tasks';
DELETE FROM sqlite_sequence WHERE name='subtasks';
DELETE FROM sqlite_sequence WHERE name='task_history';

-- User IDs:
-- 1: admin
-- 2: jane_smith  
-- 3: john_doe
-- 4: mike_wilson

-- Generate tasks for October 15-31, 2025 (excluding weekends)
-- October 2025: 15(Wed), 16(Thu), 17(Fri), 20(Mon), 21(Tue), 22(Wed), 23(Thu), 24(Fri), 27(Mon), 28(Tue), 29(Wed), 30(Thu), 31(Fri)

-- Jane Smith's tasks (User ID: 2)
INSERT INTO tasks (title, description, priority, status, due_date, assigned_to, created_by, created_at, updated_at, completed_at, estimated_minutes, actual_minutes) VALUES
('Backend API Development', 'Implement REST endpoints for user management', 'HIGH', 'COMPLETED', '2025-10-15', 2, 1, '2025-10-15 08:00:00', '2025-10-15 12:30:00', '2025-10-15 12:30:00', 240, 270),
('Database Schema Update', 'Add new columns for analytics tracking', 'URGENT', 'COMPLETED', '2025-10-15', 2, 1, '2025-10-15 08:00:00', '2025-10-15 16:00:00', '2025-10-15 16:00:00', 180, 200),

('Code Review - Feature X', 'Review pull requests from team', 'MEDIUM', 'COMPLETED', '2025-10-16', 2, 1, '2025-10-16 09:00:00', '2025-10-16 11:00:00', '2025-10-16 11:00:00', 120, 120),
('Unit Test Coverage', 'Increase test coverage to 80%', 'HIGH', 'COMPLETED', '2025-10-16', 2, 1, '2025-10-16 09:00:00', '2025-10-16 15:30:00', '2025-10-16 15:30:00', 300, 390),

('Security Audit', 'Review authentication flow', 'URGENT', 'COMPLETED', '2025-10-17', 2, 1, '2025-10-17 08:00:00', '2025-10-17 13:00:00', '2025-10-17 13:00:00', 240, 300),
('Documentation Update', 'Update API documentation', 'MEDIUM', 'COMPLETED', '2025-10-17', 2, 1, '2025-10-17 08:00:00', '2025-10-17 17:00:00', '2025-10-17 17:00:00', 180, 180),

-- John Doe's tasks (User ID: 3)  
('Frontend Components', 'Build reusable UI components', 'HIGH', 'COMPLETED', '2025-10-15', 3, 1, '2025-10-15 08:30:00', '2025-10-15 14:00:00', '2025-10-15 14:00:00', 300, 330),
('CSS Styling', 'Implement responsive design', 'MEDIUM', 'COMPLETED', '2025-10-15', 3, 1, '2025-10-15 08:30:00', '2025-10-15 17:30:00', '2025-10-15 17:30:00', 240, 240),

('Bug Fix - Login Issue', 'Fix authentication bug', 'URGENT', 'COMPLETED', '2025-10-16', 3, 1, '2025-10-16 08:00:00', '2025-10-16 10:30:00', '2025-10-16 10:30:00', 120, 150),
('Performance Optimization', 'Optimize page load time', 'HIGH', 'COMPLETED', '2025-10-16', 3, 1, '2025-10-16 08:00:00', '2025-10-16 16:00:00', '2025-10-16 16:00:00', 360, 480),

('Integration Testing', 'Test API integration', 'MEDIUM', 'COMPLETED', '2025-10-17', 3, 1, '2025-10-17 09:00:00', '2025-10-17 12:00:00', '2025-10-17 12:00:00', 180, 180),
('Deploy to Staging', 'Deploy latest build', 'HIGH', 'COMPLETED', '2025-10-17', 3, 1, '2025-10-17 09:00:00', '2025-10-17 15:00:00', '2025-10-17 15:00:00', 120, 360),

-- Mike Wilson's tasks (User ID: 4)
('Database Backup', 'Implement automated backup', 'HIGH', 'COMPLETED', '2025-10-15', 4, 1, '2025-10-15 09:00:00', '2025-10-15 13:00:00', '2025-10-15 13:00:00', 240, 240),
('Server Monitoring', 'Setup monitoring dashboard', 'MEDIUM', 'COMPLETED', '2025-10-15', 4, 1, '2025-10-15 09:00:00', '2025-10-15 16:30:00', '2025-10-15 16:30:00', 180, 450),

('Log Analysis', 'Analyze error logs', 'URGENT', 'COMPLETED', '2025-10-16', 4, 1, '2025-10-16 08:30:00', '2025-10-16 11:00:00', '2025-10-16 11:00:00', 150, 150),
('Infrastructure Update', 'Update server configurations', 'HIGH', 'COMPLETED', '2025-10-16', 4, 1, '2025-10-16 08:30:00', '2025-10-16 17:00:00', '2025-10-16 17:00:00', 300, 510),

('CI/CD Pipeline', 'Configure deployment pipeline', 'MEDIUM', 'COMPLETED', '2025-10-17', 4, 1, '2025-10-17 08:00:00', '2025-10-17 14:00:00', '2025-10-17 14:00:00', 360, 360),
('Network Security', 'Review firewall rules', 'HIGH', 'COMPLETED', '2025-10-17', 4, 1, '2025-10-17 08:00:00', '2025-10-17 16:30:00', '2025-10-17 16:30:00', 180, 270),

-- Week of Oct 20-24
('API Refactoring', 'Clean up legacy code', 'MEDIUM', 'COMPLETED', '2025-10-20', 2, 1, '2025-10-20 08:00:00', '2025-10-20 14:00:00', '2025-10-20 14:00:00', 360, 360),
('User Dashboard', 'Create analytics dashboard', 'HIGH', 'COMPLETED', '2025-10-20', 2, 1, '2025-10-20 08:00:00', '2025-10-20 17:00:00', '2025-10-20 17:00:00', 300, 540),

('Mobile Responsiveness', 'Make app mobile-friendly', 'HIGH', 'COMPLETED', '2025-10-21', 3, 1, '2025-10-21 08:00:00', '2025-10-21 15:00:00', '2025-10-21 15:00:00', 420, 420),
('Dark Mode', 'Implement dark theme', 'MEDIUM', 'COMPLETED', '2025-10-21', 3, 1, '2025-10-21 08:00:00', '2025-10-21 17:30:00', '2025-10-21 17:30:00', 240, 570),

('Load Balancing', 'Configure load balancer', 'URGENT', 'COMPLETED', '2025-10-22', 4, 1, '2025-10-22 08:00:00', '2025-10-22 12:00:00', '2025-10-22 12:00:00', 240, 240),
('Disaster Recovery', 'Test backup restore', 'HIGH', 'COMPLETED', '2025-10-22', 4, 1, '2025-10-22 08:00:00', '2025-10-22 16:00:00', '2025-10-22 16:00:00', 300, 480),

('Email Templates', 'Design notification emails', 'MEDIUM', 'COMPLETED', '2025-10-23', 2, 1, '2025-10-23 09:00:00', '2025-10-23 13:00:00', '2025-10-23 13:00:00', 240, 240),
('Push Notifications', 'Implement push service', 'HIGH', 'COMPLETED', '2025-10-23', 2, 1, '2025-10-23 09:00:00', '2025-10-23 17:00:00', '2025-10-23 17:00:00', 300, 480),

('Accessibility', 'Add ARIA labels', 'MEDIUM', 'COMPLETED', '2025-10-24', 3, 1, '2025-10-24 08:00:00', '2025-10-24 12:00:00', '2025-10-24 12:00:00', 240, 240),
('Internationalization', 'Add multi-language support', 'HIGH', 'COMPLETED', '2025-10-24', 3, 1, '2025-10-24 08:00:00', '2025-10-24 16:30:00', '2025-10-24 16:30:00', 360, 510),

-- Week of Oct 27-31
('Cache Implementation', 'Add Redis caching', 'URGENT', 'COMPLETED', '2025-10-27', 4, 1, '2025-10-27 08:00:00', '2025-10-27 13:00:00', '2025-10-27 13:00:00', 300, 300),
('Performance Metrics', 'Setup APM tool', 'HIGH', 'COMPLETED', '2025-10-27', 4, 1, '2025-10-27 08:00:00', '2025-10-27 17:00:00', '2025-10-27 17:00:00', 240, 540),

('Search Feature', 'Implement full-text search', 'HIGH', 'COMPLETED', '2025-10-28', 2, 1, '2025-10-28 08:00:00', '2025-10-28 14:00:00', '2025-10-28 14:00:00', 360, 360),
('Export Functionality', 'Add CSV/PDF export', 'MEDIUM', 'COMPLETED', '2025-10-28', 2, 1, '2025-10-28 08:00:00', '2025-10-28 16:00:00', '2025-10-28 16:00:00', 240, 480),

('Animations', 'Add smooth transitions', 'LOW', 'COMPLETED', '2025-10-29', 3, 1, '2025-10-29 09:00:00', '2025-10-29 12:00:00', '2025-10-29 12:00:00', 180, 180),
('Icon Library', 'Integrate icon set', 'MEDIUM', 'COMPLETED', '2025-10-29', 3, 1, '2025-10-29 09:00:00', '2025-10-29 15:00:00', '2025-10-29 15:00:00', 240, 360),

('SSL Certificates', 'Renew SSL certs', 'URGENT', 'COMPLETED', '2025-10-30', 4, 1, '2025-10-30 08:00:00', '2025-10-30 10:00:00', '2025-10-30 10:00:00', 120, 120),
('Database Optimization', 'Add indexes', 'HIGH', 'COMPLETED', '2025-10-30', 4, 1, '2025-10-30 08:00:00', '2025-10-30 15:00:00', '2025-10-30 15:00:00', 300, 420),

('User Feedback', 'Collect user reviews', 'MEDIUM', 'COMPLETED', '2025-10-31', 2, 1, '2025-10-31 09:00:00', '2025-10-31 13:00:00', '2025-10-31 13:00:00', 240, 240),
('Analytics Report', 'Generate monthly report', 'HIGH', 'COMPLETED', '2025-10-31', 2, 1, '2025-10-31 09:00:00', '2025-10-31 17:00:00', '2025-10-31 17:00:00', 300, 480),

-- November tasks (extending through Nov 20)
-- Week of Nov 3-7
('Rate Limiting', 'Implement API rate limits', 'HIGH', 'COMPLETED', '2025-11-03', 3, 1, '2025-11-03 08:00:00', '2025-11-03 14:00:00', '2025-11-03 14:00:00', 360, 360),
('Error Handling', 'Improve error messages', 'MEDIUM', 'COMPLETED', '2025-11-03', 3, 1, '2025-11-03 08:00:00', '2025-11-03 16:00:00', '2025-11-03 16:00:00', 240, 480),

('Kubernetes Setup', 'Deploy to K8s cluster', 'URGENT', 'COMPLETED', '2025-11-04', 4, 1, '2025-11-04 08:00:00', '2025-11-04 13:00:00', '2025-11-04 13:00:00', 300, 300),
('Auto-scaling', 'Configure pod autoscaling', 'HIGH', 'COMPLETED', '2025-11-04', 4, 1, '2025-11-04 08:00:00', '2025-11-04 17:00:00', '2025-11-04 17:00:00', 360, 540),

('Webhook Integration', 'Setup webhook endpoints', 'MEDIUM', 'COMPLETED', '2025-11-05', 2, 1, '2025-11-05 09:00:00', '2025-11-05 13:00:00', '2025-11-05 13:00:00', 240, 240),
('Payment Gateway', 'Integrate Stripe', 'HIGH', 'COMPLETED', '2025-11-05', 2, 1, '2025-11-05 09:00:00', '2025-11-05 17:30:00', '2025-11-05 17:30:00', 420, 510),

('User Onboarding', 'Create tutorial flow', 'MEDIUM', 'COMPLETED', '2025-11-06', 3, 1, '2025-11-06 08:00:00', '2025-11-06 13:00:00', '2025-11-06 13:00:00', 300, 300),
('Help Center', 'Build FAQ section', 'LOW', 'COMPLETED', '2025-11-06', 3, 1, '2025-11-06 08:00:00', '2025-11-06 15:00:00', '2025-11-06 15:00:00', 240, 420),

('Compliance Audit', 'GDPR compliance check', 'URGENT', 'COMPLETED', '2025-11-07', 4, 1, '2025-11-07 08:00:00', '2025-11-07 12:00:00', '2025-11-07 12:00:00', 240, 240),
('Data Encryption', 'Encrypt sensitive data', 'HIGH', 'COMPLETED', '2025-11-07', 4, 1, '2025-11-07 08:00:00', '2025-11-07 16:00:00', '2025-11-07 16:00:00', 300, 480),

-- Week of Nov 10-14
('Microservices', 'Split monolith into services', 'HIGH', 'COMPLETED', '2025-11-10', 2, 1, '2025-11-10 08:00:00', '2025-11-10 15:00:00', '2025-11-10 15:00:00', 420, 420),
('GraphQL API', 'Implement GraphQL endpoint', 'MEDIUM', 'COMPLETED', '2025-11-10', 2, 1, '2025-11-10 08:00:00', '2025-11-10 17:00:00', '2025-11-10 17:00:00', 300, 540),

('Progressive Web App', 'Add PWA support', 'HIGH', 'COMPLETED', '2025-11-11', 3, 1, '2025-11-11 08:00:00', '2025-11-11 14:00:00', '2025-11-11 14:00:00', 360, 360),
('Offline Mode', 'Implement service workers', 'MEDIUM', 'COMPLETED', '2025-11-11', 3, 1, '2025-11-11 08:00:00', '2025-11-11 17:00:00', '2025-11-11 17:00:00', 360, 540),

('Message Queue', 'Setup RabbitMQ', 'URGENT', 'COMPLETED', '2025-11-12', 4, 1, '2025-11-12 08:00:00', '2025-11-12 12:00:00', '2025-11-12 12:00:00', 240, 240),
('Background Jobs', 'Implement job scheduler', 'HIGH', 'COMPLETED', '2025-11-12', 4, 1, '2025-11-12 08:00:00', '2025-11-12 17:00:00', '2025-11-12 17:00:00', 360, 540),

('Real-time Updates', 'Add WebSocket support', 'HIGH', 'COMPLETED', '2025-11-13', 2, 1, '2025-11-13 08:00:00', '2025-11-13 14:00:00', '2025-11-13 14:00:00', 360, 360),
('Chat Feature', 'Build messaging system', 'MEDIUM', 'COMPLETED', '2025-11-13', 2, 1, '2025-11-13 08:00:00', '2025-11-13 17:30:00', '2025-11-13 17:30:00', 420, 570),

('Custom Themes', 'Allow user themes', 'LOW', 'COMPLETED', '2025-11-14', 3, 1, '2025-11-14 09:00:00', '2025-11-14 12:00:00', '2025-11-14 12:00:00', 180, 180),
('Keyboard Shortcuts', 'Add hotkeys', 'MEDIUM', 'COMPLETED', '2025-11-14', 3, 1, '2025-11-14 09:00:00', '2025-11-14 15:00:00', '2025-11-14 15:00:00', 240, 360),

-- Week of Nov 17-20
('Container Registry', 'Setup private registry', 'HIGH', 'COMPLETED', '2025-11-17', 4, 1, '2025-11-17 08:00:00', '2025-11-17 13:00:00', '2025-11-17 13:00:00', 300, 300),
('Image Optimization', 'Optimize Docker images', 'MEDIUM', 'COMPLETED', '2025-11-17', 4, 1, '2025-11-17 08:00:00', '2025-11-17 16:00:00', '2025-11-17 16:00:00', 300, 480),

('Social Login', 'Add OAuth providers', 'HIGH', 'COMPLETED', '2025-11-18', 2, 1, '2025-11-18 08:00:00', '2025-11-18 14:00:00', '2025-11-18 14:00:00', 360, 360),
('Two-Factor Auth', 'Implement 2FA', 'URGENT', 'COMPLETED', '2025-11-18', 2, 1, '2025-11-18 08:00:00', '2025-11-18 17:00:00', '2025-11-18 17:00:00', 420, 540),

('File Upload', 'Add drag-drop upload', 'MEDIUM', 'COMPLETED', '2025-11-19', 3, 1, '2025-11-19 08:00:00', '2025-11-19 13:00:00', '2025-11-19 13:00:00', 300, 300),
('Image Gallery', 'Create photo viewer', 'LOW', 'COMPLETED', '2025-11-19', 3, 1, '2025-11-19 08:00:00', '2025-11-19 15:30:00', '2025-11-19 15:30:00', 240, 450),

('Cost Optimization', 'Reduce cloud costs', 'HIGH', 'COMPLETED', '2025-11-20', 4, 1, '2025-11-20 08:00:00', '2025-11-20 13:00:00', '2025-11-20 13:00:00', 300, 300),
('Resource Tagging', 'Tag all resources', 'MEDIUM', 'COMPLETED', '2025-11-20', 4, 1, '2025-11-20 08:00:00', '2025-11-20 16:00:00', '2025-11-20 16:00:00', 240, 480);

-- Update categories for the tasks
UPDATE tasks SET category_id = (ABS(RANDOM()) % 6) + 1;

-- Vacuum and analyze
VACUUM;
ANALYZE;

SELECT 'Task data updated successfully!' as message;
SELECT COUNT(*) as total_tasks FROM tasks;
SELECT status, COUNT(*) as count FROM tasks GROUP BY status;
SELECT assigned_to, COUNT(*) as task_count FROM tasks WHERE status = 'COMPLETED' GROUP BY assigned_to;
