package com.niravramdhanie.twod.game.actions;

/**
 * An action that simulates opening or closing a door.
 */
public class DoorAction implements Action {
    private String doorId;
    private boolean doorOpen;
    private DoorStateChangeListener listener;
    
    /**
     * Creates a new door action.
     * 
     * @param doorId The ID of the door
     */
    public DoorAction(String doorId) {
        this.doorId = doorId;
        this.doorOpen = false;
        this.listener = null;
    }
    
    @Override
    public void execute() {
        // Toggle the door state
        doorOpen = !doorOpen;
        
        // Notify the listener if present
        if (listener != null) {
            listener.onDoorStateChanged(doorId, doorOpen);
        }
        
        // Print a message to the console
        System.out.println("Door " + doorId + " is now " + (doorOpen ? "open" : "closed"));
    }
    
    @Override
    public String getDescription() {
        return "Door '" + doorId + "' - " + (doorOpen ? "OPEN" : "CLOSED");
    }
    
    /**
     * Interface for components that need to be notified of door state changes.
     */
    public interface DoorStateChangeListener {
        /**
         * Called when a door's state changes.
         * 
         * @param doorId The ID of the door
         * @param isOpen True if the door is now open, false if closed
         */
        void onDoorStateChanged(String doorId, boolean isOpen);
    }
    
    /**
     * Sets the listener for door state changes.
     * 
     * @param listener The listener to notify when the door state changes
     */
    public void setDoorStateChangeListener(DoorStateChangeListener listener) {
        this.listener = listener;
    }
    
    /**
     * Gets the door ID.
     * 
     * @return The door ID
     */
    public String getDoorId() {
        return doorId;
    }
    
    /**
     * Sets the door ID.
     * 
     * @param doorId The door ID
     */
    public void setDoorId(String doorId) {
        this.doorId = doorId;
    }
    
    /**
     * Checks if the door is open.
     * 
     * @return True if the door is open, false if closed
     */
    public boolean isDoorOpen() {
        return doorOpen;
    }
    
    /**
     * Sets the door state.
     * 
     * @param doorOpen True to set as open, false for closed
     */
    public void setDoorOpen(boolean doorOpen) {
        boolean oldState = this.doorOpen;
        this.doorOpen = doorOpen;
        
        // If the state changed and we have a listener, notify it
        if (oldState != doorOpen && listener != null) {
            listener.onDoorStateChanged(doorId, doorOpen);
        }
    }
} 