package com.niravramdhanie.twod.game.actions;

import com.niravramdhanie.twod.game.entity.Button;

/**
 * An action that executes another action and then automatically deactivates the button after a specified duration.
 */
public class TimedAction implements Action {
    private Action wrappedAction;
    private int duration; // in milliseconds
    private Button targetButton;
    private long activationTime;
    
    /**
     * Creates a new timed action.
     * 
     * @param wrappedAction The action to execute
     * @param duration The duration in milliseconds before the button deactivates
     */
    public TimedAction(Action wrappedAction, int duration) {
        this.wrappedAction = wrappedAction;
        this.duration = duration;
        this.activationTime = 0;
    }
    
    /**
     * Creates a timed action with a default duration of 3 seconds.
     * 
     * @param wrappedAction The action to execute
     */
    public TimedAction(Action wrappedAction) {
        this(wrappedAction, 3000); // Default 3 seconds
    }
    
    /**
     * Creates a timed message action with the specified message and duration.
     * 
     * @param message The message to display
     * @param durationMillis The duration in milliseconds before deactivation
     */
    public static TimedAction createTimedMessage(String message, int durationMillis) {
        MessageAction msgAction = new MessageAction(message);
        TimedAction timedAction = new TimedAction(msgAction, durationMillis);
        return timedAction;
    }
    
    @Override
    public void execute() {
        // Execute the wrapped action
        if (wrappedAction != null) {
            wrappedAction.execute();
        }
        
        // Record the activation time
        activationTime = System.currentTimeMillis();
        
        // Start a thread to deactivate the button after the duration
        if (targetButton != null) {
            new Thread(() -> {
                try {
                    Thread.sleep(duration);
                    targetButton.deactivate();
                } catch (InterruptedException e) {
                    System.err.println("TimedAction interrupted: " + e.getMessage());
                }
            }).start();
        }
    }
    
    @Override
    public String getDescription() {
        String baseDesc = wrappedAction != null ? wrappedAction.getDescription() : "No action";
        return baseDesc + " (Timed: " + duration / 1000.0 + "s)";
    }
    
    /**
     * Sets the button that this timed action will deactivate.
     * 
     * @param button The button to deactivate
     */
    public void setTargetButton(Button button) {
        this.targetButton = button;
    }
    
    /**
     * Gets the button that this action will deactivate.
     * 
     * @return The target button
     */
    public Button getTargetButton() {
        return targetButton;
    }
    
    /**
     * Sets the duration before the button deactivates.
     * 
     * @param duration The duration in milliseconds
     */
    public void setDuration(int duration) {
        this.duration = duration;
    }
    
    /**
     * Gets the duration before the button deactivates.
     * 
     * @return The duration in milliseconds
     */
    public int getDuration() {
        return duration;
    }
    
    /**
     * Sets the wrapped action.
     * 
     * @param wrappedAction The action to execute
     */
    public void setWrappedAction(Action wrappedAction) {
        this.wrappedAction = wrappedAction;
    }
    
    /**
     * Gets the wrapped action.
     * 
     * @return The wrapped action
     */
    public Action getWrappedAction() {
        return wrappedAction;
    }
    
    /**
     * Gets the time when this action was activated.
     * 
     * @return The activation time as milliseconds since epoch, or 0 if not activated
     */
    public long getActivationTime() {
        return activationTime;
    }
    
    /**
     * Calculates the remaining time before deactivation.
     * 
     * @return The remaining time in milliseconds, or 0 if not activated or time expired
     */
    public long getRemainingTime() {
        if (activationTime == 0) {
            return 0;
        }
        
        long elapsed = System.currentTimeMillis() - activationTime;
        long remaining = duration - elapsed;
        
        return Math.max(0, remaining);
    }
} 