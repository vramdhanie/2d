package com.niravramdhanie.twod.game.actions;

/**
 * An action that toggles between two different actions.
 */
public class ToggleAction implements Action {
    private Action offAction;
    private Action onAction;
    private boolean toggled;
    
    /**
     * Creates a new toggle action with actions for on and off states.
     * 
     * @param offAction The action to execute when toggled off
     * @param onAction The action to execute when toggled on
     */
    public ToggleAction(Action offAction, Action onAction) {
        this.offAction = offAction;
        this.onAction = onAction;
        this.toggled = false;
    }
    
    /**
     * Creates a toggle action with the same action for both states.
     * 
     * @param action The action to execute on every toggle
     */
    public ToggleAction(Action action) {
        this(action, action);
    }
    
    @Override
    public void execute() {
        // Toggle the state
        toggled = !toggled;
        
        // Execute the appropriate action based on the toggled state
        if (toggled) {
            if (onAction != null) {
                onAction.execute();
            }
        } else {
            if (offAction != null) {
                offAction.execute();
            }
        }
    }
    
    @Override
    public String getDescription() {
        if (toggled) {
            String onDesc = onAction != null ? onAction.getDescription() : "No action";
            return "Toggle ON: " + onDesc;
        } else {
            String offDesc = offAction != null ? offAction.getDescription() : "No action";
            return "Toggle OFF: " + offDesc;
        }
    }
    
    /**
     * Checks if this action is toggled on.
     * 
     * @return True if toggled on, false if toggled off
     */
    public boolean isToggled() {
        return toggled;
    }
    
    /**
     * Sets the toggled state.
     * 
     * @param toggled The new toggled state
     */
    public void setToggled(boolean toggled) {
        this.toggled = toggled;
    }
    
    /**
     * Gets the action executed when toggled off.
     * 
     * @return The off action
     */
    public Action getOffAction() {
        return offAction;
    }
    
    /**
     * Sets the action to execute when toggled off.
     * 
     * @param offAction The new off action
     */
    public void setOffAction(Action offAction) {
        this.offAction = offAction;
    }
    
    /**
     * Gets the action executed when toggled on.
     * 
     * @return The on action
     */
    public Action getOnAction() {
        return onAction;
    }
    
    /**
     * Sets the action to execute when toggled on.
     * 
     * @param onAction The new on action
     */
    public void setOnAction(Action onAction) {
        this.onAction = onAction;
    }
} 