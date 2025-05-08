package com.niravramdhanie.twod.game.actions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * An enhanced version of PrintAction that can cycle through multiple messages.
 */
public class MessageAction implements Action {
    private List<String> messages;
    private int currentMessageIndex;
    private boolean cycling;
    
    /**
     * Creates a new message action with a single message.
     * 
     * @param message The message to display
     */
    public MessageAction(String message) {
        this.messages = new ArrayList<>();
        this.messages.add(message);
        this.currentMessageIndex = 0;
        this.cycling = false;
    }
    
    /**
     * Creates a new message action with multiple messages that will cycle when executed.
     * 
     * @param cycling Whether to cycle through messages
     * @param messages The messages to cycle through
     */
    public MessageAction(boolean cycling, String... messages) {
        this.messages = new ArrayList<>(Arrays.asList(messages));
        this.currentMessageIndex = 0;
        this.cycling = cycling;
    }
    
    @Override
    public void execute() {
        // Print the current message
        if (!messages.isEmpty()) {
            System.out.println(getCurrentMessage());
            
            // Move to the next message if cycling is enabled
            if (cycling) {
                currentMessageIndex = (currentMessageIndex + 1) % messages.size();
            }
        }
    }
    
    @Override
    public String getDescription() {
        if (cycling) {
            return "Cycling message (" + (currentMessageIndex + 1) + "/" + messages.size() + ")";
        } else {
            return "Message: " + getCurrentMessage();
        }
    }
    
    /**
     * Gets the current message.
     * 
     * @return The current message
     */
    public String getCurrentMessage() {
        if (messages.isEmpty()) {
            return "No message";
        }
        return messages.get(currentMessageIndex);
    }
    
    /**
     * Gets all messages.
     * 
     * @return The list of messages
     */
    public List<String> getMessages() {
        return messages;
    }
    
    /**
     * Adds a message to the list.
     * 
     * @param message The message to add
     */
    public void addMessage(String message) {
        messages.add(message);
    }
    
    /**
     * Sets whether the action should cycle through messages.
     * 
     * @param cycling True to cycle, false to stay on the current message
     */
    public void setCycling(boolean cycling) {
        this.cycling = cycling;
    }
    
    /**
     * Checks if the action is cycling through messages.
     * 
     * @return True if cycling, false otherwise
     */
    public boolean isCycling() {
        return cycling;
    }
    
    /**
     * Gets the current message index.
     * 
     * @return The current message index
     */
    public int getCurrentMessageIndex() {
        return currentMessageIndex;
    }
    
    /**
     * Sets the current message index.
     * 
     * @param index The index to set
     */
    public void setCurrentMessageIndex(int index) {
        if (index >= 0 && index < messages.size()) {
            currentMessageIndex = index;
        }
    }
} 