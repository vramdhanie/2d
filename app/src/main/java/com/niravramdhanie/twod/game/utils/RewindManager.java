package com.niravramdhanie.twod.game.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.niravramdhanie.twod.game.actions.Action;
import com.niravramdhanie.twod.game.actions.DoorAction;
import com.niravramdhanie.twod.game.actions.ToggleAction;
import com.niravramdhanie.twod.game.entity.BallPlayer;
import com.niravramdhanie.twod.game.entity.Button;

/**
 * Manages the rewind feature for the game.
 * Records player position, timer value, and button states.
 * Replays recorded actions when rewinding.
 */
public class RewindManager {
    // Enum to track rewind state
    public enum RewindState {
        IDLE,       // Not recording or rewinding
        RECORDING,  // Currently recording actions
        REWINDING   // Currently rewinding and replaying actions
    }
    
    // Current state of the rewind system
    private RewindState currentState = RewindState.IDLE;
    
    // Timestamp relative to the start of recording
    private long recordingStartTime;
    private long rewindStartTime;
    
    // Recorded initial state
    private float playerStartX;
    private float playerStartY;
    private int timerStartValue;
    private List<ButtonState> initialButtonStates = new ArrayList<>();
    private Map<String, Boolean> initialToggleStates = new HashMap<>();
    private Map<String, Boolean> initialDoorStates = new HashMap<>();
    
    // Recorded actions
    private List<RecordedAction> recordedActions = new ArrayList<>();
    
    // Reference to game objects
    private BallPlayer player;
    private List<Button> buttons;
    private TimerManager timer;
    
    // Map to track toggle states during replay
    private Map<String, Boolean> currentToggleStates = new HashMap<>();
    
    /**
     * Creates a new RewindManager.
     * 
     * @param player The player to record/restore
     * @param buttons The list of buttons to record/restore
     * @param timer The timer to record/restore
     */
    public RewindManager(BallPlayer player, List<Button> buttons, TimerManager timer) {
        this.player = player;
        this.buttons = buttons;
        this.timer = timer;
    }
    
    /**
     * Starts recording player actions and game state.
     */
    public void startRecording() {
        // Clear previous recordings
        recordedActions.clear();
        initialButtonStates.clear();
        initialToggleStates.clear();
        initialDoorStates.clear();
        currentToggleStates.clear();
        
        // Record initial state
        playerStartX = player.getX();
        playerStartY = player.getY();
        timerStartValue = timer.getTime();
        
        // Record initial button states and toggle states
        for (Button button : buttons) {
            String buttonId = getButtonId(button);
            boolean isActivated = button.isActivated();
            initialButtonStates.add(new ButtonState(buttonId, isActivated));
            
            // Store toggle states for ToggleAction buttons
            Action action = button.getAction();
            if (action instanceof ToggleAction) {
                ToggleAction toggleAction = (ToggleAction) action;
                initialToggleStates.put(buttonId, toggleAction.isToggled());
                
                // Store door states for DoorActions inside ToggleActions
                if (toggleAction.getOnAction() instanceof DoorAction) {
                    DoorAction doorAction = (DoorAction) toggleAction.getOnAction();
                    initialDoorStates.put(doorAction.getDoorId(), doorAction.isDoorOpen());
                }
            } else if (action instanceof DoorAction) {
                DoorAction doorAction = (DoorAction) action;
                initialDoorStates.put(doorAction.getDoorId(), doorAction.isDoorOpen());
            }
        }
        
        // Set recording state
        currentState = RewindState.RECORDING;
        recordingStartTime = System.currentTimeMillis();
        
        System.out.println("Rewind: Started recording at position (" + playerStartX + ", " + playerStartY + 
                          "), time: " + timerStartValue);
    }
    
    /**
     * Records a button activation.
     * 
     * @param button The button that was activated
     * @param activated Whether the button was activated or deactivated
     */
    public void recordButtonActivation(Button button, boolean activated) {
        if (currentState != RewindState.RECORDING) {
            return;
        }
        
        String buttonId = getButtonId(button);
        long timestamp = System.currentTimeMillis() - recordingStartTime;
        recordedActions.add(new RecordedAction(ActionType.BUTTON_ACTIVATION, timestamp, buttonId, activated));
        
        System.out.println("Rewind: Recorded button " + buttonId + " " + 
                          (activated ? "activation" : "deactivation") + " at time " + timestamp + "ms");
    }
    
    /**
     * Starts rewinding the game to the recorded state.
     */
    public void startRewinding() {
        if (currentState != RewindState.RECORDING) {
            return;
        }
        
        // Reset player position
        player.setX(playerStartX);
        player.setY(playerStartY);
        
        // Reset timer
        timer.setTime(timerStartValue);
        
        // Initialize current toggle states with the initial values
        currentToggleStates.clear();
        currentToggleStates.putAll(initialToggleStates);
        
        // Reset button states and toggle states
        for (ButtonState state : initialButtonStates) {
            for (Button button : buttons) {
                String buttonId = getButtonId(button);
                if (buttonId.equals(state.buttonId)) {
                    // Set button activation state without triggering actions
                    button.setActivated(state.activated);
                    
                    // Handle toggle states using the new method
                    boolean initialToggled = initialToggleStates.getOrDefault(buttonId, false);
                    button.forceSetToggleState(initialToggled);
                    
                    // For actions that aren't toggles but might be DoorActions
                    Action action = button.getAction();
                    if (!(action instanceof ToggleAction) && action instanceof DoorAction) {
                        DoorAction doorAction = (DoorAction) action;
                        String doorId = doorAction.getDoorId();
                        boolean initialDoorOpen = initialDoorStates.getOrDefault(doorId, false);
                        doorAction.setDoorOpen(initialDoorOpen);
                    }
                    
                    break;
                }
            }
        }
        
        // Mark all actions as not applied
        for (RecordedAction action : recordedActions) {
            action.applied = false;
        }
        
        // Sort actions by timestamp to ensure correct replay order
        Collections.sort(recordedActions, Comparator.comparingLong(a -> a.timestamp));
        
        // Start rewinding
        currentState = RewindState.REWINDING;
        rewindStartTime = System.currentTimeMillis();
        
        System.out.println("Rewind: Started rewinding to position (" + playerStartX + ", " + playerStartY + 
                          "), time: " + timerStartValue);
    }
    
    /**
     * Updates the rewind system.
     * Should be called on each game tick.
     */
    public void update() {
        if (currentState != RewindState.REWINDING) {
            return;
        }
        
        // Calculate current time relative to rewind start
        long currentRelativeTime = System.currentTimeMillis() - rewindStartTime;
        
        // Apply any actions that should occur at this time
        for (RecordedAction action : recordedActions) {
            if (action.timestamp <= currentRelativeTime && !action.applied) {
                applyAction(action);
                action.applied = true;
                
                // Add a small delay to make actions visually apparent
                try {
                    Thread.sleep(30);
                } catch (InterruptedException e) {
                    // Ignore
                }
            }
        }
        
        // Check if all actions have been applied
        boolean allApplied = true;
        for (RecordedAction action : recordedActions) {
            if (!action.applied) {
                allApplied = false;
                break;
            }
        }
        
        // If all actions are applied, reset rewind state
        if (allApplied) {
            // Sync final states to ensure consistency
            synchronizeFinalStates();
            
            // Reset rewind state
            currentState = RewindState.IDLE;
            System.out.println("Rewind: All actions replayed, returning to idle state");
        }
    }
    
    /**
     * Synchronize final states to ensure consistency between buttons, toggles, and doors
     */
    private void synchronizeFinalStates() {
        // Apply the current toggle states from our tracking
        for (Button button : buttons) {
            String buttonId = getButtonId(button);
            Action action = button.getAction();
            
            // For toggle actions, apply the final tracked state
            if (action instanceof ToggleAction) {
                ToggleAction toggleAction = (ToggleAction) action;
                if (currentToggleStates.containsKey(buttonId)) {
                    boolean finalToggleState = currentToggleStates.get(buttonId);
                    button.forceSetToggleState(finalToggleState);
                }
                
                // Ensure door state is consistent
                if (toggleAction.getOnAction() instanceof DoorAction) {
                    DoorAction doorAction = (DoorAction) toggleAction.getOnAction();
                    boolean toggledState = toggleAction.isToggled();
                    doorAction.setDoorOpen(toggledState);
                }
            }
        }
    }
    
    /**
     * Applies a recorded action.
     * 
     * @param action The action to apply
     */
    private void applyAction(RecordedAction action) {
        if (action.type == ActionType.BUTTON_ACTIVATION) {
            // Find the button with matching ID
            for (Button button : buttons) {
                String buttonId = getButtonId(button);
                if (buttonId.equals(action.buttonId)) {
                    // Record the current state before making changes
                    boolean wasActivated = button.isActivated();
                    
                    // Apply the action
                    if (action.activated) {
                        // If activating a button, use the regular activate method
                        // which will trigger any associated actions
                        button.activate();
                        
                        // Update toggle state tracking
                        if (button.getAction() instanceof ToggleAction) {
                            ToggleAction toggleAction = (ToggleAction) button.getAction();
                            boolean newToggleState = toggleAction.isToggled();
                            currentToggleStates.put(buttonId, newToggleState);
                            
                            // Ensure door state is consistent with toggle state
                            if (toggleAction.getOnAction() instanceof DoorAction) {
                                DoorAction doorAction = (DoorAction) toggleAction.getOnAction();
                                doorAction.setDoorOpen(newToggleState);
                            }
                        }
                    } else {
                        // If deactivating, set without executing actions
                        button.setActivated(false);
                    }
                    
                    System.out.println("Rewind: Replayed button " + action.buttonId + 
                                     " " + (action.activated ? "activation" : "deactivation") + 
                                     " at time " + action.timestamp + "ms");
                    break;
                }
            }
        }
    }
    
    /**
     * Toggles the rewind feature when the 'R' key is pressed.
     */
    public void toggleRewind() {
        switch (currentState) {
            case IDLE:
                startRecording();
                break;
                
            case RECORDING:
                startRewinding();
                break;
                
            case REWINDING:
                // Do nothing if already rewinding
                break;
        }
    }
    
    /**
     * Gets the current state of the rewind system.
     * 
     * @return The current rewind state
     */
    public RewindState getCurrentState() {
        return currentState;
    }
    
    /**
     * Generate a unique ID for a button based on its position.
     * 
     * @param button The button to get an ID for
     * @return A unique string ID
     */
    private String getButtonId(Button button) {
        // Create a unique ID based on the button's position
        return "btn_" + (int)button.getX() + "_" + (int)button.getY();
    }
    
    /**
     * Records the state of a button.
     */
    private static class ButtonState {
        String buttonId;
        boolean activated;
        
        ButtonState(String buttonId, boolean activated) {
            this.buttonId = buttonId;
            this.activated = activated;
        }
    }
    
    /**
     * Types of actions that can be recorded.
     */
    private enum ActionType {
        BUTTON_ACTIVATION
    }
    
    /**
     * A recorded action with timestamp.
     */
    private static class RecordedAction {
        ActionType type;
        long timestamp;
        String buttonId;
        boolean activated;
        boolean applied = false;
        
        RecordedAction(ActionType type, long timestamp, String buttonId, boolean activated) {
            this.type = type;
            this.timestamp = timestamp;
            this.buttonId = buttonId;
            this.activated = activated;
        }
    }
} 