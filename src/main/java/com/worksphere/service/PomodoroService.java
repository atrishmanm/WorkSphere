package com.worksphere.service;

import com.worksphere.dao.TaskDAO;
import com.worksphere.model.Task;

import javax.swing.Timer;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Pomodoro Timer service for productivity and time tracking
 */
public class PomodoroService {
    
    public static final int WORK_DURATION_MINUTES = 25;
    public static final int SHORT_BREAK_MINUTES = 5;
    public static final int LONG_BREAK_MINUTES = 15;
    public static final int CYCLES_BEFORE_LONG_BREAK = 4;
    
    private Timer timer;
    private PomodoroState currentState;
    private int remainingSeconds;
    private int completedCycles;
    private Task currentTask;
    private LocalDateTime sessionStartTime;
    private List<PomodoroListener> listeners;
    private TaskDAO taskDAO;
    
    public PomodoroService() {
        this.currentState = PomodoroState.STOPPED;
        this.completedCycles = 0;
        this.listeners = new ArrayList<>();
        this.taskDAO = new TaskDAO();
        
        // Create timer that ticks every second
        this.timer = new Timer(1000, e -> tick());
    }
    
    /**
     * Start a Pomodoro session for a specific task
     */
    public void startPomodoro(Task task) {
        if (currentState != PomodoroState.STOPPED) {
            stopPomodoro();
        }
        
        this.currentTask = task;
        this.currentState = PomodoroState.WORKING;
        this.remainingSeconds = WORK_DURATION_MINUTES * 60;
        this.sessionStartTime = LocalDateTime.now();
        
        timer.start();
        notifyListeners(PomodoroEvent.STARTED);
    }
    
    /**
     * Start a break (short or long based on completed cycles)
     */
    public void startBreak() {
        if (currentState == PomodoroState.WORKING) {
            completedCycles++;
        }
        
        boolean isLongBreak = (completedCycles % CYCLES_BEFORE_LONG_BREAK == 0);
        currentState = isLongBreak ? PomodoroState.LONG_BREAK : PomodoroState.SHORT_BREAK;
        remainingSeconds = (isLongBreak ? LONG_BREAK_MINUTES : SHORT_BREAK_MINUTES) * 60;
        
        timer.start();
        notifyListeners(isLongBreak ? PomodoroEvent.LONG_BREAK_STARTED : PomodoroEvent.SHORT_BREAK_STARTED);
    }
    
    /**
     * Pause the current timer
     */
    public void pausePomodoro() {
        if (currentState != PomodoroState.STOPPED) {
            timer.stop();
            currentState = PomodoroState.PAUSED;
            notifyListeners(PomodoroEvent.PAUSED);
        }
    }
    
    /**
     * Resume the paused timer
     */
    public void resumePomodoro() {
        if (currentState == PomodoroState.PAUSED) {
            timer.start();
            currentState = remainingSeconds > 0 ? 
                (completedCycles % CYCLES_BEFORE_LONG_BREAK == 0 && completedCycles > 0 ? 
                    PomodoroState.LONG_BREAK : 
                    (completedCycles > 0 ? PomodoroState.SHORT_BREAK : PomodoroState.WORKING)) 
                : PomodoroState.WORKING;
            notifyListeners(PomodoroEvent.RESUMED);
        }
    }
    
    /**
     * Stop the current Pomodoro session
     */
    public void stopPomodoro() {
        timer.stop();
        
        // Log time spent if we were working on a task
        if (currentState == PomodoroState.WORKING && currentTask != null && sessionStartTime != null) {
            int minutesWorked = calculateElapsedMinutes();
            logTimeSpent(currentTask, minutesWorked);
        }
        
        currentState = PomodoroState.STOPPED;
        currentTask = null;
        sessionStartTime = null;
        notifyListeners(PomodoroEvent.STOPPED);
    }
    
    /**
     * Skip to next phase (work -> break or break -> work)
     */
    public void skipToNext() {
        if (currentState == PomodoroState.WORKING) {
            // Complete the work session and log time
            if (currentTask != null && sessionStartTime != null) {
                logTimeSpent(currentTask, WORK_DURATION_MINUTES);
            }
            startBreak();
        } else if (currentState == PomodoroState.SHORT_BREAK || currentState == PomodoroState.LONG_BREAK) {
            startNextWorkSession();
        }
    }
    
    /**
     * Start the next work session
     */
    public void startNextWorkSession() {
        currentState = PomodoroState.WORKING;
        remainingSeconds = WORK_DURATION_MINUTES * 60;
        sessionStartTime = LocalDateTime.now();
        
        timer.start();
        notifyListeners(PomodoroEvent.WORK_SESSION_STARTED);
    }
    
    /**
     * Add a listener for Pomodoro events
     */
    public void addListener(PomodoroListener listener) {
        listeners.add(listener);
    }
    
    /**
     * Remove a listener
     */
    public void removeListener(PomodoroListener listener) {
        listeners.remove(listener);
    }
    
    // Getters
    public PomodoroState getCurrentState() { return currentState; }
    public int getRemainingSeconds() { return remainingSeconds; }
    public int getCompletedCycles() { return completedCycles; }
    public Task getCurrentTask() { return currentTask; }
    
    public String getFormattedTimeRemaining() {
        int minutes = remainingSeconds / 60;
        int seconds = remainingSeconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }
    
    public double getProgress() {
        int totalSeconds = getCurrentPhaseDuration() * 60;
        return totalSeconds > 0 ? 1.0 - (double) remainingSeconds / totalSeconds : 0.0;
    }
    
    private void tick() {
        remainingSeconds--;
        notifyListeners(PomodoroEvent.TICK);
        
        if (remainingSeconds <= 0) {
            timer.stop();
            handlePhaseComplete();
        }
    }
    
    private void handlePhaseComplete() {
        switch (currentState) {
            case WORKING:
                // Log the completed work session
                if (currentTask != null && sessionStartTime != null) {
                    logTimeSpent(currentTask, WORK_DURATION_MINUTES);
                }
                notifyListeners(PomodoroEvent.WORK_COMPLETED);
                // Auto-start break or ask user
                startBreak();
                break;
                
            case SHORT_BREAK:
                notifyListeners(PomodoroEvent.SHORT_BREAK_COMPLETED);
                currentState = PomodoroState.STOPPED;
                break;
                
            case LONG_BREAK:
                notifyListeners(PomodoroEvent.LONG_BREAK_COMPLETED);
                currentState = PomodoroState.STOPPED;
                // Reset cycle count after long break
                completedCycles = 0;
                break;
                
            case STOPPED:
            case PAUSED:
                // No action needed for these states
                break;
        }
    }
    
    private int getCurrentPhaseDuration() {
        switch (currentState) {
            case WORKING: return WORK_DURATION_MINUTES;
            case SHORT_BREAK: return SHORT_BREAK_MINUTES;
            case LONG_BREAK: return LONG_BREAK_MINUTES;
            default: return 0;
        }
    }
    
    private int calculateElapsedMinutes() {
        if (sessionStartTime == null) return 0;
        
        LocalDateTime now = LocalDateTime.now();
        long elapsedMinutes = java.time.temporal.ChronoUnit.MINUTES.between(sessionStartTime, now);
        return (int) Math.max(0, elapsedMinutes);
    }
    
    private void logTimeSpent(Task task, int minutes) {
        try {
            // Update task's actual time
            int newActualTime = task.getActualMinutes() + minutes;
            taskDAO.updateTaskTime(task.getId(), newActualTime);
            
            // Update the task object
            task.setActualMinutes(newActualTime);
            task.setLastWorkedAt(LocalDateTime.now());
            
        } catch (SQLException e) {
            System.err.println("Error logging time spent: " + e.getMessage());
        }
    }
    
    private void notifyListeners(PomodoroEvent event) {
        for (PomodoroListener listener : listeners) {
            listener.onPomodoroEvent(event, this);
        }
    }
    
    // Enums and interfaces
    
    public enum PomodoroState {
        STOPPED,
        WORKING,
        SHORT_BREAK,
        LONG_BREAK,
        PAUSED
    }
    
    public enum PomodoroEvent {
        STARTED,
        PAUSED,
        RESUMED,
        STOPPED,
        TICK,
        WORK_COMPLETED,
        SHORT_BREAK_STARTED,
        SHORT_BREAK_COMPLETED,
        LONG_BREAK_STARTED,
        LONG_BREAK_COMPLETED,
        WORK_SESSION_STARTED
    }
    
    public interface PomodoroListener {
        void onPomodoroEvent(PomodoroEvent event, PomodoroService service);
    }
    
    /**
     * Get statistics for Pomodoro sessions
     */
    public PomodoroStats getStats() {
        PomodoroStats stats = new PomodoroStats();
        stats.completedCycles = this.completedCycles;
        stats.isActive = currentState != PomodoroState.STOPPED;
        stats.currentTask = this.currentTask;
        stats.currentState = this.currentState;
        return stats;
    }
    
    public static class PomodoroStats {
        public int completedCycles;
        public boolean isActive;
        public Task currentTask;
        public PomodoroState currentState;
    }
}