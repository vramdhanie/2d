package com.niravramdhanie.twod.game.utils;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

/**
 * Manages the game timer.
 * Displays and updates a countdown timer on screen.
 */
public class TimerManager {
    private int time; // Time in seconds
    private int maxTime; // Maximum time
    private boolean running; // Whether the timer is running
    private long lastUpdateTime; // Time of last update in milliseconds
    
    /**
     * Creates a new timer with the specified maximum time.
     * 
     * @param maxTime The maximum time in seconds
     */
    public TimerManager(int maxTime) {
        this.maxTime = maxTime;
        this.time = maxTime;
        this.running = false;
        this.lastUpdateTime = 0;
    }
    
    /**
     * Starts the timer.
     */
    public void start() {
        running = true;
        lastUpdateTime = System.currentTimeMillis();
    }
    
    /**
     * Stops the timer.
     */
    public void stop() {
        running = false;
    }
    
    /**
     * Resets the timer to the maximum time.
     */
    public void reset() {
        time = maxTime;
        lastUpdateTime = System.currentTimeMillis();
    }
    
    /**
     * Updates the timer based on elapsed time.
     */
    public void update() {
        if (!running || time <= 0) {
            return;
        }
        
        long currentTime = System.currentTimeMillis();
        long elapsed = currentTime - lastUpdateTime;
        
        // Decrease timer by 1 second if 1000ms has passed
        if (elapsed >= 1000) {
            time--;
            lastUpdateTime = currentTime;
            
            // Stop timer if it reaches 0
            if (time <= 0) {
                time = 0;
                stop();
            }
        }
    }
    
    /**
     * Renders the timer on screen.
     * 
     * @param g The graphics context to render to
     * @param screenWidth The width of the screen
     */
    public void render(Graphics2D g, int screenWidth) {
        // Save original font and color
        Font originalFont = g.getFont();
        Color originalColor = g.getColor();
        
        // Set font for timer
        Font timerFont = new Font("Arial", Font.BOLD, 24);
        g.setFont(timerFont);
        
        // Format time as mm:ss
        String timeText = String.format("%02d:%02d", time / 60, time % 60);
        
        // Determine color based on remaining time
        if (time > maxTime / 2) {
            g.setColor(Color.WHITE); // More than half time remains
        } else if (time > maxTime / 4) {
            g.setColor(Color.YELLOW); // Between 1/4 and 1/2 time remains
        } else {
            g.setColor(Color.RED); // Less than 1/4 time remains
        }
        
        // Center text
        FontMetrics metrics = g.getFontMetrics();
        Rectangle2D bounds = metrics.getStringBounds(timeText, g);
        int x = (screenWidth - (int)bounds.getWidth()) / 2;
        int y = 30; // Position near the top of the screen
        
        // Draw timer
        g.drawString(timeText, x, y);
        
        // Restore original font and color
        g.setFont(originalFont);
        g.setColor(originalColor);
    }
    
    /**
     * Gets the current time.
     * 
     * @return The current time in seconds
     */
    public int getTime() {
        return time;
    }
    
    /**
     * Sets the current time.
     * 
     * @param time The time in seconds
     */
    public void setTime(int time) {
        this.time = time;
    }
    
    /**
     * Gets the maximum time.
     * 
     * @return The maximum time in seconds
     */
    public int getMaxTime() {
        return maxTime;
    }
    
    /**
     * Sets the maximum time.
     * 
     * @param maxTime The maximum time in seconds
     */
    public void setMaxTime(int maxTime) {
        this.maxTime = maxTime;
    }
    
    /**
     * Checks if the timer is running.
     * 
     * @return True if the timer is running, false otherwise
     */
    public boolean isRunning() {
        return running;
    }
} 