package com.niravramdhanie.twod.game.actions;

/**
 * A simple action that prints a message to the console.
 */
public class PrintAction implements Action {
    private String message;
    
    /**
     * Creates a new print action with the given message.
     * 
     * @param message The message to print
     */
    public PrintAction(String message) {
        this.message = message;
    }
    
    /**
     * Creates a new print action with the default message.
     */
    public PrintAction() {
        this("Button pressed");
    }
    
    @Override
    public void execute() {
        System.out.println(message);
    }
    
    @Override
    public String getDescription() {
        return "Prints: " + message;
    }
    
    /**
     * Sets the message to print.
     * 
     * @param message The new message
     */
    public void setMessage(String message) {
        this.message = message;
    }
    
    /**
     * Gets the message that will be printed.
     * 
     * @return The message
     */
    public String getMessage() {
        return message;
    }
} 