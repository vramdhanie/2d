package com.niravramdhanie.twod.game.actions;

/**
 * Interface for actions that can be executed when a button is pressed.
 */
public interface Action {
    /**
     * Executes the action.
     */
    void execute();
    
    /**
     * Gets a description of the action.
     * 
     * @return A description of what the action does
     */
    String getDescription();
} 