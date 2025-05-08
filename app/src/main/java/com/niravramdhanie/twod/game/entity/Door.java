package com.niravramdhanie.twod.game.entity;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import com.niravramdhanie.twod.game.actions.DoorAction;
import com.niravramdhanie.twod.game.utils.ResourceLoader;

/**
 * A door entity that can be opened or closed.
 * When closed, it blocks player movement.
 * When open, it allows the player to pass through.
 * Extends Block to work with collision detection.
 */
public class Door extends Block implements DoorAction.DoorStateChangeListener {
    private String id;
    private boolean isOpen;
    private BufferedImage doorImage;
    private BufferedImage doorOpenImage;
    private Color doorColor = new Color(139, 69, 19); // Brown door color
    private Color frameColor = new Color(101, 67, 33); // Darker frame color
    
    /**
     * Creates a new door entity.
     * 
     * @param x The x position
     * @param y The y position
     * @param width The width
     * @param height The height
     * @param id The unique ID of the door
     */
    public Door(float x, float y, int width, int height, String id) {
        super(x, y, width, height);
        this.id = id;
        this.isOpen = false;
        
        try {
            // Load door images
            doorImage = ResourceLoader.loadImage("/sprites/door_closed.png");
            doorOpenImage = ResourceLoader.loadImage("/sprites/door_open.png");
        } catch (Exception e) {
            System.err.println("Error loading door images: " + e.getMessage());
            doorImage = null;
            doorOpenImage = null;
        }
    }
    
    @Override
    public void update() {
        // Currently nothing to update for the door
    }
    
    @Override
    public void render(Graphics2D g) {
        if (doorImage != null && doorOpenImage != null) {
            // Render using images
            if (isOpen) {
                g.drawImage(doorOpenImage, (int)position.x, (int)position.y, width, height, null);
            } else {
                g.drawImage(doorImage, (int)position.x, (int)position.y, width, height, null);
            }
        } else {
            // Fallback rendering with rectangles
            if (isOpen) {
                // Draw frame but no door
                drawDoorFrame(g);
            } else {
                // Draw both frame and door
                drawDoorFrame(g);
                drawDoorBody(g);
            }
        }
    }
    
    /**
     * Draws the door frame.
     * 
     * @param g The graphics context
     */
    private void drawDoorFrame(Graphics2D g) {
        // Draw the door frame
        g.setColor(frameColor);
        
        // Left side
        g.fillRect((int)position.x, (int)position.y, width / 8, height);
        
        // Right side
        g.fillRect((int)position.x + width - width / 8, (int)position.y, width / 8, height);
        
        // Top
        g.fillRect((int)position.x, (int)position.y, width, height / 8);
        
        // Bottom
        g.fillRect((int)position.x, (int)position.y + height - height / 8, width, height / 8);
    }
    
    /**
     * Draws the door body.
     * 
     * @param g The graphics context
     */
    private void drawDoorBody(Graphics2D g) {
        // Door body fill
        int doorBodyX = (int)position.x + width / 8;
        int doorBodyY = (int)position.y + height / 8;
        int doorBodyWidth = width - width / 4;
        int doorBodyHeight = height - height / 4;
        
        // Create a gradient for 3D effect
        GradientPaint gradient = new GradientPaint(
            doorBodyX, doorBodyY, doorColor.brighter(),
            doorBodyX + doorBodyWidth, doorBodyY, doorColor.darker()
        );
        
        // Save original paint
        java.awt.Paint originalPaint = g.getPaint();
        
        // Apply gradient and draw door
        g.setPaint(gradient);
        g.fillRect(doorBodyX, doorBodyY, doorBodyWidth, doorBodyHeight);
        
        // Add a door handle
        g.setColor(Color.BLACK);
        g.fillOval(doorBodyX + doorBodyWidth - doorBodyWidth / 6, 
                  doorBodyY + doorBodyHeight / 2, 
                  doorBodyWidth / 10, 
                  doorBodyWidth / 10);
        
        // Restore original paint
        g.setPaint(originalPaint);
    }
    
    /**
     * Opens the door.
     */
    public void open() {
        isOpen = true;
    }
    
    /**
     * Closes the door.
     */
    public void close() {
        isOpen = false;
    }
    
    /**
     * Checks if the door is open.
     * 
     * @return True if open, false if closed
     */
    public boolean isOpen() {
        return isOpen;
    }
    
    /**
     * Gets the door ID.
     * 
     * @return The door ID
     */
    public String getId() {
        return id;
    }
    
    /**
     * Sets the door ID.
     * 
     * @param id The door ID
     */
    public void setId(String id) {
        this.id = id;
    }
    
    /**
     * Toggles the door state.
     * 
     * @return The new door state
     */
    public boolean toggle() {
        isOpen = !isOpen;
        return isOpen;
    }
    
    @Override
    public void onDoorStateChanged(String doorId, boolean isOpen) {
        if (this.id.equals(doorId)) {
            this.isOpen = isOpen;
        }
    }
} 