package com.niravramdhanie.twod.game.actions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.niravramdhanie.twod.game.entity.Door;

/**
 * Controls and manages doors in the game.
 * Tracks door states and provides methods to open/close doors by ID.
 */
public class DoorController implements DoorAction.DoorStateChangeListener {
    private List<Door> doors;
    private Map<String, Door> doorMap;
    
    /**
     * Creates a new door controller.
     */
    public DoorController() {
        doors = new ArrayList<>();
        doorMap = new HashMap<>();
    }
    
    /**
     * Registers a door with the controller.
     * 
     * @param door The door to register
     */
    public void registerDoor(Door door) {
        doors.add(door);
        doorMap.put(door.getId(), door);
        
        // Add this controller as a listener to the door
        for (DoorAction action : findDoorActions(door.getId())) {
            action.setDoorStateChangeListener(this);
        }
    }
    
    /**
     * Finds all door actions for a specific door ID.
     * 
     * @param doorId The door ID
     * @return List of door actions
     */
    private List<DoorAction> findDoorActions(String doorId) {
        // This would typically be connected to a button registry or similar,
        // but for now, we'll return an empty list and register actions manually
        return new ArrayList<>();
    }
    
    /**
     * Opens a door by ID.
     * 
     * @param doorId The ID of the door to open
     * @return True if the door was found and opened, false otherwise
     */
    public boolean openDoor(String doorId) {
        Door door = doorMap.get(doorId);
        if (door != null) {
            door.open();
            return true;
        }
        return false;
    }
    
    /**
     * Closes a door by ID.
     * 
     * @param doorId The ID of the door to close
     * @return True if the door was found and closed, false otherwise
     */
    public boolean closeDoor(String doorId) {
        Door door = doorMap.get(doorId);
        if (door != null) {
            door.close();
            return true;
        }
        return false;
    }
    
    /**
     * Toggles a door's state by ID.
     * 
     * @param doorId The ID of the door to toggle
     * @return True if the door was found and toggled, false otherwise
     */
    public boolean toggleDoor(String doorId) {
        Door door = doorMap.get(doorId);
        if (door != null) {
            door.toggle();
            return true;
        }
        return false;
    }
    
    /**
     * Closes all doors managed by this controller.
     */
    public void closeAllDoors() {
        for (Door door : doors) {
            door.close();
        }
    }
    
    /**
     * Opens all doors managed by this controller.
     */
    public void openAllDoors() {
        for (Door door : doors) {
            door.open();
        }
    }
    
    /**
     * Gets all doors managed by this controller.
     * 
     * @return The list of doors
     */
    public List<Door> getDoors() {
        return doors;
    }
    
    /**
     * Gets a door by ID.
     * 
     * @param doorId The door ID
     * @return The door, or null if not found
     */
    public Door getDoor(String doorId) {
        return doorMap.get(doorId);
    }
    
    /**
     * Removes a door from the controller.
     * 
     * @param doorId The ID of the door to remove
     * @return True if the door was found and removed, false otherwise
     */
    public boolean removeDoor(String doorId) {
        Door door = doorMap.remove(doorId);
        if (door != null) {
            doors.remove(door);
            return true;
        }
        return false;
    }
    
    @Override
    public void onDoorStateChanged(String doorId, boolean isOpen) {
        // Propagate the door state change to the actual door
        Door door = doorMap.get(doorId);
        if (door != null) {
            if (isOpen) {
                door.open();
            } else {
                door.close();
            }
            System.out.println("DoorController: Door " + doorId + " is now " + 
                              (isOpen ? "open" : "closed"));
        } else {
            System.out.println("DoorController: Warning - Door " + doorId + 
                              " not found when trying to change state");
        }
    }
} 