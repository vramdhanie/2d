package com.niravramdhanie.twod.game.actions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * An action that executes multiple actions in sequence.
 */
public class MultiAction implements Action {
    private List<Action> actions;
    
    /**
     * Creates a new multi-action with the given actions.
     * 
     * @param actions The actions to execute in sequence
     */
    public MultiAction(Action... actions) {
        this.actions = new ArrayList<>(Arrays.asList(actions));
    }
    
    /**
     * Creates a new multi-action with a list of actions.
     * 
     * @param actions The list of actions to execute
     */
    public MultiAction(List<Action> actions) {
        this.actions = new ArrayList<>(actions);
    }
    
    @Override
    public void execute() {
        // Execute each action in sequence
        for (Action action : actions) {
            if (action != null) {
                action.execute();
            }
        }
    }
    
    @Override
    public String getDescription() {
        StringBuilder description = new StringBuilder("Multi-action (");
        description.append(actions.size()).append(" actions)");
        
        // Add descriptions of individual actions if there aren't too many
        if (actions.size() <= 3) {
            description.append(": ");
            for (int i = 0; i < actions.size(); i++) {
                if (i > 0) {
                    description.append(" → ");
                }
                Action action = actions.get(i);
                description.append(action != null ? action.getDescription() : "null");
            }
        }
        
        return description.toString();
    }
    
    /**
     * Adds an action to the sequence.
     * 
     * @param action The action to add
     */
    public void addAction(Action action) {
        actions.add(action);
    }
    
    /**
     * Removes an action from the sequence.
     * 
     * @param action The action to remove
     * @return True if the action was found and removed, false otherwise
     */
    public boolean removeAction(Action action) {
        return actions.remove(action);
    }
    
    /**
     * Gets all actions in the sequence.
     * 
     * @return The list of actions
     */
    public List<Action> getActions() {
        return actions;
    }
    
    /**
     * Clears all actions.
     */
    public void clearActions() {
        actions.clear();
    }
    
    /**
     * Gets the number of actions.
     * 
     * @return The number of actions
     */
    public int getActionCount() {
        return actions.size();
    }
} 