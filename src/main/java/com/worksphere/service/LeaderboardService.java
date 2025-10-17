package com.worksphere.service;

import com.worksphere.dao.TaskDAO;
import com.worksphere.dao.UserDAO;
import com.worksphere.model.Task;
import com.worksphere.model.TaskStatus;
import com.worksphere.model.Priority;
import com.worksphere.model.User;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for calculating user rankings and leaderboard statistics.
 * Points system:
 * - Completed task: +10 points
 * - Urgent task completed: +20 points (additional)
 * - On-time completion: +5 points (additional)
 * - Overdue completion: -5 points (penalty)
 */
public class LeaderboardService {
    private final TaskDAO taskDAO;
    private final UserDAO userDAO;

    public LeaderboardService(TaskDAO taskDAO, UserDAO userDAO) {
        this.taskDAO = taskDAO;
        this.userDAO = userDAO;
    }

    /**
     * Get leaderboard for all time
     */
    public List<UserRanking> getAllTimeRankings() {
        return calculateRankings(null, null);
    }

    /**
     * Get leaderboard for current week
     */
    public List<UserRanking> getWeeklyRankings() {
        LocalDateTime weekStart = LocalDate.now().atStartOfDay().minusDays(7);
        return calculateRankings(weekStart, null);
    }

    /**
     * Get leaderboard for current month
     */
    public List<UserRanking> getMonthlyRankings() {
        LocalDateTime monthStart = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        return calculateRankings(monthStart, null);
    }

    /**
     * Calculate rankings for a given time period
     * @param startDate Start date for filtering (null = no start limit)
     * @param endDate End date for filtering (null = no end limit)
     * @return Sorted list of user rankings
     */
    private List<UserRanking> calculateRankings(LocalDateTime startDate, LocalDateTime endDate) {
        try {
            List<User> allUsers = userDAO.findAll();
            List<Task> allTasks = taskDAO.findAll();

            // Calculate statistics for each user
            Map<Integer, UserRanking> rankingMap = new HashMap<>();
            
            for (User user : allUsers) {
                UserRanking ranking = new UserRanking();
                ranking.setUserId(user.getId());
                ranking.setUsername(user.getUsername());
                ranking.setFullName(user.getFullName());
                
                // Get user's completed tasks, filtered by date range if specified
                List<Task> userTasks = allTasks.stream()
                    .filter(task -> {
                        // Must be assigned to this user and completed
                        if (task.getAssignedTo() == null || task.getAssignedTo() != user.getId()) {
                            return false;
                        }
                        if (task.getStatus() != TaskStatus.COMPLETED) {
                            return false;
                        }
                        
                        // Apply date filter only if start/end date is specified
                        if (startDate != null || endDate != null) {
                            LocalDateTime completedDate = task.getCompletedAt();
                            if (completedDate == null) return false;
                            if (startDate != null && completedDate.isBefore(startDate)) return false;
                            if (endDate != null && completedDate.isAfter(endDate)) return false;
                        }
                        
                        return true;
                    })
                    .collect(Collectors.toList());

                ranking.setTasksCompleted(userTasks.size());
                
                // Calculate points and statistics
                int totalPoints = 0;
                long totalCompletionTimeHours = 0;
                int onTimeCount = 0;
                int overdueCount = 0;

                for (Task task : userTasks) {
                    // Base points for completion
                    totalPoints += 10;

                    // Bonus for urgent tasks
                    if (task.getPriority() == Priority.URGENT) {
                        totalPoints += 20;
                    }

                    // Check if completed on time
                    if (task.getDueDate() != null && task.getCompletedAt() != null) {
                        LocalDate completedDate = task.getCompletedAt().toLocalDate();
                        if (completedDate.isBefore(task.getDueDate()) || 
                            completedDate.isEqual(task.getDueDate())) {
                            totalPoints += 5; // On-time bonus
                            onTimeCount++;
                        } else {
                            totalPoints -= 5; // Overdue penalty
                            overdueCount++;
                        }

                        // Calculate completion time
                        long hours = ChronoUnit.HOURS.between(task.getCreatedAt(), task.getCompletedAt());
                        totalCompletionTimeHours += hours;
                    }
                }

                ranking.setPoints(totalPoints);
                
                // Calculate average completion time
                if (userTasks.size() > 0) {
                    ranking.setAvgCompletionTimeHours(totalCompletionTimeHours / userTasks.size());
                    ranking.setCompletionRate((double) onTimeCount / userTasks.size() * 100);
                } else {
                    ranking.setAvgCompletionTimeHours(0);
                    ranking.setCompletionRate(0.0);
                }

                ranking.setOnTimeCount(onTimeCount);
                ranking.setOverdueCount(overdueCount);

                rankingMap.put(user.getId(), ranking);
            }

            // Sort by points (descending), then by tasks completed, then by completion rate
            List<UserRanking> rankings = new ArrayList<>(rankingMap.values());
            rankings.sort((r1, r2) -> {
                int pointsCompare = Integer.compare(r2.getPoints(), r1.getPoints());
                if (pointsCompare != 0) return pointsCompare;
                
                int tasksCompare = Integer.compare(r2.getTasksCompleted(), r1.getTasksCompleted());
                if (tasksCompare != 0) return tasksCompare;
                
                return Double.compare(r2.getCompletionRate(), r1.getCompletionRate());
            });

            // Assign ranks
            for (int i = 0; i < rankings.size(); i++) {
                rankings.get(i).setRank(i + 1);
            }

            return rankings;
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    /**
     * Get top N users for a given time period
     */
    public List<UserRanking> getTopUsers(int limit, String period) {
        List<UserRanking> rankings;
        
        switch (period.toLowerCase()) {
            case "weekly":
                rankings = getWeeklyRankings();
                break;
            case "monthly":
                rankings = getMonthlyRankings();
                break;
            case "all-time":
            default:
                rankings = getAllTimeRankings();
                break;
        }

        return rankings.stream()
            .limit(limit)
            .collect(Collectors.toList());
    }

    /**
     * Data class for user ranking information
     */
    public static class UserRanking {
        private int rank;
        private int userId;
        private String username;
        private String fullName;
        private int points;
        private int tasksCompleted;
        private long avgCompletionTimeHours;
        private double completionRate;
        private int onTimeCount;
        private int overdueCount;

        // Getters and setters
        public int getRank() { return rank; }
        public void setRank(int rank) { this.rank = rank; }

        public int getUserId() { return userId; }
        public void setUserId(int userId) { this.userId = userId; }

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }

        public String getFullName() { return fullName; }
        public void setFullName(String fullName) { this.fullName = fullName; }

        public int getPoints() { return points; }
        public void setPoints(int points) { this.points = points; }

        public int getTasksCompleted() { return tasksCompleted; }
        public void setTasksCompleted(int tasksCompleted) { this.tasksCompleted = tasksCompleted; }

        public long getAvgCompletionTimeHours() { return avgCompletionTimeHours; }
        public void setAvgCompletionTimeHours(long avgCompletionTimeHours) { 
            this.avgCompletionTimeHours = avgCompletionTimeHours; 
        }

        public double getCompletionRate() { return completionRate; }
        public void setCompletionRate(double completionRate) { this.completionRate = completionRate; }

        public int getOnTimeCount() { return onTimeCount; }
        public void setOnTimeCount(int onTimeCount) { this.onTimeCount = onTimeCount; }

        public int getOverdueCount() { return overdueCount; }
        public void setOverdueCount(int overdueCount) { this.overdueCount = overdueCount; }

        @Override
        public String toString() {
            return String.format("#%d %s - %d points (%d tasks)", 
                rank, username, points, tasksCompleted);
        }
    }
}
