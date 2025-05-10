package com.niravramdhanie.twod.game.actions;

import com.niravramdhanie.twod.game.entity.Button;

/**
 * An action that executes for a limited time and then automatically deactivates.
 */
public class TimedAction implements Action {
    private Action wrappedAction;
    private Action deactivateAction; // Action to execute when deactivated
    private long durationMillis;
    private boolean isActive;
    private long startTime;
    private Button targetButton;
    
    /**
     * Creates a new timed action.
     * 
     * @param wrappedAction The action to execute for a limited time
     * @param durationMillis The duration in milliseconds
     */
    public TimedAction(Action wrappedAction, long durationMillis) {
        this.wrappedAction = wrappedAction;
        this.durationMillis = durationMillis;
        this.isActive = false;
        this.startTime = 0;
        this.deactivateAction = null;
    }
    
    /**
     * Sets the button that should be deactivated when the timer expires.
     * 
     * @param button The button to deactivate
     */
    public void setTargetButton(Button button) {
        this.targetButton = button;
    }
    
    /**
     * Sets an action to execute when this timed action deactivates.
     * 
     * @param deactivateAction The action to execute on deactivation
     */
    public void setDeactivateAction(Action deactivateAction) {
        this.deactivateAction = deactivateAction;
    }
    
    @Override
    public void execute() {
        if (!isActive) {
            // Start the timer
            isActive = true;
            startTime = System.currentTimeMillis();
            
            // Execute the wrapped action
            if (wrappedAction != null) {
                wrappedAction.execute();
            }
            
            System.out.println("Timed action started for " + durationMillis + "ms");
        } else {
            // Already active, check if it should be deactivated
            long elapsedTime = System.currentTimeMillis() - startTime;
            if (elapsedTime >= durationMillis) {
                deactivate();
                System.out.println("Timed action expired");
            } else {
                // Refresh the timer
                startTime = System.currentTimeMillis();
                System.out.println("Timed action refreshed for " + durationMillis + "ms");
            }
        }
    }
    
    /**
     * Updates the timed action status. Should be called regularly.
     * 
     * @return True if the action is still active, false if it has expired
     */
    public boolean update() {
        if (isActive) {
            long elapsedTime = System.currentTimeMillis() - startTime;
            if (elapsedTime >= durationMillis) {
                deactivate();
                return false;
            }
            return true;
        }
        return false;
    }
    
    /**
     * Gets the remaining time in milliseconds.
     * 
     * @return The remaining time, or 0 if not active
     */
    public long getRemainingTime() {
        if (!isActive) {
            return 0;
        }
        
        long elapsedTime = System.currentTimeMillis() - startTime;
        long remaining = durationMillis - elapsedTime;
        return (remaining > 0) ? remaining : 0;
    }
    
    /**
     * Gets the fraction of time remaining (0.0 to 1.0).
     * 
     * @return The fraction of time remaining, or 0 if not active
     */
    public float getTimeRemainingFraction() {
        if (!isActive || durationMillis <= 0) {
            return 0.0f;
        }
        
        return (float)getRemainingTime() / durationMillis;
    }
    
    /**
     * Forcefully deactivates the timed action.
     */
    public void deactivate() {
        isActive = false;
        
        // Execute the deactivate action if set
        if (deactivateAction != null) {
            deactivateAction.execute();
        }
        
        // Deactivate the target button if set
        if (targetButton != null) {
            targetButton.deactivate();
        }
    }
    
    /**
     * Checks if the action is currently active.
     * 
     * @return True if active, false otherwise
     */
    public boolean isActive() {
        return isActive;
    }
    
    /**
     * Sets the active state without executing any actions.
     * Useful for rewinding operations.
     * 
     * @param active Whether the action is active
     */
    public void setActive(boolean active) {
        this.isActive = active;
        
        // Set the start time to the current time when activating
        if (active) {
            this.startTime = System.currentTimeMillis();
        } else {
            this.startTime = 0;
        }
    }
    
    @Override
    public String getDescription() {
        String baseDesc = wrappedAction != null ? wrappedAction.getDescription() : "No action";
        return "Timed (" + (durationMillis / 1000f) + "s): " + baseDesc;
    }
} 