package com.niravramdhanie.twod.game.actions;

import java.util.ArrayList;
import java.util.List;

/**
 * An action that only executes when all required buttons are activated.
 * Used for scenarios where multiple buttons need to be pressed simultaneously.
 */
public class MultiButtonAction implements Action {
    private List<String> requiredButtonIds;
    private List<Boolean> buttonStates;
    private Action targetAction;
    private boolean permanentActivation;
    private boolean wasActivated;
    
    /**
     * Creates a new MultiButtonAction.
     * 
     * @param targetAction The action to execute when all buttons are activated
     * @param permanentActivation If true, once activated, the action stays activated even if buttons are released
     */
    public MultiButtonAction(Action targetAction, boolean permanentActivation) {
        this.targetAction = targetAction;
        this.requiredButtonIds = new ArrayList<>();
        this.buttonStates = new ArrayList<>();
        this.permanentActivation = permanentActivation;
        this.wasActivated = false;
    }
    
    /**
     * Adds a button requirement.
     * 
     * @param buttonId The ID of the required button
     */
    public void addRequiredButton(String buttonId) {
        requiredButtonIds.add(buttonId);
        buttonStates.add(false);
    }
    
    /**
     * Updates the state of a button.
     * 
     * @param buttonId The ID of the button
     * @param isActivated Whether the button is activated
     * @return True if the update changed the overall activation state, false otherwise
     */
    public boolean updateButtonState(String buttonId, boolean isActivated) {
        int index = requiredButtonIds.indexOf(buttonId);
        if (index == -1) {
            System.out.println("Button ID not found: " + buttonId);
            return false;
        }
        
        // Update the button state
        boolean oldState = buttonStates.get(index);
        buttonStates.set(index, isActivated);
        
        boolean stateChanged = oldState != isActivated;
        if (stateChanged) {
            System.out.println("Button " + buttonId + " is now " + (isActivated ? "activated" : "deactivated"));
        }
        
        // Check if all buttons are now activated
        boolean allActivated = checkAllButtonsActivated();
        
        if (allActivated) {
            System.out.println("All buttons are now activated!");
            
            // If this is the first time all buttons are activated, record it
            if (!wasActivated) {
                wasActivated = true;
                System.out.println("Permanent activation achieved!");
            }
        } else {
            // Log which buttons are still needed
            if (isActivated) {
                logRemainingButtons();
            }
        }
        
        // Execute the target action if we're activated
        if ((allActivated || (permanentActivation && wasActivated)) && targetAction != null) {
            if (allActivated) {
                System.out.println("Executing target action because all buttons are active");
            } else {
                System.out.println("Executing target action because of permanent activation");
            }
            targetAction.execute();
            return true;
        }
        
        return false;
    }
    
    /**
     * Logs information about which buttons still need to be activated.
     */
    private void logRemainingButtons() {
        List<String> missingButtons = new ArrayList<>();
        for (int i = 0; i < requiredButtonIds.size(); i++) {
            if (!buttonStates.get(i)) {
                missingButtons.add(requiredButtonIds.get(i));
            }
        }
        
        if (!missingButtons.isEmpty()) {
            System.out.println("Still need to activate: " + String.join(", ", missingButtons));
        }
    }
    
    /**
     * Checks if all required buttons are activated.
     * 
     * @return True if all buttons are activated, false otherwise
     */
    private boolean checkAllButtonsActivated() {
        for (Boolean state : buttonStates) {
            if (!state) {
                return false;
            }
        }
        return !buttonStates.isEmpty();
    }
    
    @Override
    public void execute() {
        // This action doesn't directly execute; it's controlled by button states
        // The actual execution happens in updateButtonState
    }
    
    @Override
    public String getDescription() {
        int totalButtons = requiredButtonIds.size();
        int activatedButtons = 0;
        
        for (Boolean state : buttonStates) {
            if (state) {
                activatedButtons++;
            }
        }
        
        String actionDesc = targetAction != null ? targetAction.getDescription() : "No action";
        return "Multi-button (" + activatedButtons + "/" + totalButtons + " active): " + actionDesc;
    }
    
    /**
     * Resets the activation state.
     */
    public void reset() {
        wasActivated = false;
        for (int i = 0; i < buttonStates.size(); i++) {
            buttonStates.set(i, false);
        }
    }
    
    /**
     * Gets whether this action has been permanently activated.
     * 
     * @return True if permanently activated, false otherwise
     */
    public boolean isPermanentlyActivated() {
        return permanentActivation && wasActivated;
    }
    
    /**
     * Sets whether this action uses permanent activation.
     * 
     * @param permanentActivation True for permanent activation, false for temporary
     */
    public void setPermanentActivation(boolean permanentActivation) {
        this.permanentActivation = permanentActivation;
    }
} 