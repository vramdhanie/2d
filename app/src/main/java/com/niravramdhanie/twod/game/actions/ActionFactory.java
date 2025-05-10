package com.niravramdhanie.twod.game.actions;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.niravramdhanie.twod.game.entity.Button;

/**
 * Factory class for creating different types of actions for buttons.
 */
public class ActionFactory {
    private static final Random random = new Random();
    
    /**
     * Action types enum
     */
    public enum ActionType {
        MESSAGE,
        TIMED,
        TOGGLE,
        DOOR,
        MULTI,
        CYCLING_MESSAGE,
        TIMED_TOGGLE,     // A timed action that toggles
        COMBINED         // Multiple actions combined
    }
    
    /**
     * Creates an action of the specified type.
     * 
     * @param type The type of action to create
     * @param button The button that will use this action (needed for timed actions)
     * @return The created action
     */
    public static Action createAction(ActionType type, Button button) {
        switch (type) {
            case MESSAGE:
                return new MessageAction("Button pressed: " + type.name());
                
            case CYCLING_MESSAGE:
                return new MessageAction(true, 
                    "This is message 1", 
                    "This is message 2", 
                    "This is message 3",
                    "Press again to cycle to the next message");
                
            case TIMED:
                MessageAction msgAction = new MessageAction("Timed button pressed! Will deactivate in 3 seconds.");
                TimedAction timedAction = new TimedAction(msgAction, 3000);
                timedAction.setTargetButton(button);
                return timedAction;
                
            case TOGGLE:
                DoorAction doorAction = new DoorAction("toggle_door_" + random.nextInt(1000));
                doorAction.setDoorStateChangeListener((doorId, isOpen) -> {
                    System.out.println("Door listener: Door " + doorId + " is now " + (isOpen ? "open" : "closed"));
                });
                return new ToggleAction(doorAction, doorAction);
                
            case DOOR:
                DoorAction simpleDoor = new DoorAction("simple_door_" + random.nextInt(1000));
                return simpleDoor;
                
            case MULTI:
                // Create a multi-action with several different actions
                MessageAction firstAction = new MessageAction("First action executed!");
                MessageAction secondAction = new MessageAction("Second action executed!");
                DoorAction thirdAction = new DoorAction("multi_door_" + random.nextInt(1000));
                thirdAction.setDoorStateChangeListener((doorId, isOpen) -> {
                    System.out.println("MultiAction door: " + doorId + " is now " + (isOpen ? "open" : "closed"));
                });
                
                return new MultiAction(firstAction, secondAction, thirdAction);
                
            case TIMED_TOGGLE:
                // Create a timed action that wraps a toggle action
                ToggleAction toggleAction = new ToggleAction(
                    new MessageAction("Toggled OFF"),
                    new MessageAction("Toggled ON")
                );
                TimedAction timedToggle = new TimedAction(toggleAction, 5000);
                timedToggle.setTargetButton(button);
                return timedToggle;
                
            case COMBINED:
                // Create a combination of different action types
                // This example creates a multi-action with cycling messages and a door action
                MessageAction cyclingMsg = new MessageAction(true, 
                    "Combined action - Message 1",
                    "Combined action - Message 2",
                    "Combined action - Message 3");
                
                DoorAction combinedDoor = new DoorAction("combined_door_" + random.nextInt(1000));
                combinedDoor.setDoorStateChangeListener((doorId, isOpen) -> {
                    System.out.println("Combined door: " + doorId + " is now " + (isOpen ? "open" : "closed"));
                });
                
                return new MultiAction(cyclingMsg, combinedDoor);
                
            default:
                return new MessageAction("Default action");
        }
    }
    
    /**
     * Creates a random action.
     * 
     * @param button The button that will use this action
     * @return A randomly selected action
     */
    public static Action createRandomAction(Button button) {
        ActionType[] types = ActionType.values();
        ActionType randomType = types[random.nextInt(types.length)];
        
        return createAction(randomType, button);
    }
    
    /**
     * Creates one of each action type.
     * 
     * @param button The button that will use these actions (for timed actions)
     * @return A list containing one of each action type
     */
    public static List<Action> createAllActionTypes(Button button) {
        List<Action> actions = new ArrayList<>();
        
        for (ActionType type : ActionType.values()) {
            actions.add(createAction(type, button));
        }
        
        return actions;
    }
    
    /**
     * Creates a timed message action.
     * 
     * @param message The message to display
     * @param durationMillis The duration in milliseconds
     * @param button The button to deactivate
     * @return A timed message action
     */
    public static TimedAction createTimedMessage(String message, int durationMillis, Button button) {
        MessageAction msgAction = new MessageAction(message);
        TimedAction timedAction = new TimedAction(msgAction, durationMillis);
        timedAction.setTargetButton(button);
        return timedAction;
    }
    
    /**
     * Creates a cycling message action.
     * 
     * @param messages The messages to cycle through
     * @return A cycling message action
     */
    public static MessageAction createCyclingMessage(String... messages) {
        return new MessageAction(true, messages);
    }
} 